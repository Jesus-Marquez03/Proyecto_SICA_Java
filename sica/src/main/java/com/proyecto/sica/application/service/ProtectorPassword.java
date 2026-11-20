package com.proyecto.sica.application.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Genera y verifica hashes PBKDF2-HMAC-SHA256 con una sal individual.
 */
public final class ProtectorPassword {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String IDENTIFICADOR = "pbkdf2-sha256";
    private static final int ITERACIONES = 600_000;
    private static final int LONGITUD_SAL_BYTES = 16;
    private static final int LONGITUD_HASH_BITS = 256;

    private final SecureRandom secureRandom;

    public ProtectorPassword() {
        this(new SecureRandom());
    }

    ProtectorPassword(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public String generarHash(char[] password) {
        validarPassword(password);
        byte[] sal = new byte[LONGITUD_SAL_BYTES];
        secureRandom.nextBytes(sal);
        byte[] hash = derivar(password, sal, ITERACIONES);

        return IDENTIFICADOR + "$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verificar(char[] password, String hashAlmacenado) {
        if (password == null || hashAlmacenado == null) {
            return false;
        }

        try {
            String[] partes = hashAlmacenado.split("\\$");
            if (partes.length != 4 || !IDENTIFICADOR.equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);
            byte[] hashCalculado = derivar(password, sal, iteraciones);
            return MessageDigest.isEqual(hashEsperado, hashCalculado);
        } catch (IllegalArgumentException excepcion) {
            return false;
        }
    }

    private byte[] derivar(char[] password, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(
                password, sal, iteraciones, LONGITUD_HASH_BITS);
        try {
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance(ALGORITMO);
            return fabrica.generateSecret(especificacion).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException excepcion) {
            throw new IllegalStateException("No fue posible proteger la contrasena", excepcion);
        } finally {
            especificacion.clearPassword();
        }
    }

    private void validarPassword(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("La contrasena es obligatoria");
        }
    }
}
