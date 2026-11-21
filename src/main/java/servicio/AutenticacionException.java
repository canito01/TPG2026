package servicio;

/**
 * Excepcion base de dominio para errores de autenticacion y registro de usuarios.
 */
public class AutenticacionException extends RuntimeException {

    public AutenticacionException(String message) {
        super(message);
    }
}
