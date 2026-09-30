package com.informales.pena.notificacion;

import java.time.Instant;

import com.informales.pena.usuario.Usuario;

import jakarta.persistence.*;

/** Un navegador/móvil que ha aceptado recibir notificaciones. */
@Entity
@Table(name = "push_suscripcion")
public class PushSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 700)
    private String endpoint;

    @Column(nullable = false, length = 150)
    private String p256dh;

    @Column(nullable = false, length = 50)
    private String auth;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getP256dh() { return p256dh; }
    public void setP256dh(String p256dh) { this.p256dh = p256dh; }
    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }
    public Instant getFecha() { return fecha; }
}
