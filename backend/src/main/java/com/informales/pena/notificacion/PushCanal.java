package com.informales.pena.notificacion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.informales.pena.usuario.Usuario;

import io.jsonwebtoken.Jwts;
import tools.jackson.databind.json.JsonMapper;

/** Notificaciones en el móvil (Web Push). */
@Component
public class PushCanal implements CanalNotificacion {

    private static final Logger log = LoggerFactory.getLogger(PushCanal.class);

    private final PushSuscripcionRepository suscripciones;
    private final String contacto;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final JsonMapper json = JsonMapper.builder().build();

    public PushCanal(PushSuscripcionRepository suscripciones, @Value("${app.contacto}") String contacto) {
        this.suscripciones = suscripciones;
        this.contacto = contacto;
    }

    @Override
    public boolean activo(Ajustes ajustes) {
        return ajustes.isCanalPush();
    }

    @Override
    @Transactional
    public void enviar(Aviso aviso, Ajustes ajustes) {
        var ids = aviso.destinatarios().stream().map(Usuario::getId).toList();
        var lista = suscripciones.findByUsuarioIdIn(ids);
        if (lista.isEmpty()) {
            log.info("[push] Nadie de los destinatarios tiene las notificaciones activadas: {}", aviso.titulo());
            return;
        }
        byte[] mensaje = json.writeValueAsString(Map.of(
                "titulo", aviso.titulo(),
                "texto", aviso.texto(),
                "url", aviso.url() != null ? aviso.url() : "/")).getBytes(StandardCharsets.UTF_8);
        var clavePrivada = WebPush.clavePrivada(ajustes.getVapidPrivada());

        for (PushSuscripcion s : lista) {
            try {
                String jwt = Jwts.builder()
                        .audience().single(WebPush.origen(s.getEndpoint()))
                        .expiration(Date.from(Instant.now().plus(Duration.ofHours(12))))
                        .subject(contacto)
                        .signWith(clavePrivada, Jwts.SIG.ES256)
                        .compact();
                HttpRequest peticion = HttpRequest.newBuilder(URI.create(s.getEndpoint()))
                        .timeout(Duration.ofSeconds(15))
                        .header("Content-Type", "application/octet-stream")
                        .header("Content-Encoding", "aes128gcm")
                        .header("TTL", "86400")
                        .header("Urgency", "normal")
                        .header("Authorization", "vapid t=" + jwt + ", k=" + ajustes.getVapidPublica())
                        .POST(HttpRequest.BodyPublishers.ofByteArray(WebPush.cifrar(mensaje, s.getP256dh(), s.getAuth())))
                        .build();
                HttpResponse<String> respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
                int estado = respuesta.statusCode();
                if (estado == 404 || estado == 410) {
                    // El navegador ya no usa esta suscripción (se desinstaló, se quitó el permiso…)
                    suscripciones.delete(s);
                } else if (estado >= 300) {
                    log.warn("[push] El servicio respondió {} para el usuario {}: {}", estado,
                            s.getUsuario().getId(), respuesta.body());
                }
            } catch (Exception e) {
                log.warn("[push] No se pudo enviar al usuario {}: {}", s.getUsuario().getId(), e.getMessage());
            }
        }
    }
}
