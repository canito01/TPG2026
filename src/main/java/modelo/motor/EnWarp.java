package modelo.motor;

/**
 * Motor listo para materializar el salto ("En warp" en el Enunciado).
 * Salto simple: avanza 1 y el motor sigue listo. Salto warp: con 5 de energia
 * los consume, avanza 2 casillas y pasa a Enfriamiento; sin energia aborta y
 * vuelve a la preparacion (reiniciada).
 */
public class EnWarp extends EstadoBase {

    @Override
    public ResultadoDeTurno saltar(MotorWarp m) { return saltoSimple(m); }

    @Override
    public ResultadoDeTurno saltarWarp(MotorWarp m) {
        if (!hayEnergiaParaWarp(m)) {
            return transicion(m, new PreparandoSalto(0), 0, "Salto warp abortado: faltan "
                    + ParametrosDelMotor.ENERGIA_WARP + " de energia; vuelve a la preparacion");
        }
        SistemasDeLaNave nave = m.getNave();
        nave.consumirEnergia(ParametrosDelMotor.ENERGIA_WARP);
        int avanzo = nave.avanzar(ParametrosDelMotor.CASILLAS_WARP);
        return transicion(m, new Enfriamiento(), avanzo, "Salto warp: consume "
                + ParametrosDelMotor.ENERGIA_WARP + " de energia y avanza " + avanzo + " casillas");
    }

    @Override
    public String nombre() { return "En warp"; }
}
