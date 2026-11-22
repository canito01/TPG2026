package modelo.motor;

/**
 * Lo que hizo el motor en un turno: dato inmutable que el asistente usa para
 * registrar la decision en la bitacora (sin que el motor conozca la bitacora)
 * y que la mision usa para saber si la nave se movio.
 */
public final class ResultadoDeTurno {

    private final String estadoAnterior;
    private final String estadoActual;
    private final int casillasAvanzadas;
    private final String descripcion;

    ResultadoDeTurno(String estadoAnterior, String estadoActual, int casillasAvanzadas, String descripcion) {
        this.estadoAnterior = estadoAnterior;
        this.estadoActual = estadoActual;
        this.casillasAvanzadas = casillasAvanzadas;
        this.descripcion = descripcion;
    }

    /** Estado del motor al empezar el turno. */
    public String getEstadoAnterior() { return estadoAnterior; }

    /** Estado del motor al terminar el turno. */
    public String getEstadoActual() { return estadoActual; }

    /** Casillas que se movio la nave en este turno (0, 1 o 2). */
    public int getCasillasAvanzadas() { return casillasAvanzadas; }

    /** Texto para la bitacora. */
    public String getDescripcion() { return descripcion; }

    public boolean avanzo() { return casillasAvanzadas > 0; }

    public boolean cambioDeEstado() { return !estadoAnterior.equals(estadoActual); }

    @Override
    public String toString() {
        return "[" + estadoAnterior + " -> " + estadoActual + "] " + descripcion;
    }
}
