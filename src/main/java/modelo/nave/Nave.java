package modelo.nave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.elementos.ResultadoEncuentro;
import modelo.motor.MotorWarp;
import modelo.motor.SistemasDeLaNave;
import modelo.navegacion.Coordenada;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.Tripulante;

/**
 * Abstracción común de todas las naves: identidad, tipo, recursos, motor,
 * tripulación, posición actual, base de origen y resolución de encuentros
 * mediante Double Dispatch.
 *
 * Las naves se crean con {@code modelo.fabrica.NaveFactory}; el código cliente
 * no debería instanciar las clases concretas directamente.
 */
public abstract class Nave {

    /** Cantidad mínima de tripulantes requerida por la Ficha (1 capitán + 4 adicionales). */
    public static final int TRIPULACION_MINIMA = 5;

    private final String id;
    private final TipoNave tipo;
    private final Recursos recursos;
    private MotorWarp motor;
    private final List<Tripulante> tripulacion = new ArrayList<>();
    private Coordenada baseDeOrigen;
    private Coordenada posicion;
    private boolean destruida;

    /**
     * Precondición: ningún argumento es nulo y {@code id} no está en blanco.
     * Postcondición: la nave queda con los componentes indicados, en posición (0,0),
     * operativa y sin tripulantes.
     *
     * @throws IllegalArgumentException si se viola la precondición
     */
    protected Nave(String id, TipoNave tipo, Recursos recursos, MotorWarp motor) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la nave no puede ser nulo ni vacío");
        }
        if (tipo == null || recursos == null || motor == null) {
            throw new IllegalArgumentException("El tipo, los recursos y el motor de la nave son obligatorios");
        }
        this.id = id;
        this.tipo = tipo;
        this.recursos = recursos;
        this.motor = motor;
        this.baseDeOrigen = new Coordenada(0, 0);
        this.posicion = this.baseDeOrigen;
        this.destruida = false;
    }

    public String getId() {
        return id;
    }

    public TipoNave getTipo() {
        return tipo;
    }

    public Recursos getRecursos() {
        return recursos;
    }

    public MotorWarp getMotor() {
        return motor;
    }

    /** Conecta el motor con los recursos y la ruta al preparar la nave para navegar. */
    public void conectarMotor(SistemasDeLaNave sistemas) {
        motor = new MotorWarp(sistemas);
    }

    public Coordenada getPosicion() {
        return posicion;
    }

    /**
     * Actualiza la posición actual de la nave en el tablero.
     * Precondición: {@code nuevaPosicion != null}.
     */
    public void setPosicion(Coordenada nuevaPosicion) {
        if (nuevaPosicion == null) {
            throw new IllegalArgumentException("La posición no puede ser nula");
        }
        this.posicion = nuevaPosicion;
    }

    public Coordenada getBaseDeOrigen() {
        return baseDeOrigen;
    }

    /**
     * Define la coordenada de la base de origen de la nave (y posiciona la nave allí).
     * Precondición: {@code base != null}.
     */
    public void setBaseDeOrigen(Coordenada base) {
        if (base == null) {
            throw new IllegalArgumentException("La base de origen no puede ser nula");
        }
        this.baseDeOrigen = base;
        this.posicion = base;
    }

    /** @return vista de solo lectura de la tripulación */
    public List<Tripulante> getTripulacion() {
        return Collections.unmodifiableList(tripulacion);
    }

    /**
     * Incorpora un tripulante a la nave.
     *
     * Precondición: {@code tripulante != null}.
     * Postcondición: el tripulante queda al final de la tripulación.
     *
     * @throws IllegalArgumentException si el tripulante es nulo
     */
    public void agregarTripulante(Tripulante tripulante) {
        if (tripulante == null) {
            throw new IllegalArgumentException("El tripulante no puede ser nulo");
        }
        tripulacion.add(tripulante);
    }

    /**
     * Verifica si la nave cumple la regla de tripulación mínima para iniciar una misión:
     * no estar destruida, tener al menos 5 tripulantes vivos y al menos un capitán vivo.
     */
    public boolean tieneTripulacionValida() {
        if (estaDestruida() || tripulacion.size() < TRIPULACION_MINIMA) {
            return false;
        }
        int vivos = 0;
        boolean hayCapitan = false;
        for (Tripulante t : tripulacion) {
            if (t.estaVivo()) {
                vivos++;
                if (t.getCargo() == Cargo.CAPITAN) {
                    hayCapitan = true;
                }
            }
        }
        return hayCapitan && vivos >= TRIPULACION_MINIMA;
    }

    /**
     * Indica si la nave está destruida (por derrota en batalla o porque sus recursos
     * llegaron a desgaste 100, energía 0 o combustible 0). Si los recursos causaron
     * la destrucción, toda la tripulación muere.
     */
    public boolean estaDestruida() {
        if (!destruida && recursos.estaDestruida()) {
            destruir();
        }
        return destruida;
    }

    /**
     * Destruye la nave y provoca la muerte de toda su tripulación.
     */
    public void destruir() {
        this.destruida = true;
        for (Tripulante t : tripulacion) {
            t.morir();
        }
    }

    // ------------------------------------------------------------------ Double Dispatch (Batallas)

    /**
     * Primer despacho del Double Dispatch: resuelve el encuentro entre esta nave
     * (nave propia) y una nave enemiga, sin usar condicionales de tipo.
     * Si esta nave pierde, es destruida junto con toda su tripulación; si gana,
     * la nave enemiga queda destruida.
     *
     * Precondición: {@code naveEnemiga != null}.
     */
    public final ResultadoEncuentro enfrentarseCon(Nave naveEnemiga) {
        if (naveEnemiga == null) {
            throw new IllegalArgumentException("La nave enemiga no puede ser nula");
        }
        ResultadoEncuentro resultado = despacharEncuentro(naveEnemiga);
        if (resultado == ResultadoEncuentro.PIERDE) {
            this.destruir();
        } else if (resultado == ResultadoEncuentro.GANA) {
            naveEnemiga.destruir();
        }
        return resultado;
    }

    /** Primer paso polimórfico: cada subclase invoca en la enemiga el método de su propio tipo. */
    protected abstract ResultadoEncuentro despacharEncuentro(Nave naveEnemiga);

    /** Segundo despacho cuando la nave propia que inicia el encuentro es una Exploradora. */
    public abstract ResultadoEncuentro lucharContraExploradora(NaveExploradora navePropia);

    /** Segundo despacho cuando la nave propia que inicia el encuentro es un Carguero. */
    public abstract ResultadoEncuentro lucharContraCarguero(NaveCarguero navePropia);

    /** Segundo despacho cuando la nave propia que inicia el encuentro es de Combate. */
    public abstract ResultadoEncuentro lucharContraCombate(NaveCombate navePropia);
}
