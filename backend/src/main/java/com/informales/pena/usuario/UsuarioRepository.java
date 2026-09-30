package com.informales.pena.usuario;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<Usuario> findAllByOrderByNombreAsc();

    List<Usuario> findByActivoTrueOrderByNombreAsc();

    List<Usuario> findByActivoTrueAndRol(Rol rol);

    long countByActivoTrueAndRol(Rol rol);
}
