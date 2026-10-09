package modelo.motor;

/** El motor se prepara. Unica accion valida: saltar (crea EnWarp). */
public class PreparandoSalto extends EstadoBase {

    @Override
    public void saltar(MotorWarp m) { m.setEstado(new EnWarp()); }

    @Override
    public String nombre() { return "Preparando salto"; }
}
