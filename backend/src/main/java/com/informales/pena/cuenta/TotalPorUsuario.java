package com.informales.pena.cuenta;

import java.math.BigDecimal;

public record TotalPorUsuario(Long usuarioId, BigDecimal total) {
}
