package servicio;

/**
 * Se lanza al intentar registrar un nombre de usuario que ya existe en el sistema.
 */
public class UsuarioDuplicadoException extends AutenticacionException {

    public UsuarioDuplicadoException(String nombreUsuario) {
        super("El usuario '" + nombreUsuario + "' ya se encuentra registrado.");
    }
}
