package modelo.concurrencia;

import java.util.Objects;

/** Retira y consume baterías de la plataforma compartida. */
public final class NaveConsumidora implements Runnable {
    private final PlataformaDeCarga plataforma;
    private final long tiempoConsumoMillis;

    public NaveConsumidora(PlataformaDeCarga plataforma, long tiempoConsumoMillis) {
        if (tiempoConsumoMillis <= 0) {
            throw new IllegalArgumentException("El tiempo de consumo debe ser mayor que cero.");
        }
        this.plataforma = Objects.requireNonNull(plataforma, "La plataforma es obligatoria.");
        this.tiempoConsumoMillis = tiempoConsumoMillis;
    }

    @Override
    public void run() {
        String nombre = Thread.currentThread().getName();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                plataforma.registrarEvento(nombre + " solicita una batería.");
                plataforma.retirarBateria();
                // El consumo ocurre después de retirar la batería, fuera del monitor.
                Thread.sleep(tiempoConsumoMillis);
                plataforma.registrarEvento(nombre + " terminó de consumir una batería.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
