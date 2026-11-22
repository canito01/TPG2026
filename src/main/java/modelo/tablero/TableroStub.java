package modelo.tablero;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import modelo.navegacion.Coordenada;
import util.observer.Sujeto;

/**
 * Tablero de prueba (Fase 0 del plan): implementa Tablero con una grilla en
 * memoria y un Random CON SEMILLA, asi toda corrida es reproducible. Sirve
 * hasta que llegue la documentacion de Destino y se escriba el adaptador.
 *
 * Seguridad para hilos: cada operacion toma el candado del tablero y los avisos
 * a los observadores se dan DESPUES de soltarlo.
 *
 * Invariante: cada casilla tiene a lo sumo un elemento.
 */
public class TableroStub extends Sujeto implements Tablero {

    private static final String EVENTO = "tablero";
    private static final int[] DX = {1, 0, -1, 0};   // derecha, abajo, izquierda, arriba
    private static final int[] DY = {0, 1, 0, -1};

    private final ElementoDePrueba[][] celdas = new ElementoDePrueba[DIMENSION][DIMENSION];  // [x][y]
    private final Random azar;
    private Coordenada nave;
    private Coordenada destino;

    public TableroStub(long semilla) {
        this.azar = new Random(semilla);
    }

    /**
     * Tablero con elementos de prueba en casillas libres al azar, sin ocupar las
     * esquinas (0,0) y (19,19) (puntos de partida y llegada tipicos).
     * Pre: la suma de elementos no supera 100. Si no: IllegalArgumentException.
     */
    public static TableroStub conElementosDePrueba(long semilla, int asteroides, int estaciones, int navesEnemigas) {
        if (asteroides < 0 || estaciones < 0 || navesEnemigas < 0 || asteroides + estaciones + navesEnemigas > 100) {
            throw new IllegalArgumentException("Cantidad de elementos invalida (0 a 100 en total)");
        }
        TableroStub t = new TableroStub(semilla);
        t.poblar("Asteroide", asteroides);
        t.poblar("EstacionDeCarga", estaciones);
        t.poblar("NaveEnemiga", navesEnemigas);
        return t;
    }

    private void poblar(String nombre, int cantidad) {
        int colocados = 0;
        while (colocados < cantidad) {
            Coordenada c = new Coordenada(azar.nextInt(DIMENSION), azar.nextInt(DIMENSION));
            boolean esquina = (c.getX() == 0 && c.getY() == 0) || (c.getX() == DIMENSION - 1 && c.getY() == DIMENSION - 1);
            if (!esquina && celdas[c.getX()][c.getY()] == null) {
                celdas[c.getX()][c.getY()] = new ElementoDePrueba(nombre);
                colocados++;
            }
        }
    }

    @Override
    public boolean esValida(Coordenada c) {
        return c != null && c.getX() >= 0 && c.getX() < DIMENSION && c.getY() >= 0 && c.getY() < DIMENSION;
    }

    @Override
    public synchronized ElementoDePrueba getElementoEn(Coordenada c) {
        exigirValida(c);
        return celdas[c.getX()][c.getY()];
    }

    @Override
    public void colocar(ElementoDePrueba elemento, Coordenada c) {
        synchronized (this) {
            if (elemento == null) {
                throw new IllegalArgumentException("El elemento no puede ser null");
            }
            exigirValida(c);
            if (celdas[c.getX()][c.getY()] != null) {
                throw new IllegalArgumentException("La casilla " + c + " ya esta ocupada");
            }
            celdas[c.getX()][c.getY()] = elemento;
        }
        notificarObservadores(EVENTO);
    }

    @Override
    public ElementoDePrueba retirar(Coordenada c) {
        ElementoDePrueba retirado;
        synchronized (this) {
            exigirValida(c);
            retirado = celdas[c.getX()][c.getY()];
            celdas[c.getX()][c.getY()] = null;
        }
        notificarObservadores(EVENTO);
        return retirado;
    }

    @Override
    public void moverElementos() {
        synchronized (this) {
            // Posiciones de los elementos al empezar el turno, en orden fijo (reproducible).
            List<Coordenada> posiciones = new ArrayList<Coordenada>();
            for (int y = 0; y < DIMENSION; y++) {
                for (int x = 0; x < DIMENSION; x++) {
                    if (celdas[x][y] != null) {
                        posiciones.add(new Coordenada(x, y));
                    }
                }
            }
            for (int i = 0; i < posiciones.size(); i++) {
                Coordenada p = posiciones.get(i);
                int dir = azar.nextInt(4);
                Coordenada d = new Coordenada(p.getX() + DX[dir], p.getY() + DY[dir]);
                if (esValida(d) && celdas[d.getX()][d.getY()] == null) {
                    celdas[d.getX()][d.getY()] = celdas[p.getX()][p.getY()];
                    celdas[p.getX()][p.getY()] = null;
                }
            }
        }
        notificarObservadores(EVENTO);
    }

    @Override
    public void moverNave(Coordenada c) {
        synchronized (this) {
            exigirValida(c);
            nave = c;
        }
        notificarObservadores(EVENTO);
    }

    @Override
    public synchronized Coordenada getPosicionNave() { return nave; }

    @Override
    public void marcarDestino(Coordenada c) {
        synchronized (this) {
            exigirValida(c);
            destino = c;
        }
        notificarObservadores(EVENTO);
    }

    @Override
    public synchronized Coordenada getDestino() { return destino; }

    @Override
    public synchronized InstantaneaDelTablero copiaParaDibujar() {
        String[][] etiquetas = new String[DIMENSION][DIMENSION];
        for (int x = 0; x < DIMENSION; x++) {
            for (int y = 0; y < DIMENSION; y++) {
                if (celdas[x][y] != null) {
                    etiquetas[x][y] = celdas[x][y].getNombre();
                }
            }
        }
        return new InstantaneaDelTablero(etiquetas, nave, destino);
    }

    private void exigirValida(Coordenada c) {
        if (!esValida(c)) {
            throw new IllegalArgumentException("Coordenada fuera del tablero: " + c);
        }
    }
}
