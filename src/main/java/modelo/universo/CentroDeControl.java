package modelo.universo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.asistente.AsistenteDeComando;

/**
 * Centro de control del universo (R1, R2, UML paquete universo): punto de
 * entrada del sistema.
 *
 * - Registra las naves listas para operar POR SU ABSTRACCION: guarda
 *   AsistenteDeComando (cada asistente opera una sola nave y se identifica con
 *   su id de nave). Una variante nueva de asistente se registra sin tocar esta
 *   clase, y el centro nunca hace new de lo que guarda: no depende de como se
 *   construyen esos objetos (Liskov / inversion de dependencias).
 * - Devuelve cualquiera por id.
 * - Permite usar UNA sola nave a la vez aunque haya varias registradas.
 * - No usa consola: informa con valores de retorno y excepciones de dominio.
 *
 * Invariante de clase: si hay asistente activo, esta registrado.
 */
public class CentroDeControl {

    private final Map<String, AsistenteDeComando> asistentes = new LinkedHashMap<String, AsistenteDeComando>();
    private AsistenteDeComando activo;   // null mientras no se seleccione ninguno

    public CentroDeControl() {
        assert invariante() : "Fallo invariante";
    }

    /**
     * Registra un asistente (la nave lista para operar).
     * Pre: a != null y su getNaveId() no es null ni vacio. Si no se cumple:
     *   IllegalArgumentException (error de programacion); el centro no cambia.
     * Post (exito): queda registrado y se puede obtener por su id; la nave
     *   activa no cambia.
     * Excepcion: AsistenteDuplicadoException si ya hay uno con ese id
     *   (el centro queda sin cambios).
     */
    public void registrar(AsistenteDeComando a) {
        if (a == null) {
            throw new IllegalArgumentException("El asistente no puede ser null");
        }
        String id = a.getNaveId();
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("El id de la nave no puede ser nulo ni vacio");
        }
        if (asistentes.containsKey(id)) {
            throw new AsistenteDuplicadoException(id);
        }
        asistentes.put(id, a);
        assert invariante() : "Fallo invariante";
    }

    /**
     * Devuelve el asistente de la nave con ese id (por su abstraccion).
     * Excepcion: AsistenteInexistenteException si no hay ninguno.
     */
    public AsistenteDeComando obtener(String id) {
        AsistenteDeComando a = asistentes.get(id);
        if (a == null) {
            throw new AsistenteInexistenteException(id);
        }
        return a;
    }

    /**
     * Hace activa a la nave con ese id; la anterior deja de serlo.
     * Post (exito): getActivo() es ese asistente.
     * Excepcion: AsistenteInexistenteException; la nave activa anterior se conserva.
     */
    public void seleccionarActivo(String id) {
        AsistenteDeComando a = obtener(id);
        activo = a;
        assert invariante() : "Fallo invariante";
    }

    /** Pre: hayActivo(). Post: devuelve el unico asistente activo. */
    public AsistenteDeComando getActivo() {
        assert hayActivo() : "No hay nave activa";
        return activo;
    }

    public boolean hayActivo() { return activo != null; }

    public boolean existe(String id) { return asistentes.containsKey(id); }

    public int cantidad() { return asistentes.size(); }

    /** Ids de nave en orden de registro; lista de solo lectura. */
    public List<String> ids() {
        return Collections.unmodifiableList(new ArrayList<String>(asistentes.keySet()));
    }

    private boolean invariante() {
        return activo == null || asistentes.get(activo.getNaveId()) == activo;
    }
}
