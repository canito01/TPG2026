package modelo.fabrica;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import modelo.elementos.NaveEnemiga;
import modelo.motor.MotorWarp;
import modelo.nave.Nave;
import modelo.nave.NaveCarguero;
import modelo.nave.NaveCombate;
import modelo.nave.NaveExploradora;
import modelo.nave.TipoNave;

/**
 * Fábrica de naves (propias y enemigas). El cliente pide un {@link TipoNave} y
 * recibe una {@link Nave} o una {@link NaveEnemiga}; nunca necesita conocer ni
 * instanciar las clases concretas.
 *
 * Garantía: toda nave creada tiene la configuración inicial válida de su tipo
 * (recursos de la Ficha de Inicio, motor propio y sin tripulantes), y su
 * {@link Nave#getTipo()} coincide con el tipo pedido.
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
     * Precondición: {@code tipo} no es nulo, está registrado, y {@code id} no es
     * nulo ni vacío.
     * Postcondición: devuelve una nave nueva e independiente, con
     * {@code getTipo() == tipo}, recursos iniciales del tipo y sin tripulantes.
     *
     * @throws IllegalArgumentException si se rompe la precondición
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
        if (nave.getTipo() != tipo) {
            throw new IllegalStateException("La fábrica produjo una nave de tipo " + nave.getTipo() + " al pedirse " + tipo);
        }
        return nave;
    }

    /**
     * Crea un elemento espacial {@link NaveEnemiga} del tipo pedido utilizando
     * el mismo registro polimórfico de la fábrica, sin que el cliente instancie
     * clases concretas de nave.
     *
     * Precondición: {@code tipo != null} y {@code id} no nulo ni vacío.
     */
    public NaveEnemiga crearNaveEnemiga(TipoNave tipo, String id) {
        return new NaveEnemiga(crear(tipo, id));
    }

    private void registrar(TipoNave tipo, BiFunction<String, MotorWarp, Nave> constructor) {
        constructores.put(tipo, constructor);
    }
}
