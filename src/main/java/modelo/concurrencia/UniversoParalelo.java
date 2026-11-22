package modelo.concurrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import util.observer.Sujeto;

/** Crea las entidades del visor y publica los eventos de su plataforma compartida. */
public final class UniversoParalelo extends Sujeto implements AutoCloseable {
    private static final int LIMITE_ENTIDADES = 20;

    private final PlataformaDeCarga plataforma;
    private final long tiempoProduccionMillis;
    private final long tiempoConsumoMillis;
    private final List<Thread> entidades = new ArrayList<>();
    private final ExecutorService avisos = Executors.newSingleThreadExecutor(
            tarea -> new Thread(tarea, "Avisos del universo paralelo"));
    private int generadoras;
    private int naves;
    private boolean cerrado;

    public UniversoParalelo(int capacidad, long tiempoProduccionMillis, long tiempoConsumoMillis) {
        if (tiempoProduccionMillis <= 0 || tiempoConsumoMillis <= 0) {
            throw new IllegalArgumentException("Los tiempos deben ser mayores que cero.");
        }
        this.plataforma = new PlataformaDeCarga(capacidad, this::publicarEvento);
        this.tiempoProduccionMillis = tiempoProduccionMillis;
        this.tiempoConsumoMillis = tiempoConsumoMillis;
    }

    public synchronized void agregarGeneradora() {
        verificarQuePuedeAgregar();
        iniciar(new GeneradoraDeEnergia(plataforma, tiempoProduccionMillis),
                "Generadora " + ++generadoras);
    }

    public synchronized void agregarNave() {
        verificarQuePuedeAgregar();
        iniciar(new NaveConsumidora(plataforma, tiempoConsumoMillis),
                "Nave " + ++naves);
    }

    private void verificarQuePuedeAgregar() {
        if (cerrado) {
            throw new IllegalStateException("La simulación ya está cerrada.");
        }
        if (entidades.size() >= LIMITE_ENTIDADES) {
            throw new IllegalStateException("Se alcanzó el límite de " + LIMITE_ENTIDADES + " entidades.");
        }
    }

    private void iniciar(Runnable actividad, String nombre) {
        Thread hilo = new Thread(actividad, nombre);
        entidades.add(hilo);
        publicarEvento(nombre + " ingresó al universo paralelo.");
        hilo.start();
    }

    private void publicarEvento(String mensaje) {
        // Un solo hilo entrega los avisos en el orden en que se encolan.
        // La plataforma puede registrar eventos bajo su monitor sin llamar a la vista.
        try {
            avisos.execute(() -> notificarObservadores(mensaje));
        } catch (RejectedExecutionException e) {
            if (!avisos.isShutdown()) {
                throw e;
            }
        }
    }

    /** Interrumpe las entidades y el hilo de avisos sin bloquear el hilo de Swing. */
    @Override
    public synchronized void close() {
        if (cerrado) {
            return;
        }
        cerrado = true;
        for (Thread hilo : entidades) {
            hilo.interrupt();
        }
        entidades.clear();
        avisos.shutdownNow();
    }
}
