package modelo.tablero;

import modelo.navegacion.Coordenada;

/**
 * Foto del tablero en un instante, para dibujarlo. Es una COPIA inmutable que
 * solo guarda textos (el nombre de cada elemento) y coordenadas: la vista no
 * toca ningun objeto vivo del modelo, asi no hay carreras entre el hilo de la
 * mision y el hilo de Swing.
 */
public final class InstantaneaDelTablero {

    private final String[][] etiquetas;   // [x][y]; null = casilla vacia
    private final Coordenada nave;
    private final Coordenada destino;

    /** Pre: etiquetas es una grilla cuadrada. Copia profundamente lo que recibe. */
    public InstantaneaDelTablero(String[][] etiquetas, Coordenada nave, Coordenada destino) {
        int n = etiquetas.length;
        this.etiquetas = new String[n][n];
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                this.etiquetas[x][y] = etiquetas[x][y];
            }
        }
        this.nave = nave;
        this.destino = destino;
    }

    public int getDimension() { return etiquetas.length; }

    /** Pre: 0 <= x, y < dimension. Post: nombre del elemento en esa casilla o null si esta vacia. */
    public String getEtiquetaEn(int x, int y) { return etiquetas[x][y]; }

    /** Marca de la nave del jugador, o null. */
    public Coordenada getNave() { return nave; }

    /** Destino marcado, o null. */
    public Coordenada getDestino() { return destino; }

    public int cantidadDeElementos() {
        int total = 0;
        for (int x = 0; x < etiquetas.length; x++) {
            for (int y = 0; y < etiquetas.length; y++) {
                if (etiquetas[x][y] != null) {
                    total++;
                }
            }
        }
        return total;
    }

    /**
     * Dibujo en texto para consola: '.' vacia, inicial del elemento, '@' la nave
     * y 'X' el destino si esta libre. Una fila de texto por cada y.
     */
    public String comoTexto() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < etiquetas.length; y++) {
            for (int x = 0; x < etiquetas.length; x++) {
                char c = '.';
                if (etiquetas[x][y] != null) {
                    c = etiquetas[x][y].charAt(0);
                }
                if (destino != null && destino.getX() == x && destino.getY() == y && c == '.') {
                    c = 'X';
                }
                if (nave != null && nave.getX() == x && nave.getY() == y) {
                    c = '@';
                }
                sb.append(c);
            }
            if (y < etiquetas.length - 1) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
