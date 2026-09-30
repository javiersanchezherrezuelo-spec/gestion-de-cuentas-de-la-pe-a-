package com.informales.pena.consumo;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.informales.pena.cuenta.TotalPorUsuario;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    @EntityGraph(attributePaths = "producto")
    List<Consumo> findTop100ByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /** Total consumido (sin anulados) por un usuario. Devuelve null si no tiene consumos. */
    @Query("select sum(c.precioUnitario * c.cantidad) from Consumo c where c.usuario.id = :usuarioId and c.anulado = false")
    BigDecimal totalDe(Long usuarioId);

    @Query("""
            select new com.informales.pena.cuenta.TotalPorUsuario(c.usuario.id, sum(c.precioUnitario * c.cantidad))
            from Consumo c where c.anulado = false group by c.usuario.id""")
    List<TotalPorUsuario> totalesPorUsuario();
}
