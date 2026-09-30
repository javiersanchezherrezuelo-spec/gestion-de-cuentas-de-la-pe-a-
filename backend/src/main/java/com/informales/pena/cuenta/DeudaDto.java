package com.informales.pena.cuenta;

import java.math.BigDecimal;

/** Lo que debe un usuario. Si es negativo, tiene saldo a favor. */
public record DeudaDto(Long usuarioId, String nombre, BigDecimal deuda) {
}
