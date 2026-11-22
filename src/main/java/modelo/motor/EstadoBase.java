package modelo.motor;

/**
 * Lo que comparten los estados: armar el resultado del turno, hacer la
 * transicion y las dos reglas que se repiten (salto simple y chequeo de
 * energia del warp). Cada estado concreto declara solo lo propio.
 */
abstract class EstadoBase implements EstadoMotor {

    /** Cambia el estado del motor y devuelve lo ocurrido. */
    protected ResultadoDeTurno transicion(MotorWarp m, EstadoMotor nuevo, int casillas, String descripcion) {
        String antes = nombre();
        m.setEstado(nuevo);
        return new ResultadoDeTurno(antes, nuevo.nombre(), casillas, descripcion);
    }

    /** Resultado de un turno que no cambia de estado. */
    protected ResultadoDeTurno mismoEstado(int casillas, String descripcion) {
        return new ResultadoDeTurno(nombre(), nombre(), casillas, descripcion);
    }

    protected boolean hayEnergiaParaWarp(MotorWarp m) {
        return m.getNave().getEnergia() >= ParametrosDelMotor.ENERGIA_WARP;
    }

    /**
     * Salto simple: consume 2 de combustible y avanza 1 casilla. Si no alcanza el
     * combustible no se mueve y lo informa (el estado no cambia).
     */
    protected ResultadoDeTurno saltoSimple(MotorWarp m) {
        SistemasDeLaNave nave = m.getNave();
        if (nave.getCombustible() < ParametrosDelMotor.COMBUSTIBLE_SALTO_SIMPLE) {
            return mismoEstado(0, "Salto simple imposible: no alcanza el combustible (hacen falta "
                    + ParametrosDelMotor.COMBUSTIBLE_SALTO_SIMPLE + "); la nave no avanza");
        }
        nave.consumirCombustible(ParametrosDelMotor.COMBUSTIBLE_SALTO_SIMPLE);
        int avanzo = nave.avanzar(ParametrosDelMotor.CASILLAS_SALTO_SIMPLE);
        return mismoEstado(avanzo, "Salto simple: avanza " + avanzo + " casilla y consume "
                + ParametrosDelMotor.COMBUSTIBLE_SALTO_SIMPLE + " de combustible");
    }
}
