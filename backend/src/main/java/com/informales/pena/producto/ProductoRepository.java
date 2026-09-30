package com.informales.pena.producto;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Producto> findByNombreIgnoreCase(String nombre);

    /** Bloquea la fila hasta el final de la transacción para que dos apuntes a la vez no descuadren el stock. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdParaActualizar(Long id);
}
