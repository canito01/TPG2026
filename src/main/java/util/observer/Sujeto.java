package util.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Base reutilizable del patron Observer para los objetos del proyecto.
 * Implementacion propia con el contrato de notificaciones de la catedra.
 * Permite suscribir o quitar observadores mientras se emite un aviso:
 * esos cambios se aplican a las siguientes notificaciones.
 */
public abstract class Sujeto {
    private final List<Observador> observadores = new ArrayList<>();

    /** Registra un observador no nulo una sola vez; no envia avisos anteriores. */
    public final void agregarObservador(Observador observador) {
        if (observador == null) {
            throw new IllegalArgumentException("El observador no puede ser nulo.");
        }
        synchronized (observadores) {
            if (!observadores.contains(observador)) {
                observadores.add(observador);
            }
        }
    }

    /** Quita un observador. Si no estaba registrado, no hace nada. */
    public final void quitarObservador(Observador observador) {
        synchronized (observadores) {
            observadores.remove(observador);
        }
    }

    /**
     * Solo el objeto observado puede emitir avisos.
     * Se notifica a una copia del registro, en orden de suscripcion y sin
     * mantener bloqueada la lista durante las llamadas a los observadores.
     */
    protected final void notificarObservadores(String mensaje) {
        List<Observador> destinatarios;
        synchronized (observadores) {
            destinatarios = List.copyOf(observadores);
        }
        for (Observador observador : destinatarios) {
            observador.actualizar(mensaje);
        }
    }
}
