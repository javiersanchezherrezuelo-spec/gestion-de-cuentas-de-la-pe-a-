package com.informales.pena.notificacion;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;

import org.junit.jupiter.api.Test;

/** Comprueba el cifrado con el ejemplo del apéndice A del RFC 8291. */
class WebPushTest {

    private static final Base64.Decoder B64 = Base64.getUrlDecoder();

    @Test
    void cifraIgualQueElEjemploDelRfc8291() throws Exception {
        String mensaje = "When I grow up, I want to be a watermelon";
        String privadaServidor = "yfWPiYE-n46HLnH0KqZOF1fJJU3MYrct3AELtAQ-oRw";
        String publicaServidor = "BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8";
        String publicaNavegador = "BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4";
        String auth = "BTBZMqHH6r4Tts7J_aSIgg";
        String sal = "DGv6ra1nlYgDCS1FRnbzlw";
        String esperado = "DGv6ra1nlYgDCS1FRnbzlwAAEABBBP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A_yl95bQpu6cVPTpK4Mqgkf1CXztLVBSt2Ks3oZwbuwXPXLWyouBWLVWGNWQexSgSxsj_Qulcy4a-fN";

        KeyPair efimero = new KeyPair(WebPush.clavePublica(B64.decode(publicaServidor)),
                WebPush.clavePrivada(privadaServidor));

        byte[] cuerpo = WebPush.cifrar(mensaje.getBytes(StandardCharsets.UTF_8), publicaNavegador, auth, efimero,
                B64.decode(sal));

        assertThat(Base64.getUrlEncoder().withoutPadding().encodeToString(cuerpo)).isEqualTo(esperado);
    }

    @Test
    void lasClavesVapidGeneradasSonValidas() throws Exception {
        String[] claves = WebPush.generarClavesVapid();

        assertThat(B64.decode(claves[0])).hasSize(65).startsWith((byte) 4);
        assertThat(B64.decode(claves[1])).hasSize(32);
        assertThat(WebPush.clavePublica(B64.decode(claves[0]))).isNotNull();
        assertThat(WebPush.clavePrivada(claves[1])).isNotNull();
    }

    @Test
    void elOrigenEsEsquemaYHost() {
        assertThat(WebPush.origen("https://fcm.googleapis.com/fcm/send/abc:123")).isEqualTo("https://fcm.googleapis.com");
        assertThat(WebPush.origen("https://web.push.apple.com/QGuQyavXutnMH")).isEqualTo("https://web.push.apple.com");
    }
}
