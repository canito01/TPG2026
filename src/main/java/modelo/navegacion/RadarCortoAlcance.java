package modelo.navegacion;

import modelo.tablero.ElementoDePrueba;
import modelo.tablero.Tablero;

/**
 * Radar de corto alcance (turno de la nave): informa que hay en la casilla
 * siguiente. No decide nada: la mision elige entre saltar y saltarWarp.
 */
public class RadarCortoAlcance {

    private final Tablero tablero;

    public RadarCortoAlcance(Tablero tablero) {
        if (tablero == null) {
            throw new IllegalArgumentException("El tablero no puede ser null");
        }
        this.tablero = tablero;
    }

    /** Pre: c valida. Post: el elemento de esa casilla o null si esta despejada. */
    public ElementoDePrueba escanear(Coordenada c) {
        return tablero.getElementoEn(c);
    }

    /**
     * Pre: ruta.contiene(actual). Post: el elemento de la casilla siguiente de la
     * ruta o null si esta despejada; null tambien si 'actual' ya es el destino.
     */
    public ElementoDePrueba escanearSiguiente(Ruta ruta, Coordenada actual) {
        Coordenada siguiente = ruta.siguienteDe(actual);
        if (siguiente.equals(actual)) {
            return null;
        }
        return escanear(siguiente);
    }
}
