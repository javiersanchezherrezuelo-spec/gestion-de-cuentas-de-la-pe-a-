package com.informales.pena.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.informales.pena.usuario.Usuario;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration validez;

    public JwtService(@Value("${app.jwt.secret}") String secreto, @Value("${app.jwt.horas-validez}") long horas) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.validez = Duration.ofHours(horas);
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(validez)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el id del usuario si el token es válido y no ha caducado. */
    public Optional<Long> leerUsuarioId(String token) {
        try {
            String sujeto = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload().getSubject();
            return Optional.of(Long.valueOf(sujeto));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
