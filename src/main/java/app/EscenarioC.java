package app;

import java.io.PrintStream;

import modelo.motor.ResultadoDeTurno;

/**
 * Escenario C adaptado a la Entrega 2 (V7: Warp completo). Con la nueva API
 * publica (solo saltar y saltarWarp) ya no hay transiciones invalidas: cada
 * operacion es valida en todo estado y lo que se verifica es lo que el Anexo
 * pide ver:
 *  C.1 ciclo completo: Disponible > Preparando salto > En warp > Enfriamiento > Disponible
 *  C.2 salto simple
 *  C.3 sin energia durante la preparacion: se queda preparando
 *  C.4 aborto del salto: sin energia al saltar, vuelve a la preparacion
 *  C.5 el turno de enfriamiento tambien se cumple con un salto simple
 */
class EscenarioC extends EscenarioBase {

    @Override
    public String getNombre() { return "Escenario C - Motor Warp (V7)"; }

    @Override
    public void ejecutar(PrintStream out) {
        cicloCompleto(out);
        saltoSimple(out);
        sinEnergiaEnLaPreparacion(out);
        abortoDelSalto(out);
        enfriamientoConSaltoSimple(out);
    }

    private void cicloCompleto(PrintStream out) {
        out.println("C.1 Ciclo completo con saltarWarp (nave con 100 de combustible y 100 de energia):");
        AsistenteDeDemostracion a = AsistenteDeDemostracion.enLinea("NAVE-C1", 100, 100);
        paso(out, a, true, "Disponible", "Preparando salto", 0);
        paso(out, a, true, "Preparando salto", "En warp", 0);
        paso(out, a, true, "En warp", "Enfriamiento", 2);
        paso(out, a, true, "Enfriamiento", "Disponible", 0);
        exigir(a.getNave().getPosicion().getX() == 2, "tras el warp la nave esta 2 casillas adelante");
        exigir(a.getNave().getEnergia() == 95, "el warp consumio 5 de energia");
        exigir(a.getNave().getCombustible() == 100, "el warp no consume combustible");
        exigir(a.getNave().getDesgaste() == 3, "el enfriamiento genero 3 de desgaste");
        out.println("  VERIFICADO: energia 95, combustible 100, desgaste 3, posicion " + a.getNave().getPosicion());
    }

    private void saltoSimple(PrintStream out) {
        out.println("C.2 Salto simple desde Disponible:");
        AsistenteDeDemostracion a = AsistenteDeDemostracion.enLinea("NAVE-C2", 100, 100);
        paso(out, a, false, "Disponible", "Disponible", 1);
        exigir(a.getNave().getCombustible() == 98, "el salto simple consume 2 de combustible");
        exigir(a.getNave().getEnergia() == 100, "el salto simple no consume energia");
        out.println("  VERIFICADO: combustible 98, energia 100, posicion " + a.getNave().getPosicion());
    }

    private void sinEnergiaEnLaPreparacion(PrintStream out) {
        out.println("C.3 Sin energia durante la preparacion (4 de energia, hacen falta 5):");
        AsistenteDeDemostracion a = AsistenteDeDemostracion.enLinea("NAVE-C3", 100, 4);
        paso(out, a, true, "Disponible", "Preparando salto", 0);
        paso(out, a, true, "Preparando salto", "Preparando salto", 0);
        paso(out, a, true, "Preparando salto", "Preparando salto", 0);
        exigir(a.getNave().getEnergia() == 4 && a.getNave().getPosicion().getX() == 0,
                "sin energia no se consume nada ni se avanza");
        a.getNave().setEnergia(10);
        out.println("  (la nave recarga: energia 10)");
        paso(out, a, true, "Preparando salto", "En warp", 0);
        out.println("  VERIFICADO: se quedo preparando sin avanzar y siguio al recuperar energia");
    }

    private void abortoDelSalto(PrintStream out) {
        out.println("C.4 Aborto: llega al salto sin energia (5 de energia y la pierde antes de saltar):");
        AsistenteDeDemostracion a = AsistenteDeDemostracion.enLinea("NAVE-C4", 100, 5);
        paso(out, a, true, "Disponible", "Preparando salto", 0);
        paso(out, a, true, "Preparando salto", "En warp", 0);
        a.getNave().consumirEnergia(5);
        out.println("  (un impacto deja la energia en 0)");
        paso(out, a, true, "En warp", "Preparando salto", 0);
        exigir(a.getNave().getPosicion().getX() == 0, "el salto abortado no mueve la nave");
        a.getNave().setEnergia(10);
        out.println("  (la nave recarga: energia 10; la preparacion se reinicio: faltan 2 turnos)");
        paso(out, a, true, "Preparando salto", "Preparando salto", 0);
        paso(out, a, true, "Preparando salto", "En warp", 0);
        out.println("  VERIFICADO: abortado, vuelve a preparar y completa la preparacion de nuevo");
    }

    private void enfriamientoConSaltoSimple(PrintStream out) {
        out.println("C.5 El turno de enfriamiento se cumple tambien con saltar():");
        AsistenteDeDemostracion a = AsistenteDeDemostracion.enLinea("NAVE-C5", 100, 100);
        paso(out, a, true, "Disponible", "Preparando salto", 0);
        paso(out, a, true, "Preparando salto", "En warp", 0);
        paso(out, a, true, "En warp", "Enfriamiento", 2);
        paso(out, a, false, "Enfriamiento", "Disponible", 0);
        exigir(a.getNave().getDesgaste() == 3 && a.getNave().getPosicion().getX() == 2,
                "enfriar con saltar(): 3 de desgaste y sin avanzar");
        out.println("  VERIFICADO: desgaste 3, la nave no avanzo mientras se enfriaba");
    }

    /** Pide un turno, lo muestra y exige el estado inicial, el final y las casillas avanzadas. */
    private void paso(PrintStream out, AsistenteDeDemostracion a, boolean warp,
                      String antes, String despues, int casillas) {
        ResultadoDeTurno r = warp ? a.saltarWarp() : a.turnoSaltar();
        out.println("  " + (warp ? "saltarWarp" : "saltar") + "  " + r + "  | pos=" + a.getNave().getPosicion());
        exigir(r.getEstadoAnterior().equals(antes), "el turno debia empezar en " + antes + " y empezo en " + r.getEstadoAnterior());
        exigir(r.getEstadoActual().equals(despues), "el turno debia terminar en " + despues + " y termino en " + r.getEstadoActual());
        exigir(r.getCasillasAvanzadas() == casillas, "debia avanzar " + casillas + " casillas y avanzo " + r.getCasillasAvanzadas());
    }
}
