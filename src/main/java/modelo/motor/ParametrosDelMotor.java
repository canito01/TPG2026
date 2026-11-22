package modelo.motor;

/**
 * Valores del Anexo (Misiones y Motor Warp) en un solo lugar, para cambiarlos
 * sin tocar los estados. Todo en unidades enteras.
 *
 * Decisiones tomadas con el plan (seccion 5): el Warp NO consume combustible
 * (lectura literal del Anexo) y no se aplica desgaste por movimiento ni costos
 * de radar de la Ficha de E1.
 */
public final class ParametrosDelMotor {

    /** Combustible de cada salto simple. */
    public static final int COMBUSTIBLE_SALTO_SIMPLE = 2;
    /** Casillas que avanza un salto simple. */
    public static final int CASILLAS_SALTO_SIMPLE = 1;
    /** Energia que exige (en la preparacion) y consume (al saltar) el salto warp. */
    public static final int ENERGIA_WARP = 5;
    /** Casillas que avanza un salto warp. */
    public static final int CASILLAS_WARP = 2;
    /** Turnos de preparacion antes de poder saltar en warp. */
    public static final int TURNOS_PREPARACION = 2;
    /** Desgaste que genera el turno de enfriamiento. */
    public static final int DESGASTE_ENFRIAMIENTO = 3;

    private ParametrosDelMotor() { }
}
