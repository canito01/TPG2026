package modelo.universo;

import modelo.asistente.ExcepcionDominio;

/** Ya hay una nave (asistente) registrada con ese id. Excepcion de dominio. */
public class AsistenteDuplicadoException extends ExcepcionDominio {

    private final String id;

    public AsistenteDuplicadoException(String id) {
        super("Ya existe un asistente para la nave '" + id + "'");
        this.id = id;
    }

    public String getId() { return id; }
}
