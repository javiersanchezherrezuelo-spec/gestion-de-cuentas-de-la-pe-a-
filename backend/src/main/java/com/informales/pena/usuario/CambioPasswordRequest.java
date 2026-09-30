package com.informales.pena.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(
        @NotBlank(message = "Escribe tu contraseña actual") String actual,
        @NotBlank(message = "Escribe la contraseña nueva")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String nueva) {
}
