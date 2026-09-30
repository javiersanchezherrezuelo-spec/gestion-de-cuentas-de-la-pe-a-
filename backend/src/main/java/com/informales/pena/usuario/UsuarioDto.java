package com.informales.pena.usuario;

public record UsuarioDto(Long id, String nombre, String username, String email, Rol rol, boolean activo) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getUsername(), u.getEmail(), u.getRol(), u.isActivo());
    }
}
