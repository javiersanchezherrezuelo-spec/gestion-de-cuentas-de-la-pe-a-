package com.informales.pena.producto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Datos de un producto. El stock solo se usa al crearlo; después se cambia reponiendo. */
public record ProductoForm(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 60, message = "El nombre es demasiado largo") String nombre,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        @Digits(integer = 6, fraction = 2, message = "El precio puede tener como mucho 2 decimales") BigDecimal precio,
        @PositiveOrZero(message = "El stock no puede ser negativo") Integer stock,
        @NotNull(message = "El stock mínimo es obligatorio")
        @PositiveOrZero(message = "El stock mínimo no puede ser negativo") Integer stockMinimo,
        @Size(max = 16, message = "El icono es demasiado largo") String emoji,
        Boolean activo) {
}
