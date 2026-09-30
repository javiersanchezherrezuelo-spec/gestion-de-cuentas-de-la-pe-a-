package com.informales.pena.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.informales.pena.usuario.Usuario;

/** Avisos por email (SMTP). Si no hay servidor de correo configurado, escribe el correo en el log. */
@Component
public class EmailCanal implements CanalNotificacion {

    private static final Logger log = LoggerFactory.getLogger(EmailCanal.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String host;
    private final String remitente;

    public EmailCanal(ObjectProvider<JavaMailSender> mailSender,
                      @Value("${spring.mail.host:}") String host,
                      @Value("${app.mail.from:}") String remitente) {
        this.mailSender = mailSender;
        this.host = host;
        this.remitente = remitente;
    }

    public boolean configurado() {
        return !host.isBlank() && mailSender.getIfAvailable() != null;
    }

    @Override
    public boolean activo(Ajustes ajustes) {
        return ajustes.isCanalEmail();
    }

    @Override
    public void enviar(Aviso aviso, Ajustes ajustes) {
        for (Usuario u : aviso.destinatarios()) {
            if (u.getEmail() == null) {
                continue;
            }
            if (!configurado()) {
                log.info("[email sin configurar] Para {} <{}> | {} | {}", u.getNombre(), u.getEmail(), aviso.titulo(),
                        aviso.texto());
                continue;
            }
            try {
                SimpleMailMessage correo = new SimpleMailMessage();
                if (!remitente.isBlank()) {
                    correo.setFrom(remitente);
                }
                correo.setTo(u.getEmail());
                correo.setSubject("[Los Informales] " + aviso.titulo());
                correo.setText("Hola " + u.getNombre() + ",\n\n" + aviso.texto() + "\n\n— Peña Los Informales");
                mailSender.getObject().send(correo);
            } catch (Exception e) {
                log.warn("[email] No se pudo enviar a {}: {}", u.getEmail(), e.getMessage());
            }
        }
    }
}
