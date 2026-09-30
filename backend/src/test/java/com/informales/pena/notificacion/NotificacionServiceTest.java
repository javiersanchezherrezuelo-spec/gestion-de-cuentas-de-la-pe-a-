package com.informales.pena.notificacion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.informales.pena.usuario.Usuario;

class NotificacionServiceTest {

    /** Canal de mentira que apunta los avisos que recibe. */
    static class CanalFalso implements CanalNotificacion {
        final List<Aviso> recibidos = new ArrayList<>();
        final Predicate<Ajustes> activo;

        CanalFalso(Predicate<Ajustes> activo) {
            this.activo = activo;
        }

        @Override
        public boolean activo(Ajustes ajustes) {
            return activo.test(ajustes);
        }

        @Override
        public void enviar(Aviso aviso, Ajustes ajustes) {
            recibidos.add(aviso);
        }
    }

    @Test
    void soloEnviaPorLosCanalesActivados() {
        Ajustes ajustes = new Ajustes();
        ajustes.setCanalPush(true);
        ajustes.setCanalEmail(false);
        AjustesService ajustesService = mock(AjustesService.class);
        when(ajustesService.obtener()).thenReturn(ajustes);

        CanalFalso push = new CanalFalso(Ajustes::isCanalPush);
        CanalFalso email = new CanalFalso(Ajustes::isCanalEmail);
        var servicio = new NotificacionService(List.of(push, email), ajustesService);

        servicio.enviar(new Aviso(List.of(new Usuario()), "Hola", "Prueba", "/"));

        assertThat(push.recibidos).hasSize(1);
        assertThat(email.recibidos).isEmpty();
    }

    @Test
    void sinDestinatariosNoEnviaNada() {
        AjustesService ajustesService = mock(AjustesService.class);
        CanalFalso push = new CanalFalso(a -> true);
        var servicio = new NotificacionService(List.of(push), ajustesService);

        servicio.enviar(new Aviso(List.of(), "Hola", "Prueba", "/"));

        assertThat(push.recibidos).isEmpty();
    }
}
