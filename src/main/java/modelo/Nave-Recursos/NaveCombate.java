package modelo.nave;

import modelo.motor.MotorWarp;

/** Nave de combate. Configuración inicial: combustible 80, energía 100, desgaste 0. */
public class NaveCombate extends Nave {

    public static final int COMBUSTIBLE_INICIAL = 80;
    public static final int ENERGIA_INICIAL = 100;
    public static final int DESGASTE_INICIAL = 0;

    /** Usar {@code NaveFactory} en lugar de invocar este constructor desde código cliente. */
    public NaveCombate(String id, MotorWarp motor) {
        super(id, TipoNave.COMBATE,
                new Recursos(COMBUSTIBLE_INICIAL, ENERGIA_INICIAL, DESGASTE_INICIAL), motor);
    }
}
