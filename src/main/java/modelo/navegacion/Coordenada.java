package modelo.navegacion;

/**
 * Posicion (x, y) en el tablero. Inmutable: dos coordenadas con los mismos
 * valores son iguales (equals/hashCode), asi sirve de clave en mapas y de
 * elemento en rutas. No sabe de los limites del tablero: eso lo decide Tablero.
 */
public final class Coordenada {

    private final int x;
    private final int y;

    public Coordenada(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }

    public int getY() { return y; }

    /** Post: true si la otra esta exactamente a una casilla en horizontal o vertical (movimiento ortogonal). */
    public boolean esAdyacenteA(Coordenada otra) {
        return otra != null && Math.abs(x - otra.x) + Math.abs(y - otra.y) == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coordenada)) {
            return false;
        }
        Coordenada c = (Coordenada) o;
        return x == c.x && y == c.y;
    }

    @Override
    public int hashCode() { return 31 * x + y; }

    @Override
    public String toString() { return "(" + x + "," + y + ")"; }
}
