package modelo.asistente;

import java.util.List;
import java.util.Optional;

import util.observer.Observador;

/**
 * Contrato del asistente. Opera una sola nave durante toda su vida.
 * Delega las operaciones a los subsistemas (recursos, motor) y
 * comunica los rechazos operativos mediante resultados.
 * Si un parametro es invalido (negativo, null, etc) lanza IllegalArgumentException.
 * Si el parametro es valido pero no se puede aplicar, devuelve un Resultado de rechazo.
 */
public interface AsistenteDeComando {

    /** devuelve el id de la nave */
    String getNaveId();

    /** devuelve el nombre del estado actual del motor (Disponible, Preparando salto, En warp, Enfriamiento) */
    String consultarEstado();

    /** Devuelve la posición actual. La consulta queda vacía si no se configuró navegación. */
    default Optional<PosicionNave> consultarPosicion() {
        return Optional.empty();
    }

    /** Consulta con el radar de corto alcance si hay un obstáculo en la casilla. */
    default ResultadoEscaneo escanear(PosicionNave casilla) {
        rechazarSinNavegacion("Escaneo", casilla);
        return ResultadoEscaneo.RECHAZADO;
    }

    /** Procesa un turno de salto normal. Si avanza, la nave se desplaza una casilla. */
    default ResultadoSalto saltar(PosicionNave siguiente) {
        return new ResultadoSalto(ResultadoSalto.Estado.RECHAZADO,
                rechazarSinNavegacion("Salto normal", siguiente));
    }

    /**
     * Procesa un turno de Warp hacia la siguiente casilla de la ruta.
     * Puede dedicar el turno a preparación, avanzar dos casillas, enfriar o rechazar
     * la maniobra. Consultar la posición permite conocer dónde quedó la nave.
     */
    default ResultadoSalto saltarWarp(PosicionNave siguiente) {
        return new ResultadoSalto(ResultadoSalto.Estado.RECHAZADO,
                rechazarSinNavegacion("Salto Warp", siguiente));
    }

    private String rechazarSinNavegacion(String accion, PosicionNave casilla) {
        if (casilla == null) {
            throw new IllegalArgumentException("La casilla es obligatoria.");
        }
        String mensaje = accion + " rechazado: la navegación no está configurada.";
        registrarEvento(mensaje);
        return mensaje;
    }

    /** devuelve una copia de los recursos actuales (no modifica nada) */
    ResumenRecursos consultarRecursos();

    /**
     * Suscribe un observador a los cambios de recursos de esta nave.
     * El aviso se envía después del cambio, en el hilo que lo realiza.
     * Para leer los valores iniciales o actualizados se usa consultarRecursos().
     * @throws IllegalArgumentException si el observador es nulo
     */
    void agregarObservadorRecursos(Observador observador);

    /** Quita la suscripción a los cambios de recursos. */
    void quitarObservadorRecursos(Observador observador);

    /**
     * Consume combustible, energia y agrega desgaste.
     * Los tres se aplican juntos o no se aplica ninguno.
     * Lanza IllegalArgumentException si alguno es negativo.
     */
    Resultado consumir(int combustible, int energia, int desgaste);

    /** Carga combustible. Si se pasa de la capacidad, devuelve rechazo. */
    Resultado cargarCombustible(int cantidad);

    /** Carga energia. Si se pasa de la capacidad, devuelve rechazo. */
    Resultado cargarEnergia(int cantidad);

    /** Hace mantenimiento, deja el desgaste en 0. */
    Resultado realizarMantenimiento();

    /** Prepara el salto. Solo funciona desde Disponible. */
    Resultado prepararSalto();

    /** Transición de la Entrega 1. Para un salto normal de misión se indica la siguiente casilla. */
    Resultado saltar();

    /** Inicia el enfriamiento. Solo funciona desde En warp. */
    Resultado enfriar();

    /** Termina el enfriamiento y vuelve a Disponible. */
    Resultado terminarEnfriamiento();

    /** Registra un evento en la bitacora (la descripcion no puede ser null ni vacia). */
    void registrarEvento(String descripcion);

    /** Devuelve una copia de la bitacora. Agregar eventos despues no cambia esta lista. */
    List<Evento> consultarBitacora();

    /**
     * Suscribe un observador no nulo a los nuevos eventos de esta bitacora.
     * No repite eventos anteriores ni registra dos veces al mismo observador.
     * El aviso se envia despues de guardar el evento, en el hilo que lo registra.
     * Para leer el historial inicial se utiliza consultarBitacora().
     * @throws IllegalArgumentException si el observador es nulo
     */
    void agregarObservadorBitacora(Observador observador);

    /** Quita la suscripcion. Si el observador no estaba registrado, no hace nada. */
    void quitarObservadorBitacora(Observador observador);
}
