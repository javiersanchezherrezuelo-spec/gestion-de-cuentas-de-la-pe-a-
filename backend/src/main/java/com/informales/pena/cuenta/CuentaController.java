package com.informales.pena.cuenta;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.informales.pena.auth.UsuarioActual;

@RestController
@RequestMapping("/api")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/yo/cuenta")
    public CuentaDto miCuenta(@AuthenticationPrincipal UsuarioActual actual) {
        return cuentaService.cuentaDe(actual.id());
    }

    @GetMapping("/admin/usuarios/{id}/cuenta")
    public CuentaDto cuentaDe(@PathVariable Long id) {
        return cuentaService.cuentaDe(id);
    }
}
