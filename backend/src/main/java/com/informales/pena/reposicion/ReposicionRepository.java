package com.informales.pena.reposicion;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReposicionRepository extends JpaRepository<Reposicion, Long> {

    @EntityGraph(attributePaths = {"producto", "admin"})
    List<Reposicion> findTop50ByOrderByFechaDesc();
}
