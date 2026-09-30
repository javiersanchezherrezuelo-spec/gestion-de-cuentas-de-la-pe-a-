package com.informales.pena.pago;

import java.math.BigDecimal;
import java.time.Instant;

public record PagoDto(Long id, BigDecimal importe, Instant fecha, String registradoPor, String nota) {

    public static PagoDto de(Pago p) {
        return new PagoDto(p.getId(), p.getImporte(), p.getFecha(), p.getRegistradoPor().getNombre(), p.getNota());
    }
}
