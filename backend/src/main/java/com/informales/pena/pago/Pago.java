package com.informales.pena.pago;

import java.math.BigDecimal;
import java.time.Instant;

import com.informales.pena.usuario.Usuario;

import jakarta.persistence.*;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario usuario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importe;

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario registradoPor;

    @Column(length = 200)
    private String nota;

    protected Pago() {
    }

    public Pago(Usuario usuario, BigDecimal importe, Usuario registradoPor, String nota) {
        this.usuario = usuario;
        this.importe = importe;
        this.registradoPor = registradoPor;
        this.nota = nota;
        this.fecha = Instant.now();
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public BigDecimal getImporte() { return importe; }
    public Instant getFecha() { return fecha; }
    public Usuario getRegistradoPor() { return registradoPor; }
    public String getNota() { return nota; }
}
