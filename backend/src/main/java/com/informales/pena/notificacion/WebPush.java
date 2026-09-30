package com.informales.pena.notificacion;

import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cifrado de mensajes Web Push (RFC 8291, "aes128gcm") y firma VAPID (RFC 8292), solo con Java y jjwt.
 * No necesita librerías externas de push.
 */
final class WebPush {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DECODER = Base64.getUrlDecoder();
    private static final ECParameterSpec P256 = parametrosP256();

    private WebPush() {
    }

    /** Genera un par de claves VAPID. Devuelve {publica, privada} en base64url. */
    static String[] generarClavesVapid() {
        KeyPair par = generarPar();
        String publica = B64.encodeToString(puntoSinComprimir((ECPublicKey) par.getPublic()));
        String privada = B64.encodeToString(a32Bytes(((ECPrivateKey) par.getPrivate()).getS()));
        return new String[] {publica, privada};
    }

    static ECPrivateKey clavePrivada(String base64) {
        try {
            var spec = new ECPrivateKeySpec(new BigInteger(1, B64_DECODER.decode(base64)), P256);
            return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(spec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Clave VAPID privada no válida", e);
        }
    }

    /** "https://fcm.googleapis.com/fcm/send/abc" -> "https://fcm.googleapis.com" (el "aud" del JWT de VAPID). */
    static String origen(String endpoint) {
        URI uri = URI.create(endpoint);
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    /**
     * Cifra el mensaje para una suscripción.
     *
     * @param p256dh clave pública del navegador (base64url)
     * @param auth   secreto de autenticación del navegador (base64url)
     * @return el cuerpo que hay que mandar al servicio de push
     */
    static byte[] cifrar(byte[] mensaje, String p256dh, String auth) {
        // Par de claves y sal de un solo uso para este mensaje
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        return cifrar(mensaje, p256dh, auth, generarPar(), sal);
    }

    /** Versión con el par de claves y la sal fijados, para poder probarla con el ejemplo del RFC 8291. */
    static byte[] cifrar(byte[] mensaje, String p256dh, String auth, KeyPair efimero, byte[] sal) {
        try {
            byte[] clavePublicaNavegador = B64_DECODER.decode(p256dh);
            byte[] secretoAuth = B64_DECODER.decode(auth);
            byte[] clavePublicaServidor = puntoSinComprimir((ECPublicKey) efimero.getPublic());

            KeyAgreement ecdh = KeyAgreement.getInstance("ECDH");
            ecdh.init(efimero.getPrivate());
            ecdh.doPhase(clavePublica(clavePublicaNavegador), true);
            byte[] secretoCompartido = ecdh.generateSecret();

            // IKM = HKDF(auth, ecdh, "WebPush: info" || 0 || ua_public || as_public, 32)
            byte[] infoClave = concatenar("WebPush: info\0".getBytes(StandardCharsets.US_ASCII),
                    clavePublicaNavegador, clavePublicaServidor);
            byte[] ikm = hkdf(secretoAuth, secretoCompartido, infoClave, 32);

            byte[] cek = hkdf(sal, ikm, "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII), 16);
            byte[] nonce = hkdf(sal, ikm, "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII), 12);

            // Un único registro: mensaje + delimitador 0x02 (último registro), sin relleno
            Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
            aes.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cek, "AES"), new GCMParameterSpec(128, nonce));
            byte[] cifrado = aes.doFinal(concatenar(mensaje, new byte[] {2}));

            // Cabecera: sal (16) | tamaño de registro (4) | longitud de la clave (1) | clave pública del servidor
            ByteBuffer cuerpo = ByteBuffer.allocate(16 + 4 + 1 + clavePublicaServidor.length + cifrado.length);
            cuerpo.put(sal).putInt(4096).put((byte) clavePublicaServidor.length).put(clavePublicaServidor).put(cifrado);
            return cuerpo.array();
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se pudo cifrar el mensaje push", e);
        }
    }

    private static byte[] hkdf(byte[] sal, byte[] ikm, byte[] info, int longitud) throws GeneralSecurityException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(sal, "HmacSHA256"));
        byte[] prk = hmac.doFinal(ikm);
        hmac.init(new SecretKeySpec(prk, "HmacSHA256"));
        byte[] t1 = hmac.doFinal(concatenar(info, new byte[] {1}));
        return Arrays.copyOf(t1, longitud);
    }

    private static KeyPair generarPar() {
        try {
            KeyPairGenerator generador = KeyPairGenerator.getInstance("EC");
            generador.initialize(new ECGenParameterSpec("secp256r1"), ALEATORIO);
            return generador.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    static PublicKey clavePublica(byte[] punto) throws GeneralSecurityException {
        if (punto.length != 65 || punto[0] != 4) {
            throw new InvalidKeyException("La clave p256dh no es un punto P-256 sin comprimir");
        }
        var x = new BigInteger(1, Arrays.copyOfRange(punto, 1, 33));
        var y = new BigInteger(1, Arrays.copyOfRange(punto, 33, 65));
        return KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(x, y), P256));
    }

    private static byte[] puntoSinComprimir(ECPublicKey clave) {
        return concatenar(new byte[] {4}, a32Bytes(clave.getW().getAffineX()), a32Bytes(clave.getW().getAffineY()));
    }

    private static byte[] a32Bytes(BigInteger numero) {
        byte[] bytes = numero.toByteArray();
        if (bytes.length == 32) return bytes;
        byte[] resultado = new byte[32];
        int copiar = Math.min(bytes.length, 32);
        System.arraycopy(bytes, bytes.length - copiar, resultado, 32 - copiar, copiar);
        return resultado;
    }

    private static byte[] concatenar(byte[]... partes) {
        int total = 0;
        for (byte[] p : partes) total += p.length;
        ByteBuffer buffer = ByteBuffer.allocate(total);
        for (byte[] p : partes) buffer.put(p);
        return buffer.array();
    }

    private static ECParameterSpec parametrosP256() {
        try {
            AlgorithmParameters parametros = AlgorithmParameters.getInstance("EC");
            parametros.init(new ECGenParameterSpec("secp256r1"));
            return parametros.getParameterSpec(ECParameterSpec.class);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
