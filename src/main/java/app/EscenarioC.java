package app;

import java.io.PrintStream;

/**
 * Escenario C - Motor Warp (Ficha de Inicio, seccion 6).
 *  1. Recorrer la secuencia valida Disponible -> Preparando salto -> En warp
 *     -> Enfriamiento -> Disponible.
 *  2. Intentar al menos una transicion invalida.
 *  3. Verificar que la transicion invalida se rechaza y queda registrada.
 * Ademas muestra el flujo de mision de R3 (por ahora el salto termina en Disponible,
 * pasando por Enfriamiento sin demora porque todavia no se modela el tiempo).
 */
class EscenarioC extends EscenarioBase {

    @Override
    public String getNombre() { return "Escenario C - Motor Warp"; }

    @Override
    public void ejecutar(PrintStream out) {
        AsistenteDeDemostracion a = new AsistenteDeDemostracion("NAVE-C");
        exigir(a.estadoDelMotor().equals("Disponible") && a.estaDisponible(), "el motor empieza Disponible");
        out.println("Estado inicial: " + a.estadoDelMotor());

        out.println();
        out.println("C.1 Secuencia valida (Ficha):");
        paso(out, a, "prepararSalto", a.prepararSalto(), true, "Preparando salto");
        paso(out, a, "saltar", a.saltar(), true, "En warp");
        paso(out, a, "enfriar", a.enfriar(), true, "Enfriamiento");
        paso(out, a, "terminarEnfriamiento", a.terminarEnfriamiento(), true, "Disponible");

        out.println();
        out.println("C.2 Transiciones invalidas (el estado no debe cambiar):");
        paso(out, a, "saltar (sin preparar)", a.saltar(), false, "Disponible");
        paso(out, a, "enfriar (sin saltar)", a.enfriar(), false, "Disponible");

        out.println();
        out.println("C.3 Verificacion: las rechazadas quedaron registradas");
        exigir(a.cantidadRechazos() == 2, "deben haberse registrado 2 rechazos");
        for (String linea : a.getRegistro()) {
            out.println("  " + linea);
        }
        out.println("  VERIFICADO: 4 cambios validos y 2 rechazos registrados, sin efecto sobre el estado");

        out.println();
        int antesDeC4 = a.getRegistro().size();
        out.println("C.4 Flujo de mision (R3: por ahora el salto termina en Disponible; el enfriamiento pasa sin demora):");
        paso(out, a, "prepararSalto", a.prepararSalto(), true, "Preparando salto");
        paso(out, a, "saltar", a.saltar(), true, "En warp");
        paso(out, a, "terminarSalto", a.terminarSalto(), true, "Disponible");
        exigir(a.estaDisponible(), "tras el salto la nave vuelve a estar disponible");
        for (int i = antesDeC4; i < a.getRegistro().size(); i++) {
            out.println("  " + a.getRegistro().get(i));
        }
    }

    private static void paso(PrintStream out, AsistenteDeDemostracion a, String accion,
                             boolean resultado, boolean esperado, String estadoEsperado) {
        out.println("  " + accion + " -> " + (resultado ? "OK" : "RECHAZADA") + " | estado: " + a.estadoDelMotor());
        exigir(resultado == esperado, accion + (esperado ? " debia aceptarse" : " debia rechazarse"));
        exigir(a.estadoDelMotor().equals(estadoEsperado), accion + ": el estado debia ser " + estadoEsperado);
    }
}
