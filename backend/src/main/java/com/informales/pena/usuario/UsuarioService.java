package com.informales.pena.usuario;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UsuarioDto> listar() {
        return usuarios.findAllByOrderByNombreAsc().stream().map(UsuarioDto::de).toList();
    }

    public Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));
    }

    @Transactional
    public UsuarioDto crear(UsuarioForm form) {
        if (form.password() == null || form.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
        if (usuarios.existsByUsernameIgnoreCase(form.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese nombre de usuario");
        }
        Usuario u = new Usuario();
        aplicar(u, form);
        u.setPasswordHash(passwordEncoder.encode(form.password()));
        return UsuarioDto.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioDto editar(Long id, UsuarioForm form) {
        Usuario u = buscar(id);
        usuarios.findByUsernameIgnoreCase(form.username())
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese nombre de usuario");
                });

        boolean dejaDeSerAdminActivo = u.esAdmin() && u.isActivo()
                && (form.rol() != Rol.ADMIN || Boolean.FALSE.equals(form.activo()));
        if (dejaDeSerAdminActivo && usuarios.countByActivoTrueAndRol(Rol.ADMIN) <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tiene que quedar al menos un administrador activo");
        }

        aplicar(u, form);
        if (form.password() != null && !form.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(form.password()));
        }
        return UsuarioDto.de(u);
    }

    @Transactional
    public void cambiarPassword(Long id, CambioPasswordRequest req) {
        Usuario u = buscar(id);
        if (!passwordEncoder.matches(req.actual(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
        }
        u.setPasswordHash(passwordEncoder.encode(req.nueva()));
    }

    @Transactional
    public UsuarioDto cambiarEmail(Long id, String email) {
        Usuario u = buscar(id);
        u.setEmail(vacioANull(email));
        return UsuarioDto.de(u);
    }

    private void aplicar(Usuario u, UsuarioForm form) {
        u.setNombre(form.nombre().trim());
        u.setUsername(form.username().trim());
        u.setEmail(vacioANull(form.email()));
        u.setRol(form.rol());
        if (form.activo() != null) {
            u.setActivo(form.activo());
        }
    }

    private static String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
