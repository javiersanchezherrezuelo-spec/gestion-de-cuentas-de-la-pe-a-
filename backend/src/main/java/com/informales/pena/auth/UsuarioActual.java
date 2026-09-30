package com.informales.pena.auth;

import com.informales.pena.usuario.Rol;
import com.informales.pena.usuario.Usuario;

/** El usuario que ha iniciado sesión. Los controladores lo reciben con @AuthenticationPrincipal. */
public record UsuarioActual(Long id, String username, String nombre, Rol rol) {

    public static UsuarioActual de(Usuario u) {
        return new UsuarioActual(u.getId(), u.getUsername(), u.getNombre(), u.getRol());
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }
}
