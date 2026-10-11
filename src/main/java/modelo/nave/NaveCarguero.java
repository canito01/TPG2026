package modelo.nave;

import modelo.motor.MotorWarp;

/** Nave carguero: combustible 100, energía 60, desgaste 0. */
public class NaveCarguero extends Nave {

    public static final int COMBUSTIBLE_INICIAL = 100;
    public static final int ENERGIA_INICIAL = 60;
    public static final int DESGASTE_INICIAL = 0;

    /** Usar NaveFactory en lugar de invocar este constructor desde código cliente. */
    public NaveCarguero(String id, MotorWarp motor) {
        super(id, TipoNave.CARGUERO,
                new Recursos(COMBUSTIBLE_INICIAL, ENERGIA_INICIAL, DESGASTE_INICIAL), motor);
    }
}
