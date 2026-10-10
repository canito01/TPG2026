package modelo.motor;

/**
 * Salto en curso ("En warp" en el Enunciado y la Ficha, "Salto warp" en la
 * Aclaracion). Unica accion valida: enfriar (crea Enfriamiento).
 */
public class EnWarp extends EstadoBase {

    @Override
    public void enfriar(MotorWarp m) { m.setEstado(new Enfriamiento()); }

    @Override
    public String nombre() { return "En warp"; }
}
