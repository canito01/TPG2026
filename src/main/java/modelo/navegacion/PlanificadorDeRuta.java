package modelo.navegacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import modelo.tablero.Tablero;

/**
 * Calcula el camino mas corto entre dos coordenadas con busqueda en anchura
 * (BFS) sobre la grilla del tablero, moviendose en vertical y horizontal.
 *
 * Decision: planifica sobre la grilla sin considerar los elementos, porque se
 * mueven al azar en cada turno; lo que aparezca en el camino lo detecta el
 * radar de corto alcance turno a turno.
 */
public class PlanificadorDeRuta {

    private static final int[] DX = {1, 0, -1, 0};
    private static final int[] DY = {0, 1, 0, -1};

    private final Tablero tablero;

    public PlanificadorDeRuta(Tablero tablero) {
        if (tablero == null) {
            throw new IllegalArgumentException("El tablero no puede ser null");
        }
        this.tablero = tablero;
    }

    /**
     * Pre: origen y destino validos en el tablero (si no: IllegalArgumentException).
     * Post: ruta de longitud minima de origen a destino, ambos incluidos; si son
     * iguales, una ruta de una sola coordenada.
     */
    public Ruta planificar(Coordenada origen, Coordenada destino) {
        if (!tablero.esValida(origen) || !tablero.esValida(destino)) {
            throw new IllegalArgumentException("Origen o destino fuera del tablero: " + origen + " -> " + destino);
        }
        Map<Coordenada, Coordenada> padre = new HashMap<Coordenada, Coordenada>();
        Queue<Coordenada> cola = new LinkedList<Coordenada>();
        padre.put(origen, null);
        cola.add(origen);
        while (!cola.isEmpty() && !padre.containsKey(destino)) {
            Coordenada actual = cola.remove();
            for (int i = 0; i < DX.length; i++) {
                Coordenada vecino = new Coordenada(actual.getX() + DX[i], actual.getY() + DY[i]);
                if (tablero.esValida(vecino) && !padre.containsKey(vecino)) {
                    padre.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        if (!padre.containsKey(destino)) {
            throw new IllegalStateException("No hay camino de " + origen + " a " + destino);
        }
        List<Coordenada> camino = new ArrayList<Coordenada>();
        for (Coordenada c = destino; c != null; c = padre.get(c)) {
            camino.add(c);
        }
        Collections.reverse(camino);
        return new Ruta(camino);
    }
}
