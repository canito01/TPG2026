package app;

/**
 * Programa principal de demostracion (R6): simula al usuario. Es el UNICO
 * lugar que imprime en consola; el modelo no usa Scanner ni System.out, por
 * eso este programa puede reemplazarse por pantallas sin cambiar ninguna
 * clase del modelo.
 *
 * Ejecuta los escenarios de la Ficha: el Universo y el C (Motor Warp) estan
 * completos; A, B y D se completan al integrar las piezas de los demas
 * (ver EscenarioPendiente).
 */
public class Main {

    public static void main(String[] args) {
        Escenario[] escenarios = {
            new EscenarioCentroDeControl(),
            new EscenarioPendiente("Escenario A - Ejecucion correcta", new String[] {
                    "Crear mediante Factory una nave de cada tipo.",
                    "Seleccionar una (centro.seleccionarActivo) y asignarle una tripulacion valida.",
                    "Preparar, ejecutar, evaluar y cerrar M-01.",
                    "Repetir el ciclo completo con M-02 y M-03.",
                    "Mostrar para cada una los recursos finales, la Bitacora y el informe."},
                new String[] {
                    "Eneas: NaveFactory y Recursos",
                    "Nicolas: Tripulacion",
                    "Tomas: Mision, M1-M3 e InformeMision",
                    "Agustin: AsistenteDeComando y Bitacora",
                    "Joaquin: centro de control (alta y seleccion de la nave activa) y motor ya listos"}),
            new EscenarioPendiente("Escenario B - Recursos insuficientes", new String[] {
                    "Utilizar una nave sin recursos suficientes para la mision.",
                    "Intentar prepararla o ejecutarla.",
                    "Verificar que la operacion se rechaza, no deja cambios parciales y registra el motivo."},
                new String[] {
                    "Eneas: Recursos (rechazo sin cambios parciales)",
                    "Tomas: Mision.preparar que consulta al asistente",
                    "Agustin: registro del motivo en la Bitacora"}),
            new EscenarioC(),
            new EscenarioPendiente("Escenario D - Contrato invalido", new String[] {
                    "Intentar una carga o actualizacion que exceda una capacidad o deje un recurso fuera de rango.",
                    "Verificar que el estado anterior se conserva."},
                new String[] {
                    "Eneas: Recursos y Nave con sus contratos (precondiciones e invariantes)"})
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
