package com.informales.pena.notificacion;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.informales.pena.auth.UsuarioActual;
import com.informales.pena.usuario.UsuarioRepository;
import com.informales.pena.usuario.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api")
public class AvisosController {

    public record AjustesDto(boolean canalPush, boolean canalEmail, boolean canalWhatsapp, boolean emailConfigurado) {
    }

    public record CanalesRequest(boolean canalPush, boolean canalEmail, boolean canalWhatsapp) {
    }

    public record SuscripcionRequest(
            @NotBlank @Size(max = 700) String endpoint,
            @NotNull @Valid Claves keys) {

        public record Claves(@NotBlank @Size(max = 150) String p256dh, @NotBlank @Size(max = 50) String auth) {
        }
    }

    public record BajaRequest(@NotBlank String endpoint) {
    }

    private final AjustesService ajustesService;
    private final AvisosService avisosService;
    private final EmailCanal emailCanal;
    private final PushSuscripcionRepository suscripciones;
    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;

    public AvisosController(AjustesService ajustesService, AvisosService avisosService, EmailCanal emailCanal,
                            PushSuscripcionRepository suscripciones, UsuarioRepository usuarios,
                            UsuarioService usuarioService) {
        this.ajustesService = ajustesService;
        this.avisosService = avisosService;
        this.emailCanal = emailCanal;
        this.suscripciones = suscripciones;
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
    }

    // --- Suscripción a notificaciones (cualquier miembro) ---

    @GetMapping("/push/clave-publica")
    public String clavePublica() {
        return ajustesService.obtener().getVapidPublica();
    }

    @PostMapping("/push/suscripciones")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void suscribir(@AuthenticationPrincipal UsuarioActual actual, @Valid @RequestBody SuscripcionRequest req) {
        PushSuscripcion s = suscripciones.findByEndpoint(req.endpoint()).orElseGet(PushSuscripcion::new);
        s.setUsuario(usuarios.getReferenceById(actual.id()));
        s.setEndpoint(req.endpoint());
        s.setP256dh(req.keys().p256dh());
        s.setAuth(req.keys().auth());
        suscripciones.save(s);
    }

    @PostMapping("/push/baja")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void darDeBaja(@Valid @RequestBody BajaRequest req) {
        suscripciones.deleteByEndpoint(req.endpoint());
    }

    // --- Ajustes de avisos (admin) ---

    @GetMapping("/admin/ajustes")
    public AjustesDto ajustes() {
        return aDto(ajustesService.obtener());
    }

    @PutMapping("/admin/ajustes")
    public AjustesDto guardar(@RequestBody CanalesRequest req) {
        return aDto(ajustesService.guardarCanales(req.canalPush(), req.canalEmail(), req.canalWhatsapp()));
    }

    @PostMapping("/admin/avisos/prueba")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void prueba(@AuthenticationPrincipal UsuarioActual actual) {
        avisosService.enviarPrueba(usuarioService.buscar(actual.id()));
    }

    @PostMapping("/admin/avisos/resumen")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resumenAhora() {
        avisosService.enviarResumenAsync();
    }

    private AjustesDto aDto(Ajustes a) {
        return new AjustesDto(a.isCanalPush(), a.isCanalEmail(), a.isCanalWhatsapp(), emailCanal.configurado());
    }
}
