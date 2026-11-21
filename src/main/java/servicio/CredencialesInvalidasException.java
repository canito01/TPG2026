package servicio;

/**
 * Se lanza cuando el usuario no existe o la contraseña ingresada no coincide.
 */
public class CredencialesInvalidasException extends AutenticacionException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.");
    }
}
