package com.informales.pena.producto;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductoService {

    private final ProductoRepository productos;
    private final ApplicationEventPublisher eventos;

    public ProductoService(ProductoRepository productos, ApplicationEventPublisher eventos) {
        this.productos = productos;
        this.eventos = eventos;
    }

    public List<ProductoDto> listarActivos() {
        return productos.findByActivoTrueOrderByNombreAsc().stream().map(ProductoDto::de).toList();
    }

    public List<ProductoDto> listarTodos() {
        return productos.findAllByOrderByNombreAsc().stream().map(ProductoDto::de).toList();
    }

    @Transactional
    public ProductoDto crear(ProductoForm form) {
        if (productos.existsByNombreIgnoreCase(form.nombre().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese nombre");
        }
        Producto p = new Producto();
        aplicar(p, form);
        p.setStock(form.stock() != null ? form.stock() : 0);
        return ProductoDto.de(productos.save(p));
    }

    @Transactional
    public ProductoDto editar(Long id, ProductoForm form) {
        Producto p = productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe"));
        productos.findByNombreIgnoreCase(form.nombre().trim())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese nombre");
                });
        aplicar(p, form);
        return ProductoDto.de(p);
    }

    /** Busca el producto bloqueándolo para modificar su stock. Hay que llamarlo dentro de una transacción. */
    public Producto bloquear(Long id) {
        return productos.findByIdParaActualizar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El producto no existe"));
    }

    /**
     * Suma (o resta, si es negativo) unidades al stock. Si el stock baja hasta el mínimo en este cambio,
     * publica un StockBajoEvent. Solo avisa al cruzar el mínimo, no en cada apunte posterior.
     */
    public void moverStock(Producto p, int cantidad) {
        int antes = p.getStock();
        int despues = antes + cantidad;
        if (despues < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No hay suficiente " + p.getNombre() + " (quedan " + antes + ")");
        }
        p.setStock(despues);
        if (antes > p.getStockMinimo() && despues <= p.getStockMinimo()) {
            eventos.publishEvent(new StockBajoEvent(p.getId(), p.getNombre(), p.getEmoji(), despues, p.getStockMinimo()));
        }
    }

    private void aplicar(Producto p, ProductoForm form) {
        p.setNombre(form.nombre().trim());
        p.setPrecio(form.precio());
        p.setStockMinimo(form.stockMinimo());
        p.setEmoji(form.emoji() == null || form.emoji().isBlank() ? null : form.emoji().trim());
        if (form.activo() != null) {
            p.setActivo(form.activo());
        }
    }
}
