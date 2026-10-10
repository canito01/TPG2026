package fabrica;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import modelo.motor.MotorWarp;
import modelo.nave.Nave;
import modelo.nave.NaveCarguero;
import modelo.nave.NaveCombate;
import modelo.nave.NaveExploradora;
import modelo.nave.TipoNave;

/**
 * Fábrica de naves. El cliente pide un {@link TipoNave} y recibe una {@link Nave}; nunca
 * necesita conocer ni instanciar las clases concretas.
 *
 * <p><b>Garantía:</b> toda nave creada tiene la configuración inicial válida de su tipo
 * (recursos de la Ficha de Inicio, motor propio y sin tripulantes), y su
 * {@link Nave#getTipo()} coincide con el tipo pedido.</p>
 *
 * <h2>Cómo agregar un tipo de nave nuevo (cambios localizados)</h2>
 * <ol>
 *   <li>Agregar la constante al enum {@link TipoNave}.</li>
 *   <li>Crear la clase concreta (por ejemplo {@code NaveCientifica extends Nave}) con su
 *       configuración inicial, copiando el esquema de {@link NaveExploradora}.</li>
 *   <li>Registrarla con <b>una sola línea</b> en el constructor de esta fábrica:
 *       {@code registrar(TipoNave.CIENTIFICA, NaveCientifica::new);}</li>
 * </ol>
 * <p>No se modifica ninguna clase existente fuera de esos tres puntos ni el método
 * {@link #crear(TipoNave, String)}, que no contiene condicionales por tipo.</p>
 */
public final class NaveFactory {

    private final Map<TipoNave, BiFunction<String, MotorWarp, Nave>> constructores =
            new EnumMap<>(TipoNave.class);

    public NaveFactory() {
        registrar(TipoNave.EXPLORADORA, NaveExploradora::new);
        registrar(TipoNave.CARGUERO, NaveCarguero::new);
        registrar(TipoNave.COMBATE, NaveCombate::new);
    }

    /**
     * Crea una nave del tipo pedido.
     *
     * <p><b>Precondición:</b> {@code tipo} no es nulo, está registrado, y {@code id} no es
     * nulo ni vacío.<br>
     * <b>Postcondición:</b> devuelve una nave nueva e independiente, con
     * {@code getTipo() == tipo}, recursos iniciales del tipo y sin tripulantes.</p>
     *
     * @throws IllegalArgumentException si se viola la precondición
     */
    public Nave crear(TipoNave tipo, String id) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de nave es obligatorio");
        }
        BiFunction<String, MotorWarp, Nave> constructor = constructores.get(tipo);
        if (constructor == null) {
            throw new IllegalArgumentException("Tipo de nave no soportado: " + tipo);
        }
        Nave nave = constructor.apply(id, new MotorWarp());
        if (nave.getTipo() != tipo) { // chequeo de la postcondición
            throw new IllegalStateException("La fábrica produjo una nave de tipo " + nave.getTipo()
                    + " al pedirse " + tipo);
        }
        return nave;
    }

    private void registrar(TipoNave tipo, BiFunction<String, MotorWarp, Nave> constructor) {
        constructores.put(tipo, constructor);
    }
}
