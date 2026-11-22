package modelo.navegacion;

import modelo.tablero.Tablero;

/**
 * Radar de largo alcance (Fase 1 'preparar' de la mision): calcula el camino
 * hacia el objetivo y lo ALMACENA para consultarlo durante la travesia.
 */
public class RadarLargoAlcance {

    private final PlanificadorDeRuta planificador;
    private Ruta rutaActual;   // null hasta el primer calculo

    public RadarLargoAlcance(Tablero tablero) {
        this.planificador = new PlanificadorDeRuta(tablero);
    }

    /**
     * Pre: origen y destino validos. Post: calcula, guarda y devuelve la ruta
     * minima; reemplaza a la guardada antes.
     */
    public Ruta calcularRuta(Coordenada origen, Coordenada destino) {
        rutaActual = planificador.planificar(origen, destino);
        return rutaActual;
    }

    /** Post: la ultima ruta calculada o null si nunca se calculo una. */
    public Ruta getRutaActual() { return rutaActual; }
}
