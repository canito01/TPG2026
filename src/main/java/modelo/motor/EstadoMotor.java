package modelo.motor;

/**
 * Tabla de estados (Aclaracion R3, Enunciado E1-02, Ficha escenario C):
 *
 *   Desde             Accion                  Hacia
 *   Disponible        prepararSalto           Preparando salto
 *   Preparando salto  saltar                  En warp
 *   En warp           enfriar                 Enfriamiento
 *   Enfriamiento      terminarEnfriamiento    Disponible      ("pasa un tiempo")
 *
 * Contrato comun de las cuatro acciones:
 *  - Pre: el estado actual es el "Desde" de la tabla para esa accion.
 *  - Post (exito): el motor queda en el "Hacia" de la tabla.
 *  - Si la Pre no se cumple (accion no permitida en el estado actual) se lanza
 *    TransicionInvalidaException: el motor queda EXACTAMENTE igual. No hay
 *    rechazos silenciosos: el asistente la captura y la registra en la bitacora.
 */
public interface EstadoMotor {

    /** De Disponible. a Preparando salto. */
    void prepararSalto(MotorWarp m);

    /** De Preparando salto. a En warp. */
    void saltar(MotorWarp m);

    /** De En warp. a Enfriamiento. */
    void enfriar(MotorWarp m);

    /** De Enfriamiento. a Disponible. */
    void terminarEnfriamiento(MotorWarp m);

    /** Nombre del estado, tal como lo nombra el Enunciado: lo usa la bitacora. */
    String nombre();

    /** Post: true solo en Disponible. Lo usan las misiones para verificar que la nave este disponible. */
    boolean esDisponible();
}
