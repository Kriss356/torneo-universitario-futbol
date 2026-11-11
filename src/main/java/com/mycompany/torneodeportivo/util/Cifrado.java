package com.mycompany.torneodeportivo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Cifra contraseñas con SHA-256 (64 caracteres hexadecimales en minúscula),
 * igual que la función SHA2(texto, 256) de MariaDB.
 *
 * @author Christian Hernandez Garcia
 */
public final class Cifrado {

    private Cifrado() {
    }

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
