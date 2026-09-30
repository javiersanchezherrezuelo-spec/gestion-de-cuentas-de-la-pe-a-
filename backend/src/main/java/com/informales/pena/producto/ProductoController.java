package com.informales.pena.producto;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /** Productos que se pueden apuntar (para todos los miembros). */
    @GetMapping("/productos")
    public List<ProductoDto> activos() {
        return productoService.listarActivos();
    }

    @GetMapping("/admin/productos")
    public List<ProductoDto> todos() {
        return productoService.listarTodos();
    }

    @PostMapping("/admin/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoDto crear(@Valid @RequestBody ProductoForm form) {
        return productoService.crear(form);
    }

    @PutMapping("/admin/productos/{id}")
    public ProductoDto editar(@PathVariable Long id, @Valid @RequestBody ProductoForm form) {
        return productoService.editar(id, form);
    }
}
