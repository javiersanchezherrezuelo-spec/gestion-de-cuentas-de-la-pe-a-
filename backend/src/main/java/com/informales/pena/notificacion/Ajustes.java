package com.informales.pena.notificacion;

import jakarta.persistence.*;

/** Configuración de avisos. Solo hay una fila (id = 1). */
@Entity
@Table(name = "ajustes")
public class Ajustes {

    public static final long ID = 1L;

    @Id
    private Long id = ID;

    @Column(nullable = false)
    private boolean canalPush = true;

    @Column(nullable = false)
    private boolean canalEmail = false;

    @Column(nullable = false)
    private boolean canalWhatsapp = true;

    /** Claves VAPID para las notificaciones push. Se generan solas la primera vez. */
    @Column(length = 100)
    private String vapidPublica;

    @Column(length = 100)
    private String vapidPrivada;

    public Long getId() { return id; }
    public boolean isCanalPush() { return canalPush; }
    public void setCanalPush(boolean canalPush) { this.canalPush = canalPush; }
    public boolean isCanalEmail() { return canalEmail; }
    public void setCanalEmail(boolean canalEmail) { this.canalEmail = canalEmail; }
    public boolean isCanalWhatsapp() { return canalWhatsapp; }
    public void setCanalWhatsapp(boolean canalWhatsapp) { this.canalWhatsapp = canalWhatsapp; }
    public String getVapidPublica() { return vapidPublica; }
    public void setVapidPublica(String vapidPublica) { this.vapidPublica = vapidPublica; }
    public String getVapidPrivada() { return vapidPrivada; }
    public void setVapidPrivada(String vapidPrivada) { this.vapidPrivada = vapidPrivada; }
}
