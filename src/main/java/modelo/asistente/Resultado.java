package modelo.asistente;

/** Resultado inmutable de una orden: indica si tuvo éxito y explica lo ocurrido. */
public final class Resultado {
    private final boolean exito;
    private final String mensaje;

    /**
     * @param exito verdadero si la operación se completó
     * @param mensaje explicación no nula, vacía ni compuesta solo por espacios
     * @throws IllegalArgumentException si el mensaje es inválido
     */
    public Resultado(boolean exito, String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El resultado debe tener un mensaje.");
        }
        this.exito = exito;
        this.mensaje = mensaje;
    }

    public boolean esExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }
}
