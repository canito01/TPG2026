package modelo.nave;

import modelo.elementos.ResultadoEncuentro;
import modelo.motor.MotorWarp;

/**
 * Nave exploradora: combustible 60, energía 80, desgaste 0.
 *
 * Tabla de encuentros (cuando esta nave es la enemiga):
 * - Propia Exploradora vs. esta Exploradora -> PACTO
 * - Propia Carguera    vs. esta Exploradora -> PIERDE (la carguera propia pierde)
 * - Propia Combate     vs. esta Exploradora -> GANA   (la de combate propia gana)
 */
public class NaveExploradora extends Nave {

    public static final int COMBUSTIBLE_INICIAL = 60;
    public static final int ENERGIA_INICIAL = 80;
    public static final int DESGASTE_INICIAL = 0;

    /** Usar {@code NaveFactory} en lugar de invocar este constructor desde código cliente. */
    public NaveExploradora(String id, MotorWarp motor) {
        super(id, TipoNave.EXPLORADORA,
                new Recursos(COMBUSTIBLE_INICIAL, ENERGIA_INICIAL, DESGASTE_INICIAL), motor);
    }

    @Override
    protected ResultadoEncuentro despacharEncuentro(Nave naveEnemiga) {
        return naveEnemiga.lucharContraExploradora(this);
    }

    @Override
    public ResultadoEncuentro lucharContraExploradora(NaveExploradora navePropia) {
        return ResultadoEncuentro.PACTO;
    }

    @Override
    public ResultadoEncuentro lucharContraCarguero(NaveCarguero navePropia) {
        return ResultadoEncuentro.PIERDE;
    }

    @Override
    public ResultadoEncuentro lucharContraCombate(NaveCombate navePropia) {
        return ResultadoEncuentro.GANA;
    }
}
