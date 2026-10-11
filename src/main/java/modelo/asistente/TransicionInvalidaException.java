package modelo.asistente;

/**
 * Excepcion que lanza el motor cuando se intenta una transicion
 * que no es valida desde el estado actual.
 * El asistente la captura y la convierte en un Resultado de rechazo.
 */
public class TransicionInvalidaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String estado;
    private final String accion;

    public TransicionInvalidaException(String estado, String accion) {
        super("Transicion invalida: no se puede " + accion + " desde " + estado);
        this.estado = estado;
        this.accion = accion;
    }

    public String getEstado() {
        return estado;
    }

    public String getAccion() {
        return accion;
    }
}
