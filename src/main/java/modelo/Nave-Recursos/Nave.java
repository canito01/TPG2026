package modelo.nave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.motor.MotorWarp;
import modelo.tripulacion.Tripulante;

/**
 * Abstracción común de todas las naves: identidad, tipo, recursos, motor y tripulación.
 *
 * <p>Las naves se crean mediante {@code fabrica.NaveFactory}; el código cliente no debe
 * instanciar las clases concretas directamente.</p>
 *
 * <h2>Invariantes de clase</h2>
 * <ul>
 *   <li>{@code id} no es nulo ni vacío y no cambia nunca.</li>
 *   <li>{@code tipo}, {@code recursos} y {@code motor} no son nulos y no se reemplazan.</li>
 *   <li>La tripulación nunca es nula ni contiene elementos nulos.</li>
 *   <li>Los recursos cumplen siempre las invariantes de {@link Recursos}.</li>
 * </ul>
 *
 * <h2>Encapsulación</h2>
 * <p>Los atributos son privados. {@link #getRecursos()} entrega el componente de recursos, que
 * se protege a sí mismo: toda modificación pasa por sus operaciones validadas, de modo que la
 * nave nunca queda en un estado inválido. La lista de tripulantes se entrega como vista de
 * solo lectura.</p>
 *
 * <p>Esta clase no depende de consola ni de Swing.</p>
 */
public abstract class Nave {

    private final String id;
    private final TipoNave tipo;
    private final Recursos recursos;
    private final MotorWarp motor;
    private final List<Tripulante> tripulacion = new ArrayList<>();

    /**
     * <p><b>Precondición:</b> ningún argumento es nulo y {@code id} no está en blanco.<br>
     * <b>Postcondición:</b> la nave queda con los componentes indicados y sin tripulantes.</p>
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

    /** @return vista de solo lectura de la tripulación; intentar modificarla lanza {@link UnsupportedOperationException} */
    public List<Tripulante> getTripulacion() {
        return Collections.unmodifiableList(tripulacion);
    }

    /**
     * Incorpora un tripulante a la nave.
     *
     * <p><b>Precondición:</b> {@code tripulante != null}.<br>
     * <b>Postcondición:</b> el tripulante queda al final de la tripulación.</p>
     *
     * @throws IllegalArgumentException si el tripulante es nulo
     */
    public void agregarTripulante(Tripulante tripulante) {
        if (tripulante == null) {
            throw new IllegalArgumentException("El tripulante no puede ser nulo");
        }
        tripulacion.add(tripulante);
    }
}
