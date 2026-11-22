package modelo.motor;

/**
 * Lo que el Motor Warp necesita de la nave: sus recursos y moverla.
 * La define Joaquin y la IMPLEMENTA la Nave de Eneas (o se adapta a sus
 * Recursos), asi el motor depende de una abstraccion y no de Nave ni Recursos
 * concretos. La nave crea su motor pasandose a si misma: new MotorWarp(this).
 *
 * Los valores son enteros, como en la Ficha. El motor siempre consulta antes de
 * consumir, por eso consumir exige tener lo que se consume.
 */
public interface SistemasDeLaNave {

    /** Post: combustible actual (0 a 100). */
    int getCombustible();

    /** Post: energia actual (0 a 100). */
    int getEnergia();

    /** Pre: 0 < cantidad <= getCombustible(). Post: combustible disminuye en 'cantidad'. */
    void consumirCombustible(int cantidad);

    /** Pre: 0 < cantidad <= getEnergia(). Post: energia disminuye en 'cantidad'. */
    void consumirEnergia(int cantidad);

    /** Pre: cantidad > 0. Post: el desgaste aumenta en 'cantidad' sin pasar de 100 (satura). */
    void agregarDesgaste(int cantidad);

    /**
     * Mueve la nave por su ruta hacia el destino.
     * Pre: casillas >= 1 y la nave tiene una ruta.
     * Post: devuelve cuantas casillas se movio de verdad (menos que 'casillas'
     * si el destino queda antes) y la posicion de la nave se actualiza.
     */
    int avanzar(int casillas);
}
