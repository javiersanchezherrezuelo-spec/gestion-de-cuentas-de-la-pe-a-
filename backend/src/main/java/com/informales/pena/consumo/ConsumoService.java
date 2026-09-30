package com.informales.pena.consumo;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.informales.pena.auth.UsuarioActual;
import com.informales.pena.producto.Producto;
import com.informales.pena.producto.ProductoService;
import com.informales.pena.usuario.Usuario;
import com.informales.pena.usuario.UsuarioService;

@Service
public class ConsumoService {

    private final ConsumoRepository consumos;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final long minutosDeshacer;

    public ConsumoService(ConsumoRepository consumos, ProductoService productoService, UsuarioService usuarioService,
                          @Value("${app.consumo.minutos-deshacer}") long minutosDeshacer) {
        this.consumos = consumos;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.minutosDeshacer = minutosDeshacer;
    }

    public long getMinutosDeshacer() {
        return minutosDeshacer;
    }

    @Transactional
    public ConsumoDto registrar(UsuarioActual actual, ConsumoRequest req) {
        Long usuarioId = req.usuarioId() != null ? req.usuarioId() : actual.id();
        if (!usuarioId.equals(actual.id()) && !actual.esAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede apuntar a otra persona");
        }
        Usuario usuario = usuarioService.buscar(usuarioId);
        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ese usuario está desactivado");
        }

        Producto producto = productoService.bloquear(req.productoId());
        if (!producto.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este producto ya no está disponible");
        }
        productoService.moverStock(producto, -req.cantidad());

        Consumo consumo = consumos.save(new Consumo(usuario, producto, req.cantidad()));
        return ConsumoDto.de(consumo, minutosDeshacer);
    }

    /**
     * Anula un apunte y devuelve las unidades al stock.
     * Un miembro solo puede anular los suyos y durante los primeros minutos; un admin, cualquiera.
     */
    @Transactional
    public void anular(UsuarioActual actual, Long consumoId) {
        Consumo consumo = consumos.findById(consumoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El apunte no existe"));
        if (consumo.isAnulado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este apunte ya estaba anulado");
        }
        if (!actual.esAdmin()) {
            if (!consumo.getUsuario().getId().equals(actual.id())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes deshacer tus propios apuntes");
            }
            Instant limite = consumo.getFecha().plus(Duration.ofMinutes(minutosDeshacer));
            if (Instant.now().isAfter(limite)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo puedes deshacer un apunte durante los "
                        + minutosDeshacer + " primeros minutos. Pide a un administrador que lo corrija.");
            }
        }
        Producto producto = productoService.bloquear(consumo.getProducto().getId());
        productoService.moverStock(producto, consumo.getCantidad());
        consumo.setAnulado(true);
    }
}
