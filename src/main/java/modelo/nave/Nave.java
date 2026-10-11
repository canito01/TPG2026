package modelo.nave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.motor.MotorWarp;
import modelo.tripulacion.Tripulante;

/**
 * Abstraccion común de todas las naves: identidad, tipo, recursos, motor y tripulación.
 *
 * Las naves se crean con{@code fabrica.NaveFactory}; el código cliente no deberia
 * instanciar las clases concretas directamente.
 */
public abstract class Nave {

    private final String id;
    private final TipoNave tipo;
    private final Recursos recursos;
    private final MotorWarp motor;
    private final List<Tripulante> tripulacion = new ArrayList<>();

    /**
     * Precondición: ningún argumento es nulo y {@code id} no está en blanco.
     * Postcondición: la nave queda con los componentes indicados y sin tripulantes.
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
}
