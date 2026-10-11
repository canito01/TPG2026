package app;

/** Lo comun de los escenarios: la verificacion que hace fallar al escenario. */
abstract class EscenarioBase implements Escenario {

    protected static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException("Verificacion fallida: " + mensaje);
        }
    }
}
