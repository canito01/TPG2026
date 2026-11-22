package modelo.misiones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.asistente.PosicionNave;

/**
 * Informe final de una mision (fase "evaluar"). El estado final de la nave se
 * completa recien en la fase "cerrar", cuando ya se aplicaron las recompensas.
 *
 * @invariant nombre, objetivo, resultado, balanceRecursos y observaciones != null
 * @invariant acciones != null && ruta != null
 */
public class InformeMision {

    private final String nombre;
    private final String objetivo;
    private final boolean exitosa;
    private final String resultado;
    private final List<String> acciones;
    private final List<PosicionNave> ruta;
    private final String balanceRecursos;
    private final String observaciones;
    private String estadoNave;

    /**
     * @pre ningun argumento es null (las listas se copian)
     * @post el informe queda sin estado final de nave hasta que se cierre la mision
     * @throws IllegalArgumentException si algun argumento es null
     */
    protected InformeMision(String nombre, String objetivo, boolean exitosa, String resultado,
                            List<String> acciones, List<PosicionNave> ruta,
                            String balanceRecursos, String estadoNave, String observaciones) {
        if (nombre == null || objetivo == null || resultado == null || acciones == null
                || ruta == null || balanceRecursos == null || estadoNave == null
                || observaciones == null) {
            throw new IllegalArgumentException("El informe no admite datos nulos");
        }
        this.nombre = nombre;
        this.objetivo = objetivo;
        this.exitosa = exitosa;
        this.resultado = resultado;
        this.acciones = new ArrayList<>(acciones);
        this.ruta = new ArrayList<>(ruta);
        this.balanceRecursos = balanceRecursos;
        this.estadoNave = estadoNave;
        this.observaciones = observaciones;
    }

    public String getNombre() { return nombre; }

    public String getObjetivo() { return objetivo; }

    public boolean isExitosa() { return exitosa; }

    public String getResultado() { return resultado; }

    /** @return copia inmutable de las acciones registradas. */
    public List<String> getAcciones() { return Collections.unmodifiableList(acciones); }

    /** @return recorrido real de la nave (posiciones visitadas, en orden). */
    public List<PosicionNave> getRuta() { return Collections.unmodifiableList(ruta); }

    /** @return balance de recursos ganados, consumidos o perdidos. */
    public String getRecursosConsumidos() { return balanceRecursos; }

    public String getEstadoNave() { return estadoNave; }

    public String getObservaciones() { return observaciones; }

    /** Uso interno de Mision: agrega acciones posteriores a la evaluacion (cierre). */
    protected void agregarAccion(String accion) {
        if (accion == null || accion.isBlank()) {
            throw new IllegalArgumentException("Accion vacia");
        }
        acciones.add(accion);
    }

    /** Uso interno de Mision: fija el estado definitivo de la nave tras el cierre. */
    protected void registrarEstadoFinal(String estado) {
        if (estado == null) {
            throw new IllegalArgumentException("Estado final nulo");
        }
        this.estadoNave = estado;
    }
}