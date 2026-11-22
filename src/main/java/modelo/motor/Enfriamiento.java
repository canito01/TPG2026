package modelo.motor;

/**
 * Disipacion termica tras el warp: un turno de espera (con saltar o con
 * saltarWarp) que genera 3 de desgaste y deja el motor Disponible. La nave no
 * avanza mientras se enfria.
 */
public class Enfriamiento extends EstadoBase {

    @Override
    public ResultadoDeTurno saltar(MotorWarp m) { return cumplirEnfriamiento(m); }

    @Override
    public ResultadoDeTurno saltarWarp(MotorWarp m) { return cumplirEnfriamiento(m); }

    private ResultadoDeTurno cumplirEnfriamiento(MotorWarp m) {
        m.getNave().agregarDesgaste(ParametrosDelMotor.DESGASTE_ENFRIAMIENTO);
        return transicion(m, new Disponible(), 0, "Cumple el turno de enfriamiento: genera "
                + ParametrosDelMotor.DESGASTE_ENFRIAMIENTO + " de desgaste; el motor vuelve a Disponible");
    }

    @Override
    public String nombre() { return "Enfriamiento"; }
}
