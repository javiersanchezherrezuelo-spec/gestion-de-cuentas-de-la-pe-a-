package com.informales.pena.usuario;

import jakarta.validation.constraints.Email;

public record PerfilRequest(@Email(message = "El email no es válido") String email) {
}
