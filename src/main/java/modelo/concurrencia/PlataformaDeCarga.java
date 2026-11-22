package modelo.concurrencia;

import java.util.Objects;
import java.util.function.Consumer;

/** Guarda baterías del universo paralelo hasta una capacidad fija. */
public final class PlataformaDeCarga {
    private final int capacidad;
    private final Consumer<String> eventos;
    private int cantidadBaterias;

    /** Crea una plataforma vacía con capacidad mayor que cero. */
    public PlataformaDeCarga(int capacidad) {
        this(capacidad, mensaje -> {});
    }

    /** El receptor encola mensajes y retorna sin esperar ni actualizar la vista. */
    PlataformaDeCarga(int capacidad, Consumer<String> eventos) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        this.capacidad = capacidad;
        this.eventos = Objects.requireNonNull(eventos);
    }

    /**
     * Deposita una batería. Si la plataforma está llena, espera hasta que haya espacio.
     * Si se interrumpe la espera, sale sin depositar.
     */
    public synchronized void depositarBateria() throws InterruptedException {
        String generadora = Thread.currentThread().getName();
        if (cantidadBaterias == capacidad) {
            registrarEvento(generadora + " espera que se libere espacio en la plataforma.");
        }
        while (cantidadBaterias == capacidad) {
            wait();
        }
        cantidadBaterias++;
        notifyAll();
        registrarEvento(generadora + " depositó una batería. " + ocupacion());
    }

    /**
     * Retira una batería. Si la plataforma está vacía, espera hasta que haya alguna.
     * Si se interrumpe la espera, sale sin retirar.
     */
    public synchronized void retirarBateria() throws InterruptedException {
        String nave = Thread.currentThread().getName();
        if (cantidadBaterias == 0) {
            registrarEvento(nave + " espera porque la plataforma está vacía.");
        }
        while (cantidadBaterias == 0) {
            wait();
        }
        cantidadBaterias--;
        notifyAll();
        registrarEvento(nave + " retiró una batería. " + ocupacion());
    }

    public synchronized int getCantidadBaterias() {
        return cantidadBaterias;
    }

    void registrarEvento(String mensaje) {
        eventos.accept(mensaje);
    }

    private String ocupacion() {
        return "Baterías en plataforma: " + cantidadBaterias + "/" + capacidad + ".";
    }
}
