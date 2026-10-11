package modelo.asistente;

/**
 * Excepcion base para las excepciones del dominio del sistema.
 */
public class ExcepcionDominio extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcepcionDominio(String mensaje) {
        super(mensaje);
    }

    public ExcepcionDominio(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

