package com.informales.pena.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.informales.pena.usuario.Rol;
import com.informales.pena.usuario.Usuario;
import com.informales.pena.usuario.UsuarioRepository;

/** La primera vez que arranca la aplicación (sin usuarios) crea el administrador inicial. */
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminInicial(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                        @Value("${app.admin.username}") String username,
                        @Value("${app.admin.password}") String password) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRol(Rol.ADMIN);
        usuarios.save(admin);
        log.warn("Creado el administrador inicial '{}'. Cambia su contraseña al entrar.", username);
    }
}
