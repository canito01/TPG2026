package persistencia.dao;

import persistencia.dto.UsuarioDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Implementacion JDBC de UsuarioDAO usando H2 y PreparedStatement.
 * Solo ejecuta operaciones de acceso a datos; las reglas de negocio viven en AutenticacionService.
 */
public class UsuarioDAOJdbc implements UsuarioDAO {

    private static final String SQL_INSERT =
            "INSERT INTO usuarios (nombre_usuario, password_hash) VALUES (?, ?)";
    private static final String SQL_SELECT_BY_NOMBRE =
            "SELECT id, nombre_usuario, password_hash FROM usuarios WHERE nombre_usuario = ?";
    private static final String SQL_EXISTS_BY_NOMBRE =
            "SELECT 1 FROM usuarios WHERE nombre_usuario = ?";

    private final ConexionBD conexionBD;

    public UsuarioDAOJdbc(ConexionBD conexionBD) {
        if (conexionBD == null) {
            throw new IllegalArgumentException("La conexionBD no puede ser null.");
        }
        this.conexionBD = conexionBD;
        this.conexionBD.inicializarEsquema();
    }

    @Override
    public UsuarioDTO guardar(UsuarioDTO usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser null.");
        }
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getPasswordHash());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long idGenerado = keys.getLong(1);
                    return new UsuarioDTO(idGenerado, usuario.getNombreUsuario(), usuario.getPasswordHash());
                }
            }
            return usuario;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el usuario '" + usuario.getNombreUsuario() + "'.", e);
        }
    }

    @Override
    public Optional<UsuarioDTO> buscarPorNombre(String nombreUsuario) {
        if (nombreUsuario == null) {
            return Optional.empty();
        }
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_NOMBRE)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UsuarioDTO dto = new UsuarioDTO(
                            rs.getLong("id"),
                            rs.getString("nombre_usuario"),
                            rs.getString("password_hash")
                    );
                    return Optional.of(dto);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el usuario '" + nombreUsuario + "'.", e);
        }
    }

    @Override
    public boolean existePorNombre(String nombreUsuario) {
        if (nombreUsuario == null) {
            return false;
        }
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_BY_NOMBRE)) {

            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar existencia del usuario '" + nombreUsuario + "'.", e);
        }
    }
}
