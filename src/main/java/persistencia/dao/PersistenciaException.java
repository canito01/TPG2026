package persistencia.dao;

/**
 * Excepcion de la capa de persistencia para encapsular errores tecnicos de JDBC/SQL
 * sin filtrar SQLException hacia el servicio o la interfaz grafica.
 */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenciaException(String message) {
        super(message);
    }
}
