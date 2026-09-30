package com.informales.pena.resumen;

import java.math.BigDecimal;
import java.util.List;

import com.informales.pena.cuenta.DeudaDto;
import com.informales.pena.producto.ProductoDto;

/** @param whatsapp si el admin ha activado el botón de compartir por WhatsApp */
public record ResumenDto(List<DeudaDto> deudas, BigDecimal totalDeuda, List<ProductoDto> productos, boolean whatsapp) {
}
