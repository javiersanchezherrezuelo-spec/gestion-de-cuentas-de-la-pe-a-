package com.informales.pena.consumo;

import java.math.BigDecimal;
import java.time.Instant;

/** Un apunte. "deshacibleHasta" indica hasta cuándo puede anularlo el propio miembro. */
public record ConsumoDto(Long id, Long productoId, String producto, String emoji, int cantidad,
                         BigDecimal precioUnitario, BigDecimal total, Instant fecha, boolean anulado,
                         Instant deshacibleHasta) {

    public static ConsumoDto de(Consumo c, long minutosDeshacer) {
        return new ConsumoDto(c.getId(), c.getProducto().getId(), c.getProducto().getNombre(),
                c.getProducto().getEmoji(), c.getCantidad(), c.getPrecioUnitario(), c.total(), c.getFecha(),
                c.isAnulado(), c.getFecha().plusSeconds(minutosDeshacer * 60));
    }
}
