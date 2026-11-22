package modelo.tablero;

import java.util.Objects;
import modelo.elementos.ElementoDelUniverso;
import modelo.nave.Nave;

/**
 * Entrada del TableroStub. Puede contener una etiqueta de prueba o un elemento
 * del universo que aplica su propio encuentro.
 */
public class ElementoDePrueba {

    private final String nombre;
    private final ElementoDelUniverso elemento;

    /** Pre: nombre no nulo ni vacio. */
    public ElementoDePrueba(String nombre) {
        this(nombre, null);
    }

    public static ElementoDePrueba representar(ElementoDelUniverso elemento) {
        return new ElementoDePrueba(Objects.requireNonNull(elemento).getNombre(), elemento);
    }

    private ElementoDePrueba(String nombre, ElementoDelUniverso elemento) {
        if (nombre == null || nombre.trim().length() == 0) {
            throw new IllegalArgumentException("El nombre del elemento no puede ser nulo ni vacio");
        }
        this.nombre = nombre;
        this.elemento = elemento;
    }

    public String getNombre() { return nombre; }

    public String interactuarCon(Nave nave) {
        return elemento == null ? null : elemento.interactuarCon(nave);
    }
}
