package modelo.misiones;

public abstract class Mision {

    private String nombre;
    private String estado;
    private InformeMision informe;
    private boolean estaPreparada;

    protected Mision(String nombre) { this.nombre = nombre; }

    public final InformeMision ejecutarCiclo(AsistenteDeComando a) {
        preparar(a);
        ejecutar(a);
        evaluar(a);
        this.informe = cerrar(a);
        return this.informe;
    }

    /**
     * Comprueba que la nave disponga de los recursos
     *
     * @param a El asistente que coordina la verificación.
     * @post estaPreparada es true si la nave tiene recursos suficientes.
     * @throws IllegalStateException si los recursos no son suficientes.
     */
    protected void preparar(AsistenteDeComando a) {

        a.verificarViabilidad(this.costoCombustible(), this.energiaAdicional(), this.costoDesgaste());
        this.estaPreparada = true;
    }

    /**
     * Ejecuta la misión y delega el consumo de recursos al asistente.
     *
     * @param a El asistente que coordina la ejecución.
     * @pre estaPreparada == true (Una misión no puede ejecutarse sin preparación previa).
     * @throws IllegalStateException si se intenta ejecutar sin haber preparado la misión.
     */
    protected void ejecutar(AsistenteDeComando a) {
        if (!this.estaPreparada) {
            throw new IllegalStateException("Una misión no puede ejecutarse sin preparación previa.");
        }
        a.consumirRecursos(this.costoCombustible(), this.energiaAdicional(), this.costoDesgaste());
    }

    /**
     * Evalúa si el objetivo específico de la misión se cumplió.
     *
     * @param a El asistente de comando.
     * @return true si la misión fue exitosa, false en caso contrario.
     */
    protected abstract boolean evaluar(AsistenteDeComando a);

    /**
     * Cierra la misión y genera el reporte final.
     *
     * @param a El asistente de comando.
     * @return El objeto InformeMision con los datos recopilados.
     * @post Retorna un InformeMision (Una misión no puede cerrarse sin resultado e informe).
     */
    protected abstract InformeMision cerrar(AsistenteDeComando a);

    /**
     * @return La energía extra requerida por el objetivo específico de la misión.
     */
    protected abstract int energiaAdicional();

    /**
     * @return El costo base de combustible para la operación controlada en E1.
     */
    protected int costoCombustible() {
        return 4;
    }

    /**
     * @return El desgaste base generado por la operación controlada en E1.
     */
    protected int costoDesgaste() {
        return 4;
    }

    public String getNombre() {
        return this.nombre;
    }
}
}
