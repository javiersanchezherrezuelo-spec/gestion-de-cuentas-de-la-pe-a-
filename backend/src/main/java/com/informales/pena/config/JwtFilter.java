package com.informales.pena.config;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.informales.pena.auth.JwtService;
import com.informales.pena.auth.UsuarioActual;
import com.informales.pena.usuario.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lee la cabecera "Authorization: Bearer <token>" y, si es válida, deja al usuario autenticado.
 * Se carga el usuario de la base de datos en cada petición para que un usuario desactivado
 * o un cambio de rol se apliquen al momento.
 */
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            jwtService.leerUsuarioId(cabecera.substring(7))
                    .flatMap(usuarios::findById)
                    .filter(u -> u.isActivo())
                    .ifPresent(u -> {
                        var principal = UsuarioActual.de(u);
                        var permisos = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name()));
                        var auth = new UsernamePasswordAuthenticationToken(principal, null, permisos);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(request, response);
    }
}
