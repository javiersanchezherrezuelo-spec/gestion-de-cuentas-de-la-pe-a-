package com.informales.pena.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.informales.pena.usuario.UsuarioDto;
import com.informales.pena.usuario.UsuarioRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(@NotBlank(message = "Escribe tu usuario") String username,
                               @NotBlank(message = "Escribe tu contraseña") String password) {
    }

    public record LoginResponse(String token, UsuarioDto usuario) {
    }

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return usuarios.findByUsernameIgnoreCase(req.username().trim())
                .filter(u -> u.isActivo() && passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .map(u -> new LoginResponse(jwtService.generar(u), UsuarioDto.de(u)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
    }
}
