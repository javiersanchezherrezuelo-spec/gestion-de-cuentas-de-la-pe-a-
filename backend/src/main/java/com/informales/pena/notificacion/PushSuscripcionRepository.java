package com.informales.pena.notificacion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSuscripcionRepository extends JpaRepository<PushSuscripcion, Long> {

    Optional<PushSuscripcion> findByEndpoint(String endpoint);

    List<PushSuscripcion> findByUsuarioIdIn(Collection<Long> usuarioIds);

    void deleteByEndpoint(String endpoint);
}
