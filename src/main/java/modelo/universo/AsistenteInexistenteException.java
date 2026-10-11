package modelo.universo;

import modelo.asistente.ExcepcionDominio;

/** No hay ninguna nave (asistente) registrada con ese id. Excepcion de dominio. */
public class AsistenteInexistenteException extends ExcepcionDominio {

    private final String id;

    public AsistenteInexistenteException(String id) {
        super("No existe un asistente para la nave '" + id + "'");
        this.id = id;
    }

    public String getId() { return id; }
}
