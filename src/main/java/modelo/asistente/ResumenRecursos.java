package modelo.asistente;

/**
 * Datos inmutables de una consulta de recursos. No consume ni carga recursos.
 * La implementación del asistente debe copiar los valores del subsistema,
 * incluida la indicación de mantenimiento, en el momento de la consulta.
 */
public final class ResumenRecursos {
    private final int combustible;
    private final int energia;
    private final int desgaste;
    private final boolean requiereMantenimiento;

    /**
     * Construye una consulta con los datos de la nave.
     * @param combustible valor actual entre 0 y 100
     * @param energia valor actual entre 0 y 100
     * @param desgaste valor actual entre 0 y 100
     * @param requiereMantenimiento indicación actual del subsistema de recursos
     * @throws IllegalArgumentException si un valor queda fuera de rango
     */
    public ResumenRecursos(int combustible, int energia, int desgaste,
                           boolean requiereMantenimiento) {
        if (combustible < 0 || combustible > 100
                || energia < 0 || energia > 100
                || desgaste < 0 || desgaste > 100) {
            throw new IllegalArgumentException("Los recursos deben estar entre 0 y 100.");
        }
        this.combustible = combustible;
        this.energia = energia;
        this.desgaste = desgaste;
        this.requiereMantenimiento = requiereMantenimiento;
    }

    public int getCombustible() {
        return combustible;
    }

    public int getEnergia() {
        return energia;
    }

    public int getDesgaste() {
        return desgaste;
    }

    public boolean requiereMantenimiento() {
        return requiereMantenimiento;
    }
}
