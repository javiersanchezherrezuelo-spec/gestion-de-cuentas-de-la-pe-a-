package com.informales.pena.notificacion;

/** Una forma de hacer llegar avisos (notificaciones push, email…). */
public interface CanalNotificacion {

    /** true si este canal está activado en los ajustes. */
    boolean activo(Ajustes ajustes);

    /** Envía el aviso. No debe lanzar excepciones: si algo falla, lo registra en el log. */
    void enviar(Aviso aviso, Ajustes ajustes);
}
