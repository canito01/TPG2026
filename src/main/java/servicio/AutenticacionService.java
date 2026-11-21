package servicio;

import persistencia.dao.UsuarioDAO;
import persistencia.dto.UsuarioDTO;

import java.util.Optional;

/**
 * Capa de servicio para el registro e inicio de sesion de operadores.
 * Aplica las reglas de negocio (unicidad, longitudes minimas, verificacion de hash)
 * delegando el acceso a datos en UsuarioDAO.
 */
public class AutenticacionService {

    public static final int MIN_LONGITUD_USUARIO = 3;
    public static final int MAX_LONGITUD_USUARIO = 50;
    public static final int MIN_LONGITUD_PASSWORD = 4;

    private final UsuarioDAO usuarioDAO;

    public AutenticacionService(UsuarioDAO usuarioDAO) {
        if (usuarioDAO == null) {
            throw new IllegalArgumentException("El UsuarioDAO no puede ser null.");
        }
        this.usuarioDAO = usuarioDAO;
    }

    public UsuarioDTO registrar(String nombreUsuario, String passwordPlano) {
        String usuarioNormalizado = validarNombreUsuario(nombreUsuario);
        validarPassword(passwordPlano);

        if (usuarioDAO.existePorNombre(usuarioNormalizado)) {
            throw new UsuarioDuplicadoException(usuarioNormalizado);
        }

        String hash = HashContrasena.hash(passwordPlano);
        UsuarioDTO nuevo = new UsuarioDTO(usuarioNormalizado, hash);
        return usuarioDAO.guardar(nuevo);
    }

    public UsuarioDTO iniciarSesion(String nombreUsuario, String passwordPlano) {
        if (nombreUsuario == null || nombreUsuario.isBlank()
                || passwordPlano == null || passwordPlano.isEmpty()) {
            throw new CredencialesInvalidasException();
        }

        String usuarioNormalizado = nombreUsuario.trim();
        Optional<UsuarioDTO> encontrado = usuarioDAO.buscarPorNombre(usuarioNormalizado);
        if (encontrado.isEmpty()) {
            throw new CredencialesInvalidasException();
        }

        UsuarioDTO dto = encontrado.get();
        if (!HashContrasena.verificar(passwordPlano, dto.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        return dto;
    }

    private String validarNombreUsuario(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new AutenticacionException("El nombre de usuario no puede estar vacio.");
        }
        String limpio = nombreUsuario.trim();
        if (limpio.length() < MIN_LONGITUD_USUARIO || limpio.length() > MAX_LONGITUD_USUARIO) {
            throw new AutenticacionException(
                    "El nombre de usuario debe tener entre " + MIN_LONGITUD_USUARIO
                            + " y " + MAX_LONGITUD_USUARIO + " caracteres.");
        }
        return limpio;
    }

    private void validarPassword(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.length() < MIN_LONGITUD_PASSWORD) {
            throw new AutenticacionException(
                    "La contraseña debe tener al menos " + MIN_LONGITUD_PASSWORD + " caracteres.");
        }
    }
}
