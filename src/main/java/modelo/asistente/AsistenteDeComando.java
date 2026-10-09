package modelo.asistente;

import java.util.List;

/**
 * Contrato de un asistente que opera una sola nave durante toda su vida.
 *
 * <p>El asistente delega las operaciones en la nave y sus subsistemas. Registra
 * las operaciones y sus resultados, y convierte los rechazos de dominio en un
 * {@link Resultado}. Un rechazo debe conservar el estado anterior de la nave.
 * Las consultas devuelven datos sin acceso para modificar los subsistemas.
 * Ninguna operación depende de consola ni de una interfaz gráfica.</p>
 *
 * <p>Los parámetros inválidos, como cantidades negativas o descripciones
 * vacías, se rechazan con {@link IllegalArgumentException}. Las cantidades
 * válidas que no pueden aplicarse por recursos o estado producen un resultado
 * de rechazo y un registro en la bitácora.</p>
 */
public interface AsistenteDeComando {

    /** @return identidad no nula ni vacía de la nave operada; no cambia. */
    String getNaveId();

    /**
     * @return nombre del estado actual del motor, sin modificarlo:
     *         Disponible, Preparando salto, En warp o Enfriamiento.
     */
    String consultarEstado();

    /** @return copia inmutable de los datos actuales de los recursos. */
    ResumenRecursos consultarRecursos();

    /**
     * Solicita el consumo de combustible y energía, y el aumento del desgaste.
     * Los tres cambios se aceptan juntos o se rechazan sin cambios parciales.
     * La validación y actualización corresponden al subsistema de recursos.
     *
     * @param combustible cantidad no negativa que se consume
     * @param energia cantidad no negativa que se consume
     * @param desgaste cantidad no negativa que se agrega
     * @return resultado de la operación, registrada junto con sus cantidades
     * @throws IllegalArgumentException si alguna cantidad es negativa
     */
    Resultado consumir(int combustible, int energia, int desgaste);

    /**
     * Solicita una carga de combustible y registra su resultado.
     * @param cantidad cantidad no negativa que se agrega
     * @return éxito o rechazo sin cambios si se excede la capacidad
     * @throws IllegalArgumentException si la cantidad es negativa
     */
    Resultado cargarCombustible(int cantidad);

    /**
     * Solicita una carga de energía y registra su resultado.
     * @param cantidad cantidad no negativa que se agrega
     * @return éxito o rechazo sin cambios si se excede la capacidad
     * @throws IllegalArgumentException si la cantidad es negativa
     */
    Resultado cargarEnergia(int cantidad);

    /**
     * Solicita mantenimiento y registra su resultado.
     * @return éxito con desgaste cero, o rechazo sin cambios
     */
    Resultado realizarMantenimiento();

    /**
     * Solicita al motor preparar el salto y registra el resultado.
     * @return éxito desde Disponible, o rechazo sin cambios desde otro estado
     */
    Resultado prepararSalto();

    /**
     * Solicita al motor el salto y registra las transiciones realizadas.
     * El motor determina el momento en que el salto termina y vuelve a Disponible.
     * @return éxito desde Preparando salto, o rechazo sin cambios
     */
    Resultado saltar();

    /**
     * Solicita al motor comenzar el enfriamiento y registra el resultado.
     * @return éxito desde En warp hacia Enfriamiento, o rechazo sin cambios
     */
    Resultado enfriar();

    /**
     * Solicita al motor terminar el enfriamiento y registra el resultado.
     * @return éxito desde Enfriamiento hacia Disponible, o rechazo sin cambios
     */
    Resultado terminarEnfriamiento();

    /**
     * Registra un acontecimiento, por ejemplo una etapa o resultado de misión.
     * @param descripcion texto no nulo, vacío ni compuesto solo por espacios
     * @throws IllegalArgumentException si la descripción es inválida
     */
    void registrarEvento(String descripcion);

    /**
     * @return copia de solo lectura de los eventos en orden temporal;
     *         los registros posteriores no modifican esta copia
     */
    List<Evento> consultarBitacora();
}
