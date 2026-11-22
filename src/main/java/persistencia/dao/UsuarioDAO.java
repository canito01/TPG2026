package persistencia.dao;

import persistencia.dto.UsuarioDTO;

import java.util.Optional;

/**
 * Interfaz DAO (Data Access Object) para el acceso a datos de usuarios.
 * Desacopla la capa de servicio de la tecnologia concreta de base de datos (JDBC / H2).
 */
public interface UsuarioDAO {

    /**
     * Inserta un nuevo usuario en la base de datos y devuelve el DTO con el id generado.
     */
    UsuarioDTO guardar(UsuarioDTO usuario);

    /**
     * Busca un usuario por su nombre de usuario exacto.
     */
    Optional<UsuarioDTO> buscarPorNombre(String nombreUsuario);

    /**
     * Indica si ya existe un usuario registrado con ese nombre.
     */
    boolean existePorNombre(String nombreUsuario);
}
