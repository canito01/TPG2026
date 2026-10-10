package modelo.motor;

/**
 * Patron State (E1-02, UML «State» EstadoMotor): interfaz comun de los estados
 * del Motor Warp. Cada estado implementa las acciones y decide por si mismo si
 * son validas y a que estado se pasa; no hay if/switch central y agregar un
 * estado nuevo no obliga a tocar los existentes. Los estados no guardan datos:
 * reciben el contexto (el motor) como parametro.
 *
 * Tabla de transiciones (Aclaracion R3, Enunciado E1-02, Ficha escenario C):
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

    /** Pre: Disponible. Post: Preparando salto. */
    void prepararSalto(MotorWarp m);

    /** Pre: Preparando salto. Post: En warp. */
    void saltar(MotorWarp m);

    /** Pre: En warp. Post: Enfriamiento. */
    void enfriar(MotorWarp m);

    /** Pre: Enfriamiento. Post: Disponible. */
    void terminarEnfriamiento(MotorWarp m);

    /** Nombre del estado, tal como lo nombra el Enunciado: lo usa la bitacora. */
    String nombre();

    /** Post: true solo en Disponible. Lo usan las misiones para verificar que la nave este disponible. */
    boolean esDisponible();
}
