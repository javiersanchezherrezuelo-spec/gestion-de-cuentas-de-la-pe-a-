package com.informales.pena.notificacion;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Reparte cada aviso entre los canales que el admin tenga activados. */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final List<CanalNotificacion> canales;
    private final AjustesService ajustesService;

    public NotificacionService(List<CanalNotificacion> canales, AjustesService ajustesService) {
        this.canales = canales;
        this.ajustesService = ajustesService;
    }

    public void enviar(Aviso aviso) {
        if (aviso.destinatarios().isEmpty()) {
            return;
        }
        Ajustes ajustes = ajustesService.obtener();
        log.info("Aviso '{}' para {} persona(s)", aviso.titulo(), aviso.destinatarios().size());
        for (CanalNotificacion canal : canales) {
            if (canal.activo(ajustes)) {
                canal.enviar(aviso, ajustes);
            }
        }
    }
}
