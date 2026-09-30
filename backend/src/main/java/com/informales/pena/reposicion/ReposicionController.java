package com.informales.pena.reposicion;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.informales.pena.auth.UsuarioActual;
import com.informales.pena.producto.Producto;
import com.informales.pena.producto.ProductoDto;
import com.informales.pena.producto.ProductoService;
import com.informales.pena.usuario.UsuarioRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/admin")
public class ReposicionController {

    public record ReponerRequest(@NotNull(message = "Indica cuántas unidades") Integer cantidad) {
    }

    public record ReposicionDto(Long id, String producto, int cantidad, Instant fecha, String admin) {
        static ReposicionDto de(Reposicion r) {
            return new ReposicionDto(r.getId(), r.getProducto().getNombre(), r.getCantidad(), r.getFecha(),
                    r.getAdmin().getNombre());
        }
    }

    private final ProductoService productoService;
    private final ReposicionRepository reposiciones;
    private final UsuarioRepository usuarios;

    public ReposicionController(ProductoService productoService, ReposicionRepository reposiciones,
                                UsuarioRepository usuarios) {
        this.productoService = productoService;
        this.reposiciones = reposiciones;
        this.usuarios = usuarios;
    }

    /** Suma unidades al stock. Con un número negativo sirve para corregir un recuento. */
    @PostMapping("/productos/{id}/reponer")
    @Transactional
    public ProductoDto reponer(@PathVariable Long id, @Valid @RequestBody ReponerRequest req,
                               @AuthenticationPrincipal UsuarioActual actual) {
        if (req.cantidad() == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad no puede ser 0");
        }
        Producto p = productoService.bloquear(id);
        productoService.moverStock(p, req.cantidad());
        reposiciones.save(new Reposicion(p, req.cantidad(), usuarios.getReferenceById(actual.id())));
        return ProductoDto.de(p);
    }

    @GetMapping("/reposiciones")
    @Transactional(readOnly = true)
    public List<ReposicionDto> ultimas() {
        return reposiciones.findTop50ByOrderByFechaDesc().stream().map(ReposicionDto::de).toList();
    }
}
