package com.informales.pena;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.informales.pena.auth.UsuarioActual;
import com.informales.pena.consumo.ConsumoDto;
import com.informales.pena.consumo.ConsumoRequest;
import com.informales.pena.consumo.ConsumoService;
import com.informales.pena.cuenta.CuentaService;
import com.informales.pena.pago.Pago;
import com.informales.pena.pago.PagoRepository;
import com.informales.pena.producto.ProductoDto;
import com.informales.pena.producto.ProductoForm;
import com.informales.pena.producto.ProductoRepository;
import com.informales.pena.producto.ProductoService;
import com.informales.pena.producto.StockBajoEvent;
import com.informales.pena.usuario.Rol;
import com.informales.pena.usuario.Usuario;
import com.informales.pena.usuario.UsuarioForm;
import com.informales.pena.usuario.UsuarioRepository;
import com.informales.pena.usuario.UsuarioService;

@SpringBootTest
@Transactional
@RecordApplicationEvents
class CuentaYStockTest {

    @Autowired ProductoService productoService;
    @Autowired ProductoRepository productos;
    @Autowired ConsumoService consumoService;
    @Autowired CuentaService cuentaService;
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarios;
    @Autowired PagoRepository pagos;
    @Autowired ApplicationEvents eventos;

    UsuarioActual pepe;
    UsuarioActual lucia;
    ProductoDto cerveza;

    @BeforeEach
    void preparar() {
        pepe = miembro("pepe");
        lucia = miembro("lucia");
        cerveza = productoService.crear(new ProductoForm("Cerveza", new BigDecimal("1.00"), 10, 3, "🍺", true));
    }

    @Test
    void laDeudaUsaElPrecioDelMomentoDelApunte() {
        apuntar(pepe, 2);
        productoService.editar(cerveza.id(), new ProductoForm("Cerveza", new BigDecimal("1.50"), null, 3, "🍺", true));
        apuntar(pepe, 1);

        assertThat(cuentaService.deudaDe(pepe.id())).isEqualByComparingTo("3.50");
    }

    @Test
    void losPagosRestanDeLaDeuda() {
        apuntar(pepe, 4);
        Usuario admin = usuarios.findByUsernameIgnoreCase("admin").orElseThrow();
        pagos.save(new Pago(usuarios.getReferenceById(pepe.id()), new BigDecimal("3.00"), admin, null));

        assertThat(cuentaService.deudaDe(pepe.id())).isEqualByComparingTo("1.00");
        assertThat(cuentaService.deudas())
                .filteredOn(d -> d.usuarioId().equals(pepe.id()))
                .singleElement()
                .satisfies(d -> assertThat(d.deuda()).isEqualByComparingTo("1.00"));
    }

    @Test
    void apuntarDescuentaStockYAnularLoDevuelve() {
        ConsumoDto consumo = apuntar(pepe, 3);
        assertThat(stock()).isEqualTo(7);
        assertThat(cuentaService.deudaDe(pepe.id())).isEqualByComparingTo("3.00");

        consumoService.anular(pepe, consumo.id());

        assertThat(stock()).isEqualTo(10);
        assertThat(cuentaService.deudaDe(pepe.id())).isEqualByComparingTo("0");
    }

    @Test
    void noSePuedeApuntarMasDeLoQueHay() {
        assertThatThrownBy(() -> apuntar(pepe, 11))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No hay suficiente Cerveza");
        assertThat(stock()).isEqualTo(10);
    }

    @Test
    void unMiembroNoPuedeDeshacerElApunteDeOtro() {
        ConsumoDto consumo = apuntar(pepe, 1);

        assertThatThrownBy(() -> consumoService.anular(lucia, consumo.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("tus propios apuntes");
    }

    @Test
    void elAvisoDeStockBajoSoloSaltaAlCruzarElMinimo() {
        apuntar(pepe, 6);  // 10 -> 4: todavía por encima del mínimo (3)
        assertThat(eventos.stream(StockBajoEvent.class)).isEmpty();

        apuntar(pepe, 1);  // 4 -> 3: cruza el mínimo
        assertThat(eventos.stream(StockBajoEvent.class)).hasSize(1);

        apuntar(lucia, 1); // 3 -> 2: ya estaba bajo, no se repite
        assertThat(eventos.stream(StockBajoEvent.class)).hasSize(1);
    }

    private ConsumoDto apuntar(UsuarioActual quien, int cantidad) {
        return consumoService.registrar(quien, new ConsumoRequest(cerveza.id(), cantidad, null));
    }

    private int stock() {
        return productos.findById(cerveza.id()).orElseThrow().getStock();
    }

    private UsuarioActual miembro(String username) {
        var dto = usuarioService.crear(new UsuarioForm(username, username, null, "secreto123", Rol.MIEMBRO, true));
        return UsuarioActual.de(usuarios.findById(dto.id()).orElseThrow());
    }
}
