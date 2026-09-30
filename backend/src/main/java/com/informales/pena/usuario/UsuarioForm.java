package com.informales.pena.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos que manda el admin al crear o editar un usuario. La contraseña es opcional al editar. */
public record UsuarioForm(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 60, message = "El nombre es demasiado largo") String nombre,
        @NotBlank(message = "El usuario es obligatorio")
        @Pattern(regexp = "[a-zA-Z0-9._-]{3,40}", message = "El usuario debe tener entre 3 y 40 letras, números, puntos o guiones")
        String username,
        @Email(message = "El email no es válido") String email,
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password,
        @NotNull(message = "El rol es obligatorio") Rol rol,
        Boolean activo) {
}
