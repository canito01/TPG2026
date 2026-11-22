package app;

import modelo.motor.SistemasDeLaNave;
import modelo.navegacion.Coordenada;
import modelo.navegacion.Ruta;
import modelo.tablero.Tablero;

/**
 * Nave de DEMOSTRACION y de pruebas: implementa SistemasDeLaNave con recursos
 * y posicion en memoria. NO es la Nave de Eneas; sirve para probar el motor, los
 * radares y la vista hasta que esa nave exista (entonces es ella la que
 * implementa SistemasDeLaNave y esta clase se descarta).
 *
 * Invariantes: combustible, energia y desgaste entre 0 y 100.
 */
public class NaveSimulada implements SistemasDeLaNave {

    private final String id;
    private final Tablero tablero;   // opcional: si no es null, se le avisa cada movimiento
    private int combustible;
    private int energia;
    private int desgaste;
    private Coordenada posicion;
    private Ruta ruta;

    /** Pre: recursos entre 0 y 100, posicion valida y (si hay tablero) dentro de el. */
    public NaveSimulada(String id, int combustible, int energia, Coordenada posicion, Tablero tablero) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("El id de la nave no puede ser nulo ni vacio");
        }
        if (posicion == null) {
            throw new IllegalArgumentException("La posicion no puede ser null");
        }
        exigirRango(combustible, "combustible");
        exigirRango(energia, "energia");
        this.id = id;
        this.combustible = combustible;
        this.energia = energia;
        this.posicion = posicion;
        this.tablero = tablero;
        if (tablero != null) {
            tablero.moverNave(posicion);
        }
    }

    public String getId() { return id; }

    public Coordenada getPosicion() { return posicion; }

    public int getDesgaste() { return desgaste; }

    public Ruta getRuta() { return ruta; }

    /** Pre: ruta != null y contiene la posicion actual. */
    public void setRuta(Ruta ruta) {
        if (ruta == null || !ruta.contiene(posicion)) {
            throw new IllegalArgumentException("La ruta debe contener la posicion actual " + posicion);
        }
        this.ruta = ruta;
    }

    /** Para simular ganar o perder energia entre turnos (estacion, asteroide). Pre: 0 a 100. */
    public void setEnergia(int energia) {
        exigirRango(energia, "energia");
        this.energia = energia;
    }

    public boolean haLlegado() {
        return ruta != null && posicion.equals(ruta.getDestino());
    }

    @Override
    public int getCombustible() { return combustible; }

    @Override
    public int getEnergia() { return energia; }

    @Override
    public void consumirCombustible(int cantidad) {
        if (cantidad <= 0 || cantidad > combustible) {
            throw new IllegalArgumentException("No se pueden consumir " + cantidad + " de combustible (hay " + combustible + ")");
        }
        combustible -= cantidad;
    }

    @Override
    public void consumirEnergia(int cantidad) {
        if (cantidad <= 0 || cantidad > energia) {
            throw new IllegalArgumentException("No se pueden consumir " + cantidad + " de energia (hay " + energia + ")");
        }
        energia -= cantidad;
    }

    @Override
    public void agregarDesgaste(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("El desgaste a agregar debe ser positivo");
        }
        desgaste = Math.min(100, desgaste + cantidad);
    }

    @Override
    public int avanzar(int casillas) {
        if (casillas < 1) {
            throw new IllegalArgumentException("Hay que avanzar al menos una casilla");
        }
        if (ruta == null) {
            throw new IllegalStateException("La nave no tiene ruta");
        }
        Coordenada nueva = ruta.avanzarDesde(posicion, casillas);
        int movidas = ruta.indiceDe(nueva) - ruta.indiceDe(posicion);
        posicion = nueva;
        if (tablero != null) {
            tablero.moverNave(posicion);
        }
        return movidas;
    }

    private static void exigirRango(int valor, String nombre) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(nombre + " fuera de rango (0 a 100): " + valor);
        }
    }
}
