package com.informales.pena.comun;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Devuelve los errores siempre como {"error": "mensaje"} para que la web pueda mostrarlos. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> errorConEstado(ResponseStatusException e) {
        String mensaje = e.getReason() != null ? e.getReason() : "Ha ocurrido un error";
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", mensaje));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> datosNoValidos(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Los datos no son válidos");
        return ResponseEntity.badRequest().body(Map.of("error", mensaje));
    }
}
