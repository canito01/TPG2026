package modelo.motor;

/** Motor listo para operar. Salto simple: avanza. Salto warp: inicia la preparacion (turno 1 de 2). */
public class Disponible extends EstadoBase {

    @Override
    public ResultadoDeTurno saltar(MotorWarp m) { return saltoSimple(m); }

    @Override
    public ResultadoDeTurno saltarWarp(MotorWarp m) {
        return transicion(m, new PreparandoSalto(1), 0,
                "Inicia la preparacion del salto warp (turno 1 de " + ParametrosDelMotor.TURNOS_PREPARACION
                + "); la nave no avanza");
    }

    @Override
    public String nombre() { return "Disponible"; }
}
