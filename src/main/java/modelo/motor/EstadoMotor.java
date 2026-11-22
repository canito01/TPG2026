package modelo.motor;

/**
 * Patron State (Anexo, E1-02): interfaz comun de las fases del Motor Warp.
 * Cada estado decide por si mismo que hace en un turno con cada operacion y a
 * que estado pasa: no hay if/switch sobre estados, y un estado nuevo no obliga
 * a tocar los existentes. Las acciones internas (preparar, saltar en warp,
 * enfriar) viven dentro de los estados; hacia afuera el motor solo muestra
 * saltar() y saltarWarp().
 */
public interface EstadoMotor {

    /** Un turno de la nave con salto simple. */
    ResultadoDeTurno saltar(MotorWarp m);

    /** Un turno de la nave con salto warp. */
    ResultadoDeTurno saltarWarp(MotorWarp m);

    /** Nombre de la fase, tal como lo nombra el Enunciado. */
    String nombre();
}
