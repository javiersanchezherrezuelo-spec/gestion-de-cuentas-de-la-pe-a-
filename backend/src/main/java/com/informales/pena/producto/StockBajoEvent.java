package com.informales.pena.producto;

/** Se publica cuando el stock de un producto baja hasta su mínimo (o por debajo). */
public record StockBajoEvent(Long productoId, String nombre, String emoji, int stock, int stockMinimo) {
}
