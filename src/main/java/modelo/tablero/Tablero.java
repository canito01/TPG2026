package modelo.tablero;

import modelo.navegacion.Coordenada;
import util.observer.Observador;

/**
 * Tablero del universo: grilla de 20 x 20. El modelo depende SIEMPRE de esta
 * interfaz, nunca de la clase Destino de la catedra: cuando llegue su
 * documentacion se escribe un adaptador que implemente Tablero y nada mas cambia
 * (el TableroStub sirve mientras tanto).
 *
 * Observable: avisa a sus observadores cada vez que algo cambia (la vista se
 * suscribe y dibuja una copia, nunca la estructura viva). Todas las operaciones
 * son seguras para hilos: la mision cambia el tablero desde su propio hilo.
 *
 * Una casilla guarda a lo sumo un elemento. La nave del jugador NO es un
 * elemento: el tablero solo recuerda su posicion para poder dibujarla (la
 * posicion verdadera es la de la Nave).
 */
public interface Tablero {

    int DIMENSION = 20;

    /** Post: true si c no es null y esta dentro de la grilla (0 a 19 en x e y). */
    boolean esValida(Coordenada c);

    /** Pre: esValida(c). Post: el elemento de esa casilla o null si esta vacia. */
    ElementoDePrueba getElementoEn(Coordenada c);

    /** Pre: elemento != null, esValida(c) y casilla libre. Post: el elemento queda en c. */
    void colocar(ElementoDePrueba elemento, Coordenada c);

    /** Pre: esValida(c). Post: la casilla queda vacia; devuelve lo que habia (o null). */
    ElementoDePrueba retirar(Coordenada c);

    /**
     * Turno del tablero: mueve al azar los elementos (una casilla ortogonal, solo
     * hacia casillas libres). Post: la cantidad de elementos no cambia.
     */
    void moverElementos();

    /** Pre: esValida(c). Post: la marca de la nave del jugador queda en c. */
    void moverNave(Coordenada c);

    /** Post: la marca de la nave, o null si todavia no se coloco. */
    Coordenada getPosicionNave();

    /** Pre: esValida(c). Post: el destino de la mision queda marcado en c. */
    void marcarDestino(Coordenada c);

    /** Post: el destino marcado, o null. */
    Coordenada getDestino();

    /** Post: copia independiente para dibujar; cambios posteriores del tablero no la afectan. */
    InstantaneaDelTablero copiaParaDibujar();

    void agregarObservador(Observador o);

    void quitarObservador(Observador o);
}
