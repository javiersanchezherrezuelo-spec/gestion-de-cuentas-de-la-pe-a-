package com.informales.pena.usuario;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.informales.pena.auth.UsuarioActual;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // --- El propio usuario ---

    @GetMapping("/yo")
    public UsuarioDto yo(@AuthenticationPrincipal UsuarioActual actual) {
        return UsuarioDto.de(usuarioService.buscar(actual.id()));
    }

    @PutMapping("/yo/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@AuthenticationPrincipal UsuarioActual actual, @Valid @RequestBody CambioPasswordRequest req) {
        usuarioService.cambiarPassword(actual.id(), req);
    }

    @PutMapping("/yo/email")
    public UsuarioDto cambiarEmail(@AuthenticationPrincipal UsuarioActual actual, @Valid @RequestBody PerfilRequest req) {
        return usuarioService.cambiarEmail(actual.id(), req.email());
    }

    // --- Administración ---

    @GetMapping("/admin/usuarios")
    public List<UsuarioDto> listar() {
        return usuarioService.listar();
    }

    @PostMapping("/admin/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody UsuarioForm form) {
        return usuarioService.crear(form);
    }

    @PutMapping("/admin/usuarios/{id}")
    public UsuarioDto editar(@PathVariable Long id, @Valid @RequestBody UsuarioForm form) {
        return usuarioService.editar(id, form);
    }
}
