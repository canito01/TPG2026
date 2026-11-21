package persistencia.dto;

import java.util.Objects;

/**
 * Data Transfer Object (DTO) para transportar las credenciales de un usuario
 * entre la capa de persistencia (DAO) y la capa de servicio/controlador.
 */
public class UsuarioDTO {

    private final Long id;
    private final String nombreUsuario;
    private final String passwordHash;

    public UsuarioDTO(String nombreUsuario, String passwordHash) {
        this(null, nombreUsuario, passwordHash);
    }

    public UsuarioDTO(Long id, String nombreUsuario, String passwordHash) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.passwordHash = passwordHash;
    }

    public Long getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioDTO)) return false;
        UsuarioDTO that = (UsuarioDTO) o;
        return Objects.equals(id, that.id)
                && Objects.equals(nombreUsuario, that.nombreUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombreUsuario);
    }

    @Override
    public String toString() {
        return "UsuarioDTO{id=" + id + ", nombreUsuario='" + nombreUsuario + "'}";
    }
}
