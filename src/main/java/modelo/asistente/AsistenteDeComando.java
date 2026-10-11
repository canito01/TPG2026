package modelo.asistente;

import java.util.List;

/**
 * Contrato del asistente. Opera una sola nave durante toda su vida.
 * Delega las operaciones a los subsistemas (recursos, motor) y
 * convierte los rechazos en un Resultado en vez de propagar excepciones.
 * Si un parametro es invalido (negativo, null, etc) lanza IllegalArgumentException.
 * Si el parametro es valido pero no se puede aplicar, devuelve un Resultado de rechazo.
 */
public interface AsistenteDeComando {

    /** devuelve el id de la nave */
    String getNaveId();

    /** devuelve el nombre del estado actual del motor (Disponible, Preparando salto, En warp, Enfriamiento) */
    String consultarEstado();

    /** devuelve una copia de los recursos actuales (no modifica nada) */
    ResumenRecursos consultarRecursos();

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

    /** Ejecuta el salto. Solo funciona desde Preparando salto. */
    Resultado saltar();

    /** Inicia el enfriamiento. Solo funciona desde En warp. */
    Resultado enfriar();

    /** Termina el enfriamiento y vuelve a Disponible. */
    Resultado terminarEnfriamiento();

    /** Registra un evento en la bitacora (la descripcion no puede ser null ni vacia). */
    void registrarEvento(String descripcion);

    /** Devuelve una copia de la bitacora. Agregar eventos despues no cambia esta lista. */
    List<Evento> consultarBitacora();
}
