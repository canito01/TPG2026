package modelo.motor;

import modelo.asistente.TransicionInvalidaException;

/**
 * Clase abstracta con el rechazo por defecto: toda accion es invalida salvo que
 * el estado concreto la redefina. Asi cada estado declara SOLO sus transiciones
 * validas y no se repite el codigo de rechazo.
 */
abstract class EstadoBase implements EstadoMotor {

    @Override
    public void prepararSalto(MotorWarp m) { rechazar("prepararSalto"); }

    @Override
    public void saltar(MotorWarp m) { rechazar("saltar"); }

    @Override
    public void enfriar(MotorWarp m) { rechazar("enfriar"); }

    @Override
    public void terminarEnfriamiento(MotorWarp m) { rechazar("terminarEnfriamiento"); }

    @Override
    public boolean esDisponible() { return false; }

    /** No toca el motor: la accion no permitida no tiene efecto, solo se informa. */
    protected void rechazar(String accion) {
        throw new TransicionInvalidaException(nombre(), accion);
    }
}
