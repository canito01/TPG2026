package modelo.asistente;

import modelo.navegacion.Coordenada;

/** Coordenadas del tablero. Los límites se comprueban en el subsistema de navegación. */
public record PosicionNave(int fila, int columna) {
    static PosicionNave desde(Coordenada coordenada) {
        return new PosicionNave(coordenada.getY(), coordenada.getX());
    }

    Coordenada aCoordenada() {
        return new Coordenada(columna, fila);
    }
}
