package modelo.motor;

/**
 * Preparando el salto warp. Cada turno (con saltar o con saltarWarp) cumple un
 * turno de preparacion SI hay 5 de energia (el Anexo: "durante la preparacion
 * el sistema evalua si la nave cuenta con las 5 unidades"); sin energia se
 * queda preparando, sin avanzar. Al completar los 2 turnos pasa a EnWarp.
 */
public class PreparandoSalto extends EstadoBase {

    private int turnosCumplidos;

    /** Pre: 0 <= turnosCumplidos < TURNOS_PREPARACION (1 al venir de Disponible, 0 tras un aborto). */
    PreparandoSalto(int turnosCumplidos) {
        this.turnosCumplidos = turnosCumplidos;
    }

    @Override
    public ResultadoDeTurno saltar(MotorWarp m) { return cumplirTurnoDePreparacion(m); }

    @Override
    public ResultadoDeTurno saltarWarp(MotorWarp m) { return cumplirTurnoDePreparacion(m); }

    private ResultadoDeTurno cumplirTurnoDePreparacion(MotorWarp m) {
        if (!hayEnergiaParaWarp(m)) {
            return mismoEstado(0, "Preparacion detenida: faltan " + ParametrosDelMotor.ENERGIA_WARP
                    + " de energia; sigue preparando sin avanzar");
        }
        turnosCumplidos++;
        if (turnosCumplidos >= ParametrosDelMotor.TURNOS_PREPARACION) {
            return transicion(m, new EnWarp(), 0, "Preparacion completa (turno "
                    + ParametrosDelMotor.TURNOS_PREPARACION + " de " + ParametrosDelMotor.TURNOS_PREPARACION
                    + "): motor listo para el salto warp");
        }
        return mismoEstado(0, "Avanza la preparacion (turno " + turnosCumplidos + " de "
                + ParametrosDelMotor.TURNOS_PREPARACION + ")");
    }

    @Override
    public String nombre() { return "Preparando salto"; }
}
