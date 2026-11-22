package modelo.navegacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Camino que debe recorrer la nave: coordenadas consecutivas desde el origen
 * hasta el destino, ambos incluidos. Inmutable.
 *
 * Invariante: no esta vacia y cada coordenada es ortogonalmente adyacente a la
 * anterior (se avanza de a una casilla, sin diagonales).
 */
public final class Ruta {

    private final List<Coordenada> camino;

    /**
     * Pre: camino no nulo, no vacio, sin nulos y con coordenadas consecutivas adyacentes.
     * Si no se cumple: IllegalArgumentException (la ruta no se crea).
     */
    public Ruta(List<Coordenada> camino) {
        if (camino == null || camino.isEmpty()) {
            throw new IllegalArgumentException("La ruta no puede ser nula ni vacia");
        }
        for (int i = 0; i < camino.size(); i++) {
            if (camino.get(i) == null) {
                throw new IllegalArgumentException("La ruta no admite coordenadas nulas");
            }
            if (i > 0 && !camino.get(i - 1).esAdyacenteA(camino.get(i))) {
                throw new IllegalArgumentException("Las coordenadas " + camino.get(i - 1) + " y "
                        + camino.get(i) + " no son adyacentes");
            }
        }
        this.camino = Collections.unmodifiableList(new ArrayList<Coordenada>(camino));
    }

    public Coordenada getOrigen() { return camino.get(0); }

    public Coordenada getDestino() { return camino.get(camino.size() - 1); }

    /** Todas las coordenadas, origen y destino incluidos; lista de solo lectura. */
    public List<Coordenada> getCamino() { return camino; }

    /** Cantidad de casillas que hay que avanzar para ir del origen al destino. */
    public int pasos() { return camino.size() - 1; }

    public boolean contiene(Coordenada c) { return camino.contains(c); }

    /** Pre: contiene(c). Post: posicion de c dentro del camino (0 = origen). */
    public int indiceDe(Coordenada c) {
        int i = camino.indexOf(c);
        if (i < 0) {
            throw new IllegalArgumentException("La coordenada " + c + " no pertenece a la ruta");
        }
        return i;
    }

    /** Pre: contiene(actual). Post: la casilla que sigue; si ya es el destino, devuelve el mismo destino. */
    public Coordenada siguienteDe(Coordenada actual) {
        return avanzarDesde(actual, 1);
    }

    /**
     * Pre: contiene(actual) y casillas >= 0.
     * Post: la coordenada que queda tras avanzar 'casillas' por la ruta; si faltan
     * menos casillas, se detiene en el destino (no se pasa).
     */
    public Coordenada avanzarDesde(Coordenada actual, int casillas) {
        if (casillas < 0) {
            throw new IllegalArgumentException("No se puede avanzar una cantidad negativa de casillas");
        }
        int destino = Math.min(indiceDe(actual) + casillas, camino.size() - 1);
        return camino.get(destino);
    }

    @Override
    public String toString() { return "Ruta" + camino; }
}
