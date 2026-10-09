package modelo.asistente;

import java.time.LocalDateTime;

/** Acontecimiento inmutable de la bitácora, con fecha y descripción válidas. */
public final class Evento {
    private final LocalDateTime fecha;
    private final String descripcion;

    /**
     * @param fecha fecha y hora no nulas del acontecimiento
     * @param descripcion texto no nulo, vacío ni compuesto solo por espacios
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public Evento(LocalDateTime fecha, String descripcion) {
        if (fecha == null) {
            throw new IllegalArgumentException("El evento debe tener una fecha.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("El evento debe tener una descripción.");
        }
        this.fecha = fecha;
        this.descripcion = descripcion;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
