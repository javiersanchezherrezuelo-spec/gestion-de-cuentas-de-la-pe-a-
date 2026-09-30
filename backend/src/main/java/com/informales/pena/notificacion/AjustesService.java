package com.informales.pena.notificacion;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AjustesService {

    private final AjustesRepository repositorio;

    public AjustesService(AjustesRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Devuelve los ajustes, creándolos (con claves VAPID nuevas) si todavía no existen. */
    @Transactional
    public Ajustes obtener() {
        Ajustes ajustes = repositorio.findById(Ajustes.ID).orElseGet(() -> repositorio.save(new Ajustes()));
        if (ajustes.getVapidPublica() == null) {
            String[] claves = WebPush.generarClavesVapid();
            ajustes.setVapidPublica(claves[0]);
            ajustes.setVapidPrivada(claves[1]);
        }
        return ajustes;
    }

    @Transactional
    public Ajustes guardarCanales(boolean push, boolean email, boolean whatsapp) {
        Ajustes ajustes = obtener();
        ajustes.setCanalPush(push);
        ajustes.setCanalEmail(email);
        ajustes.setCanalWhatsapp(whatsapp);
        return ajustes;
    }
}
