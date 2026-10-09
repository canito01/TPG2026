package modelo.asistente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conserva eventos válidos en orden temporal. La colección interna no se expone.
 * Los eventos con la misma fecha mantienen su orden de registro.
 */
public final class Bitacora {
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
     * @param evento acontecimiento no nulo
     * @throws IllegalArgumentException si el evento es nulo, sin alterar registros
     */
    public void registrar(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("No se puede registrar un evento nulo.");
        }

        int posicion = 0;
        while (posicion < eventos.size()
                && !eventos.get(posicion).getFecha().isAfter(evento.getFecha())) {
            posicion++;
        }
        eventos.add(posicion, evento);
    }

    /**
     * @return copia de solo lectura en orden temporal; futuras inserciones
     *         no modifican la lista devuelta
     */
    public List<Evento> getEventos() {
        return List.copyOf(eventos);
    }
}
