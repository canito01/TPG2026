package modelo.misiones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import util.observer.Sujeto;

/**
 * Historial en memoria de los informes finales de mision (no se persiste en base de datos).
 * Es seguro para acceso concurrente entre el hilo de mision y el hilo de Swing,
 * notifica a sus observadores cada vez que se registra un informe y entrega copias inmutables.
 */
public class HistorialDeInformes extends Sujeto {

    private final List<InformeMision> informes = new ArrayList<>();

    /**
     * Registra un informe finalizado y notifica a los observadores fuera del candado.
     * Precondicion: {@code informe != null}.
     */
    public void agregar(InformeMision informe) {
        if (informe == null) {
            throw new IllegalArgumentException("El informe no puede ser nulo.");
        }
        String resumen;
        synchronized (this) {
            informes.add(informe);
            resumen = informe.getNombre() + " — " + (informe.isExitosa() ? "EXITOSA" : "FALLIDA");
        }
        notificarObservadores(resumen);
    }

    /** Devuelve una copia inmutable de todos los informes en orden cronologico. */
    public synchronized List<InformeMision> obtenerTodos() {
        return Collections.unmodifiableList(new ArrayList<>(informes));
    }

    public synchronized int cantidad() {
        return informes.size();
    }

    public synchronized Optional<InformeMision> obtenerUltimo() {
        if (informes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(informes.get(informes.size() - 1));
    }
}
