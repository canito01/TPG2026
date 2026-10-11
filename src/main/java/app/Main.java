package app;

/**
 * Programa principal de demostracion (R6): simula al usuario. Es el UNICO
 * lugar que imprime en consola; el modelo no usa Scanner ni System.out, por
 * eso este programa puede reemplazarse por pantallas sin cambiar ninguna
 * clase del modelo.
 *
 * Ejecuta los escenarios de la Ficha: Centro de Control, Escenario A, Escenario B,
 * Escenario C (Motor Warp) y Escenario D.
 */
public class Main {

    public static void main(String[] args) {
        Escenario[] escenarios = {
            new EscenarioCentroDeControl(),
            new EscenarioA(),
            new EscenarioB(),
            new EscenarioC(),
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
