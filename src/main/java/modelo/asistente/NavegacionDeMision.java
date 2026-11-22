package modelo.asistente;

import java.util.List;

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

    /** Incluye el origen y el destino. Una lista vacía indica que no hay ruta. */
    default List<PosicionNave> calcularRuta(PosicionNave destino) { return List.of(); }

    /** Devuelve la descripción del encuentro, o null si no hay un elemento. */
    default String interactuarConElementoActual() { return null; }

    default void avanzarTurnoTablero() { }
}
