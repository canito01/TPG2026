package modelo.tablero;

/**
 * Elemento para poblar el TableroStub mientras Eneas publica sus elementos
 * espaciales. Vive en modelo.tablero (paquete de Joaquin).
 */
public class ElementoDePrueba {

    private final String nombre;

    /** Pre: nombre no nulo ni vacio. */
    public ElementoDePrueba(String nombre) {
        if (nombre == null || nombre.trim().length() == 0) {
            throw new IllegalArgumentException("El nombre del elemento no puede ser nulo ni vacio");
        }
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
}
