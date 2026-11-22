package modelo.elementos;

import modelo.nave.Nave;

/**
 * Contrato común de todos los elementos espaciales con los que la nave puede
 * coincidir en una coordenada del tablero durante su travesía:
 * naves enemigas, asteroides y estaciones de carga.
 */
public interface ElementoDelUniverso {

    /**
     * Nombre identificatorio del elemento para el tablero y la bitácora.
     * Postcondición: nunca es nulo ni vacío.
     */
    String getNombre();

    /**
     * Aplica sobre la nave propia la interacción correspondiente al coincidir
     * en la misma coordenada del universo.
     *
     * Precondición: {@code nave != null}.
     * Postcondición: el estado de la nave refleja las consecuencias del encuentro
     * y se devuelve una descripción del evento ocurrido.
     */
    String interactuarCon(Nave nave);
}
