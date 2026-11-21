package servicio;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Utilidad criptografica estandar del JDK (javax.crypto) para guardar contraseñas
 * con hash PBKDF2WithHmacSHA256 y sal aleatoria por usuario, sin librerias externas.
 */
public final class HashContrasena {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 65536;
    private static final int LONGITUD_CLAVE_BITS = 256;
    private static final int LONGITUD_SAL_BYTES = 16;

    private static final SecureRandom RANDOM = new SecureRandom();

    private HashContrasena() {
    }

    public static String hash(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser vacia.");
        }
        byte[] sal = new byte[LONGITUD_SAL_BYTES];
        RANDOM.nextBytes(sal);
        byte[] hash = derivar(passwordPlano.toCharArray(), sal);
        return Base64.getEncoder().encodeToString(sal) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String passwordPlano, String hashAlmacenado) {
        if (passwordPlano == null || hashAlmacenado == null) {
            return false;
        }
        String[] partes = hashAlmacenado.split(":");
        if (partes.length != 2) {
            return false;
        }
        byte[] sal = Base64.getDecoder().decode(partes[0]);
        byte[] hashEsperado = Base64.getDecoder().decode(partes[1]);
        byte[] hashCalculado = derivar(passwordPlano.toCharArray(), sal);
        return MessageDigest.isEqual(hashEsperado, hashCalculado);
    }

    private static byte[] derivar(char[] password, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(password, sal, ITERACIONES, LONGITUD_CLAVE_BITS);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITMO);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo generar el hash de la contraseña.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
