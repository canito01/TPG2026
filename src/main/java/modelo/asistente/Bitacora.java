package modelo.asistente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import util.observer.Sujeto;

/**
 * Conserva eventos válidos en orden temporal. La colección interna no se expone.
 * Los eventos con la misma fecha mantienen su orden de registro.
 * Cada registro nuevo notifica su descripcion a los observadores.
 */
public final class Bitacora extends Sujeto {
    private final List<Evento> eventos = new ArrayList<>();

    /**
     * Registra un acontecimiento con la fecha y hora actuales.
     * @param descripcion texto válido para un evento
     * @throws IllegalArgumentException si la descripción es inválida;
     *         la bitácora conserva sus registros anteriores
     */
    public void registrar(String descripcion) {
        registrar(new Evento(LocalDateTime.now(), descripcion));
    }

    /**
     * Registra un evento conservando su fecha y el orden temporal de la bitácora.
     * Una vez guardado, notifica su descripcion en el hilo que registra.
     * Los avisos corresponden a nuevas inserciones; para consultar el orden
     * temporal completo se utiliza getEventos().
     * @param evento acontecimiento no nulo
     * @throws IllegalArgumentException si el evento es nulo, sin alterar registros
     */
    public void registrar(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("No se puede registrar un evento nulo.");
        }

        synchronized (eventos) {
            int posicion = 0;
            while (posicion < eventos.size()
                    && !eventos.get(posicion).getFecha().isAfter(evento.getFecha())) {
                posicion++;
            }
            eventos.add(posicion, evento);
        }

        notificarObservadores(evento.getDescripcion());
    }

    /**
     * @return copia de solo lectura en orden temporal; futuras inserciones
     *         no modifican la lista devuelta
     */
    public List<Evento> getEventos() {
        synchronized (eventos) {
            return List.copyOf(eventos);
        }
    }
}
