package com.informales.pena.producto;

import java.math.BigDecimal;

public record ProductoDto(Long id, String nombre, BigDecimal precio, int stock, int stockMinimo,
                          String emoji, boolean activo, boolean stockBajo) {

    public static ProductoDto de(Producto p) {
        return new ProductoDto(p.getId(), p.getNombre(), p.getPrecio(), p.getStock(), p.getStockMinimo(),
                p.getEmoji(), p.isActivo(), p.stockBajo());
    }
}
