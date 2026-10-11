package modelo.nave;

import modelo.motor.MotorWarp;

/** Nave exploradora: combustible 60, energía 80, desgaste 0. */
public class NaveExploradora extends Nave {

    public static final int COMBUSTIBLE_INICIAL = 60;
    public static final int ENERGIA_INICIAL = 80;
    public static final int DESGASTE_INICIAL = 0;

    /** Usar {@code NaveFactory} en lugar de invocar este constructor desde código cliente. */
    public NaveExploradora(String id, MotorWarp motor) {
        super(id, TipoNave.EXPLORADORA,
                new Recursos(COMBUSTIBLE_INICIAL, ENERGIA_INICIAL, DESGASTE_INICIAL), motor);
    }
}
