package app;

import java.io.PrintStream;

/**
 * Escenario cuyos pasos de la Ficha dependen de piezas de OTROS integrantes
 * (fabrica y recursos de Eneas, misiones de Tomas, asistente y bitacora de
 * Agustin, tripulacion de Nicolas). No se simulan esas piezas: este escenario
 * deja escritos los pasos oficiales y quien aporta cada uno, y se reemplaza
 * por la ejecucion real cuando se integren (Fase 2 del plan).
 */
class EscenarioPendiente implements Escenario {

    private final String nombre;
    private final String[] pasosDeLaFicha;
    private final String[] aporta;

    EscenarioPendiente(String nombre, String[] pasosDeLaFicha, String[] aporta) {
        this.nombre = nombre;
        this.pasosDeLaFicha = pasosDeLaFicha;
        this.aporta = aporta;
    }

    @Override
    public String getNombre() { return nombre; }

    @Override
    public void ejecutar(PrintStream out) {
        out.println("PENDIENTE DE INTEGRACION. Pasos de la Ficha:");
        for (int i = 0; i < pasosDeLaFicha.length; i++) {
            out.println("  " + (i + 1) + ". " + pasosDeLaFicha[i]);
        }
        out.println("Lo aporta:");
        for (int i = 0; i < aporta.length; i++) {
            out.println("  - " + aporta[i]);
        }
    }
}
