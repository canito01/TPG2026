package modelo.nave;

import modelo.elementos.ResultadoEncuentro;
import modelo.motor.MotorWarp;

/**
 * Nave carguero: combustible 100, energía 60, desgaste 0.
 *
 * Tabla de encuentros (cuando esta nave es la enemiga):
 * - Propia Exploradora vs. esta Carguera -> GANA   (la exploradora propia gana)
 * - Propia Carguera    vs. esta Carguera -> PACTO
 * - Propia Combate     vs. esta Carguera -> PIERDE (la de combate propia pierde)
 */
public class NaveCarguero extends Nave {

    public static final int COMBUSTIBLE_INICIAL = 100;
    public static final int ENERGIA_INICIAL = 60;
    public static final int DESGASTE_INICIAL = 0;

    /** Usar {@code NaveFactory} en lugar de invocar este constructor desde código cliente. */
    public NaveCarguero(String id, MotorWarp motor) {
        super(id, TipoNave.CARGUERO,
                new Recursos(COMBUSTIBLE_INICIAL, ENERGIA_INICIAL, DESGASTE_INICIAL), motor);
    }

    @Override
    protected ResultadoEncuentro despacharEncuentro(Nave naveEnemiga) {
        return naveEnemiga.lucharContraCarguero(this);
    }

    @Override
    public ResultadoEncuentro lucharContraExploradora(NaveExploradora navePropia) {
        return ResultadoEncuentro.GANA;
    }

    @Override
    public ResultadoEncuentro lucharContraCarguero(NaveCarguero navePropia) {
        return ResultadoEncuentro.PACTO;
    }

    @Override
    public ResultadoEncuentro lucharContraCombate(NaveCombate navePropia) {
        return ResultadoEncuentro.PIERDE;
    }
}
