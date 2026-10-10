package modelo.motor;

/** El motor se enfria. Unica accion valida: terminarEnfriamiento (crea Disponible). */
public class Enfriamiento extends EstadoBase {

    @Override
    public void terminarEnfriamiento(MotorWarp m) { m.setEstado(new Disponible()); }

    @Override
    public String nombre() { return "Enfriamiento"; }
}
