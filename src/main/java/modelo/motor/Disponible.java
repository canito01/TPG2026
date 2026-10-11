package modelo.motor;

/** Estado inicial. Unica accion valida: prepararSalto (crea PreparandoSalto). */
public class Disponible extends EstadoBase {

    @Override
    public void prepararSalto(MotorWarp m) { m.setEstado(new PreparandoSalto()); }

    @Override
    public String nombre() { return "Disponible"; }

    @Override
    public boolean esDisponible() { return true; }
}
