package com.informales.pena.consumo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Apunte de un consumo. "usuarioId" solo lo usan los admins para apuntar a otra persona. */
public record ConsumoRequest(
        @NotNull(message = "Elige un producto") Long productoId,
        @NotNull(message = "Indica la cantidad")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 50, message = "La cantidad máxima es 50") Integer cantidad,
        Long usuarioId) {
}
