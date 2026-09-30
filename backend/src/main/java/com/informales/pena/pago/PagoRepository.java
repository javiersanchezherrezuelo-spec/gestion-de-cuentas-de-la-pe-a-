package com.informales.pena.pago;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.informales.pena.cuenta.TotalPorUsuario;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @EntityGraph(attributePaths = "registradoPor")
    List<Pago> findTop50ByUsuarioIdOrderByFechaDesc(Long usuarioId);

    /** Total pagado por un usuario. Devuelve null si no ha pagado nunca. */
    @Query("select sum(p.importe) from Pago p where p.usuario.id = :usuarioId")
    BigDecimal totalDe(Long usuarioId);

    @Query("""
            select new com.informales.pena.cuenta.TotalPorUsuario(p.usuario.id, sum(p.importe))
            from Pago p group by p.usuario.id""")
    List<TotalPorUsuario> totalesPorUsuario();
}
