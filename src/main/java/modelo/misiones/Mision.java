package modelo.misiones;

import java.util.ArrayList;
import java.util.List;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.Resultado;

public abstract class Mision {

    private final String nombre;
    private InformeMision informe;
    private boolean estaPreparada = false;
    private boolean operacionCompletada = false;
    private boolean exito = false;
    private final List<String> acciones = new ArrayList<>();

    protected Mision(String nombre) {
        this.nombre = nombre;
    }

    public final InformeMision ejecutarCiclo(AsistenteDeComando a) {
        acciones.clear();
        operacionCompletada = false;
        exito = false;
        try {
            preparar(a);
            ejecutar(a);
            this.exito = evaluar(a);
            this.informe = cerrar(a);
            return this.informe;
        } finally {
            this.estaPreparada = false;
        }
    }

    /**
     * Prepara la mision para su ejecucion.
     * La verificacion de recursos no se hace aca: la realiza de forma atomica
     *
     * @param a El asistente de comando que coordina la mision.
     * @post estaPreparada == true
     * @post se registra la accion "preparada" en la lista de acciones
     */
    protected void preparar(AsistenteDeComando a) {
        this.estaPreparada = true;
        acciones.add("Mision " + nombre + " preparada");
    }

    /**
     * Ejecuta la mision consumiendo los recursos de forma atomica a traves del asistente.
     *
     * @param a El asistente de comando que coordina la ejecucion.
     * @pre estaPreparada == true
     * @post si tuvo exito, los recursos de la nave fueron consumidos y operacionCompletada == true
     * @throws IllegalStateException si no se preparo la mision o si los recursos son insuficientes
     *         (en ese caso la nave no sufre cambios parciales).
     */
    protected void ejecutar(AsistenteDeComando a) {
        if (!this.estaPreparada) {
            throw new IllegalStateException("Una mision no puede ejecutarse sin preparacion previa.");
        }
        Resultado res = a.consumir(costoCombustible(), energiaAdicional(), costoDesgaste());
        if (!res.fueExito()) {
            throw new IllegalStateException("Recursos insuficientes: " + res.getMensaje());
        }
        this.operacionCompletada = true;
        acciones.add("Mision " + nombre + " ejecutada (recursos consumidos)");
    }

    /**
     * Evalua si el objetivo especifico de la mision se cumplio.
     * El resultado queda disponible para {@link #cerrar(AsistenteDeComando)}
     * mediante {@link #fueExitosa()}.
     *
     * @param a El asistente de comando.
     * @return true si la mision fue exitosa, false en caso contrario.
     */
    protected abstract boolean evaluar(AsistenteDeComando a);

    /**
     * Cierra la mision y genera el reporte final.
     *
     * @param a El asistente de comando.
     * @return El objeto InformeMision con los datos recopilados.
     * @post Retorna un InformeMision (una mision no puede cerrarse sin resultado e informe).
     */
    protected abstract InformeMision cerrar(AsistenteDeComando a);

    /** @return La energia extra requerida por el objetivo especifico de la mision. */
    protected abstract int energiaAdicional();

    /** @return El costo base de combustible para la operacion controlada en E1. */
    protected int costoCombustible() {
        return 4;
    }

    /** @return El desgaste base generado por la operacion controlada en E1. */
    protected int costoDesgaste() {
        return 4;
    }

    // ---- Apoyo para las subclases ----

    /** Permite a las subclases agregar acciones a la lista de la mision. */
    protected void registrarAccion(String accion) {
        acciones.add(accion);
    }

    /** @return copia de las acciones registradas hasta el momento. */
    protected List<String> getAcciones() {
        return new ArrayList<>(acciones);
    }

    /** @return true si la operacion controlada se completo (recursos consumidos). */
    protected boolean operacionCompletada() {
        return operacionCompletada;
    }

    /** @return resultado de la ultima evaluacion. */
    protected boolean fueExitosa() {
        return exito;
    }

    /** @return resumen de los recursos consumidos por la mision. */
    protected String resumenRecursosConsumidos() {
        return "Combustible: " + costoCombustible()
                + ", Energia: " + energiaAdicional()
                + ", Desgaste: " + costoDesgaste();
    }

    public String getNombre() {
        return this.nombre;
    }
}
