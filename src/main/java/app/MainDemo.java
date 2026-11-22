package app;

/**
 * Main de la Entrega 1 conservado como MainDemo (regresion por consola, plan
 * E2): simula al usuario ejecutando los escenarios de la Ficha adaptados a la
 * nueva API junto con la navegacion en tablero. El arranque con interfaz
 * grafica es MainApp.
 *
 * Es el UNICO lugar que imprime en consola; el modelo no usa Scanner ni
 * System.out.
 */
public class MainDemo {

    public static void main(String[] args) {
        Escenario[] escenarios = {
            new EscenarioCentroDeControl(),
            new EscenarioA(),
            new EscenarioB(),
            new EscenarioC(),
            new EscenarioNavegacion(),
            new EscenarioD()
        };

        int fallos = 0;
        for (int i = 0; i < escenarios.length; i++) {
            System.out.println();
            System.out.println("=== " + escenarios[i].getNombre() + " ===");
            try {
                escenarios[i].ejecutar(System.out);
            } catch (IllegalStateException e) {
                fallos++;
                System.out.println("FALLO: " + e.getMessage());
            }
        }
        System.out.println();
        System.out.println(fallos == 0 ? "Escenarios ejecutados sin fallos."
                : fallos + " escenario(s) con fallos.");
        if (fallos > 0) {
            System.exit(1);
        }
    }
}
