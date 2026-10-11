package modelo.nave;

/**
 * Se lanza cuando una operación sobre los {@link Recursos} dejaría algún valor fuera de su
 * rango válido (por ejemplo, cargar más combustible que la capacidad máxima o consumir más
 * energía de la disponible).
 *
 * <p>Cuando se lanza, el objeto {@link Recursos} conserva exactamente su estado anterior:
 * las operaciones nunca dejan cambios parciales.</p>
 *
 * <p>Es una excepción no verificada para que el asistente de comando pueda capturarla,
 * registrarla en la bitácora y devolver un resultado usable al cliente.</p>
 */
public class RecursoFueraDeRangoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RecursoFueraDeRangoException(String mensaje) {
        super(mensaje);
    }
}
