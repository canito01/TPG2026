package modelo.concurrencia;

import java.util.Objects;

/** Produce una batería por ciclo y la deposita en la plataforma compartida. */
public final class GeneradoraDeEnergia implements Runnable {
    private final PlataformaDeCarga plataforma;
    private final long tiempoProduccionMillis;

    public GeneradoraDeEnergia(PlataformaDeCarga plataforma, long tiempoProduccionMillis) {
        if (tiempoProduccionMillis <= 0) {
            throw new IllegalArgumentException("El tiempo de producción debe ser mayor que cero.");
        }
        this.plataforma = Objects.requireNonNull(plataforma, "La plataforma es obligatoria.");
        this.tiempoProduccionMillis = tiempoProduccionMillis;
    }

    @Override
    public void run() {
        String nombre = Thread.currentThread().getName();
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(tiempoProduccionMillis);
                plataforma.registrarEvento(nombre + " produjo una batería.");
                // Si está llena, la generadora espera con esta batería ya producida.
                plataforma.depositarBateria();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
