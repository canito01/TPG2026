package modelo.nave;

import modelo.elementos.ResultadoEncuentro;
import modelo.motor.MotorWarp;

/**
 * Nave de combate: combustible 80, energía 100, desgaste 0.
 *
 * Tabla de encuentros (cuando esta nave es la enemiga):
 * - Propia Exploradora vs. esta de Combate -> PIERDE (la exploradora propia pierde)
 * - Propia Carguera    vs. esta de Combate -> GANA   (la carguera propia gana)
 * - Propia Combate     vs. esta de Combate -> PACTO
 */
public class NaveCombate extends Nave {

    public static final int COMBUSTIBLE_INICIAL = 80;
    public static final int ENERGIA_INICIAL = 100;
    public static final int DESGASTE_INICIAL = 0;

    /** Usar {@code NaveFactory} en lugar de invocar este constructor desde código cliente. */
    public NaveCombate(String id, MotorWarp motor) {
        super(id, TipoNave.COMBATE,
                new Recursos(COMBUSTIBLE_INICIAL, ENERGIA_INICIAL, DESGASTE_INICIAL), motor);
    }

    @Override
    protected ResultadoEncuentro despacharEncuentro(Nave naveEnemiga) {
        return naveEnemiga.lucharContraCombate(this);
    }

    @Override
    public ResultadoEncuentro lucharContraExploradora(NaveExploradora navePropia) {
        return ResultadoEncuentro.PIERDE;
    }

    @Override
    public ResultadoEncuentro lucharContraCarguero(NaveCarguero navePropia) {
        return ResultadoEncuentro.GANA;
    }

    @Override
    public ResultadoEncuentro lucharContraCombate(NaveCombate navePropia) {
        return ResultadoEncuentro.PACTO;
    }
}
