package com.informales.pena.notificacion;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import com.informales.pena.cuenta.CuentaService;
import com.informales.pena.cuenta.DeudaDto;
import com.informales.pena.producto.StockBajoEvent;
import com.informales.pena.usuario.Rol;
import com.informales.pena.usuario.Usuario;
import com.informales.pena.usuario.UsuarioRepository;

/** Decide cuándo se avisa y a quién: stock bajo y resumen mensual de deudas. */
@Service
public class AvisosService {

    private static final Logger log = LoggerFactory.getLogger(AvisosService.class);

    private final NotificacionService notificaciones;
    private final UsuarioRepository usuarios;
    private final CuentaService cuentaService;

    public AvisosService(NotificacionService notificaciones, UsuarioRepository usuarios, CuentaService cuentaService) {
        this.notificaciones = notificaciones;
        this.usuarios = usuarios;
        this.cuentaService = cuentaService;
    }

    /** Cuando un producto llega a su mínimo, avisa a los admins (después de guardar el apunte, sin bloquearlo). */
    @Async
    @TransactionalEventListener
    public void alBajarStock(StockBajoEvent e) {
        String nombre = e.emoji() != null ? e.emoji() + " " + e.nombre() : e.nombre();
        String texto = e.stock() == 0
                ? "Se ha acabado. Toca reponer."
                : "Quedan " + e.stock() + " (el mínimo es " + e.stockMinimo() + "). Toca reponer.";
        notificaciones.enviar(new Aviso(admins(), "Quedan pocas: " + nombre, texto, "/admin/productos"));
    }

    /** El día 1 de cada mes a las 10:00 (hora de Madrid) manda el resumen de deudas. */
    @Scheduled(cron = "0 0 10 1 * *", zone = "Europe/Madrid")
    public void resumenMensual() {
        log.info("Enviando el resumen mensual de deudas");
        enviarResumen();
    }

    /** Cada miembro recibe lo que debe; los admins, además, la lista completa. */
    @Async
    public void enviarResumenAsync() {
        enviarResumen();
    }

    public void enviarResumen() {
        List<DeudaDto> deudas = cuentaService.deudas();
        Map<Long, Usuario> porId = usuarios.findByActivoTrueOrderByNombreAsc().stream()
                .collect(Collectors.toMap(Usuario::getId, Function.identity()));

        for (DeudaDto d : deudas) {
            Usuario u = porId.get(d.usuarioId());
            if (u != null && d.deuda().signum() > 0) {
                notificaciones.enviar(new Aviso(List.of(u), "Tu cuenta del mes",
                        "Debes " + euros(d.deuda()) + ". Págaselo a un administrador cuando puedas. ¡Gracias!", "/cuenta"));
            }
        }

        List<String> lineas = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (DeudaDto d : deudas) {
            if (d.deuda().signum() > 0) {
                lineas.add("• " + d.nombre() + ": " + euros(d.deuda()));
                total = total.add(d.deuda());
            }
        }
        String texto = lineas.isEmpty()
                ? "¡Nadie debe nada! 🎉"
                : String.join("\n", lineas) + "\nTotal: " + euros(total);
        notificaciones.enviar(new Aviso(admins(), "Resumen de deudas", texto, "/resumen"));
    }

    /** Aviso de prueba para comprobar que los canales funcionan. */
    @Async
    public void enviarPrueba(Usuario destinatario) {
        notificaciones.enviar(new Aviso(List.of(destinatario), "Prueba de avisos",
                "Si lees esto, los avisos funcionan 🍻", "/"));
    }

    private List<Usuario> admins() {
        return usuarios.findByActivoTrueAndRol(Rol.ADMIN);
    }

    static String euros(BigDecimal importe) {
        return NumberFormat.getCurrencyInstance(Locale.of("es", "ES")).format(importe);
    }
}
