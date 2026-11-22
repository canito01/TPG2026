package modelo.asistente;

/**
 * Operaciones que el asistente delega a la navegación de una nave.
 * El objeto recibido por el asistente debe operar sobre esa misma nave.
 * El subsistema decide si se puede avanzar y aplica el movimiento y los costos.
 */
public interface NavegacionDeMision {
    PosicionNave consultarPosicion();

    String consultarEstado();

    ResultadoEscaneo escanear(PosicionNave casilla);

    ResultadoSalto saltar(PosicionNave siguiente);

    /** La siguiente casilla indica la dirección del Warp, que puede avanzar dos. */
    ResultadoSalto saltarWarp(PosicionNave siguiente);
}
