package com.informales.pena;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.informales.pena.usuario.Rol;
import com.informales.pena.usuario.UsuarioForm;
import com.informales.pena.usuario.UsuarioRepository;
import com.informales.pena.usuario.UsuarioService;
import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class SeguridadTest {

    @Autowired MockMvc mvc;
    @Autowired UsuarioService usuarioService;
    @Autowired UsuarioRepository usuarios;

    @BeforeEach
    void crearMiembro() {
        if (!usuarios.existsByUsernameIgnoreCase("socio")) {
            usuarioService.crear(new UsuarioForm("Socio", "socio", null, "socio1234", Rol.MIEMBRO, true));
        }
    }

    @Test
    void sinTokenNoSePuedeUsarLaApi() throws Exception {
        mvc.perform(get("/api/resumen")).andExpect(status().isUnauthorized());
    }

    @Test
    void conPasswordIncorrectaNoSeEntra() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"socio\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void unMiembroVeElResumenPeroNoLaAdministracion() throws Exception {
        String token = login("socio", "socio1234");

        mvc.perform(get("/api/resumen").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void elAdminPuedeEntrarEnLaAdministracion() throws Exception {
        String token = login("admin", "admin1234");

        mvc.perform(get("/api/admin/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String login(String username, String password) throws Exception {
        String respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }
}
