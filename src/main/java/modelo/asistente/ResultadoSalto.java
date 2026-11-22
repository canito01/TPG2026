package modelo.asistente;

/** Resultado de un turno de navegación y explicación para la bitácora. */
public record ResultadoSalto(Estado estado, String mensaje) {
    public ResultadoSalto {
        if (estado == null || mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El resultado del salto requiere estado y mensaje.");
        }
    }

    public enum Estado {
        AVANZO,
        /** El turno se dedicó a preparar el Warp y la nave mantuvo su posición. */
        PREPARANDO,
        /** El turno se dedicó al enfriamiento y la nave mantuvo su posición. */
        ENFRIANDO,
        RECHAZADO
    }
}
