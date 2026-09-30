package com.informales.pena.consumo;

import java.math.BigDecimal;
import java.time.Instant;

import com.informales.pena.producto.Producto;
import com.informales.pena.usuario.Usuario;

import jakarta.persistence.*;

@Entity
@Table(name = "consumo", indexes = @Index(columnList = "usuario_id, fecha"))
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Producto producto;

    @Column(nullable = false)
    private int cantidad;

    /** Precio del producto en el momento del apunte: si luego sube, las deudas antiguas no cambian. */
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false)
    private Instant fecha;

    @Column(nullable = false)
    private boolean anulado = false;

    protected Consumo() {
    }

    public Consumo(Usuario usuario, Producto producto, int cantidad) {
        this.usuario = usuario;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
        this.fecha = Instant.now();
    }

    public BigDecimal total() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public Instant getFecha() { return fecha; }
    public boolean isAnulado() { return anulado; }
    public void setAnulado(boolean anulado) { this.anulado = anulado; }
}
