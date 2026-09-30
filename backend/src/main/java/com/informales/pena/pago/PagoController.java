package com.informales.pena.pago;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.informales.pena.auth.UsuarioActual;
import com.informales.pena.usuario.UsuarioRepository;
import com.informales.pena.usuario.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/admin/pagos")
public class PagoController {

    public record PagoRequest(
            @NotNull(message = "Elige quién paga") Long usuarioId,
            @NotNull(message = "Indica el importe")
            @DecimalMin(value = "0.01", message = "El importe tiene que ser mayor que 0")
            @Digits(integer = 8, fraction = 2, message = "El importe puede tener como mucho 2 decimales") BigDecimal importe,
            @Size(max = 200, message = "La nota es demasiado larga") String nota) {
    }

    private final PagoRepository pagos;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarios;

    public PagoController(PagoRepository pagos, UsuarioService usuarioService, UsuarioRepository usuarios) {
        this.pagos = pagos;
        this.usuarioService = usuarioService;
        this.usuarios = usuarios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public PagoDto registrar(@Valid @RequestBody PagoRequest req, @AuthenticationPrincipal UsuarioActual actual) {
        String nota = req.nota() == null || req.nota().isBlank() ? null : req.nota().trim();
        Pago pago = new Pago(usuarioService.buscar(req.usuarioId()), req.importe(),
                usuarios.getReferenceById(actual.id()), nota);
        return PagoDto.de(pagos.save(pago));
    }

    /** Para corregir un pago apuntado por error. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void borrar(@PathVariable Long id) {
        if (!pagos.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El pago no existe");
        }
        pagos.deleteById(id);
    }
}
