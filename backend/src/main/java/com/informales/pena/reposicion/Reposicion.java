package com.informales.pena.reposicion;

import java.time.Instant;

import com.informales.pena.producto.Producto;
import com.informales.pena.usuario.Usuario;

import jakarta.persistence.*;

/** Historial de entradas (o correcciones) de stock hechas por un admin. */
@Entity
@Table(name = "reposicion")
public class Reposicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Producto producto;

    /** Positiva al reponer, negativa al corregir un recuento. */
    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private Instant fecha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario admin;

    protected Reposicion() {
    }

    public Reposicion(Producto producto, int cantidad, Usuario admin) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.admin = admin;
        this.fecha = Instant.now();
    }

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public Instant getFecha() { return fecha; }
    public Usuario getAdmin() { return admin; }
}
