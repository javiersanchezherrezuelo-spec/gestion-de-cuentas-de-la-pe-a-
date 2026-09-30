package com.informales.pena.consumo;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.informales.pena.auth.UsuarioActual;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consumos")
public class ConsumoController {

    private final ConsumoService consumoService;

    public ConsumoController(ConsumoService consumoService) {
        this.consumoService = consumoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsumoDto apuntar(@AuthenticationPrincipal UsuarioActual actual, @Valid @RequestBody ConsumoRequest req) {
        return consumoService.registrar(actual, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deshacer(@AuthenticationPrincipal UsuarioActual actual, @PathVariable Long id) {
        consumoService.anular(actual, id);
    }
}
