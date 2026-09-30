package com.informales.pena.resumen;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.informales.pena.cuenta.CuentaService;
import com.informales.pena.cuenta.DeudaDto;
import com.informales.pena.producto.ProductoService;

/** Pestaña "Resumen": deudas de todos y stock, visible para cualquier miembro. */
@RestController
public class ResumenController {

    private final CuentaService cuentaService;
    private final ProductoService productoService;

    public ResumenController(CuentaService cuentaService, ProductoService productoService) {
        this.cuentaService = cuentaService;
        this.productoService = productoService;
    }

    @GetMapping("/api/resumen")
    public ResumenDto resumen() {
        List<DeudaDto> deudas = cuentaService.deudas();
        BigDecimal total = deudas.stream().map(DeudaDto::deuda)
                .filter(d -> d.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenDto(deudas, total, productoService.listarActivos());
    }
}
