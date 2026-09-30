package com.informales.pena.notificacion;

import java.util.List;

import com.informales.pena.usuario.Usuario;

/**
 * Un aviso para uno o varios usuarios.
 *
 * @param url pestaña de la web que se abre al tocar la notificación (por ejemplo "/resumen")
 */
public record Aviso(List<Usuario> destinatarios, String titulo, String texto, String url) {
}
