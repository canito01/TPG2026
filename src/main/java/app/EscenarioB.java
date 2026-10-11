package app;

import java.io.PrintStream;
import java.util.List;

import modelo.asistente.AsistenteBase;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.Evento;
import modelo.asistente.ResumenRecursos;
import modelo.fabrica.NaveFactory;
import modelo.misiones.Mision1;
import modelo.nave.Nave;
import modelo.nave.TipoNave;

/**
 * Escenario B - Recursos insuficientes (Ficha de Inicio).
 *  1. Utilizar una nave sin recursos suficientes para la mision.
 *  2. Intentar prepararla o ejecutarla.
 *  3. Verificar que la operacion se rechaza, no deja cambios parciales y registra el motivo.
 */
class EscenarioB extends EscenarioBase {

    @Override
    public String getNombre() {
        return "Escenario B - Recursos insuficientes";
    }

    @Override
    public void ejecutar(PrintStream out) {
        // 1. Utilizar una nave sin recursos suficientes para la mision
        out.println("B.1 Preparar nave con recursos insuficientes:");
        NaveFactory fabrica = new NaveFactory();
        Nave nave = fabrica.crear(TipoNave.EXPLORADORA, "EXP-B");
        AsistenteDeComando a = new AsistenteBase(nave);

        // La mision 1 requiere 4 de combustible. Consumimos 58 para dejar solo 2.
        a.consumir(58, 0, 0);
        ResumenRecursos antes = a.consultarRecursos();
        out.println("  Recursos antes del intento: Combustible=" + antes.getCombustible()
                + ", Energia=" + antes.getEnergia()
                + ", Desgaste=" + antes.getDesgaste());
        exigir(antes.getCombustible() == 2, "el combustible disponible debe ser 2 (insuficiente para M-01 que pide 4)");

        // 2. Intentar ejecutar la mision
        out.println("\nB.2 Intento de ejecucion de Mision M-01:");
        Mision1 mision = new Mision1();
        boolean excepcionLanzada = false;
        try {
            mision.ejecutarCiclo(a);
            exigir(false, "la mision debia fallar por recursos insuficientes");
        } catch (IllegalStateException e) {
            excepcionLanzada = true;
            out.println("  Rechazo capturado correctamente: " + e.getMessage());
        }
        exigir(excepcionLanzada, "debia lanzarse IllegalStateException");

        // 3. Verificar que la operacion se rechaza, no deja cambios parciales y registra el motivo
        out.println("\nB.3 Verificacion de atomicidad y registro en Bitacora:");
        ResumenRecursos despues = a.consultarRecursos();
        out.println("  Recursos tras el rechazo: Combustible=" + despues.getCombustible()
                + ", Energia=" + despues.getEnergia()
                + ", Desgaste=" + despues.getDesgaste());

        exigir(despues.getCombustible() == antes.getCombustible(), "el combustible se conserva sin cambios");
        exigir(despues.getEnergia() == antes.getEnergia(), "la energia se conserva sin cambios");
        exigir(despues.getDesgaste() == antes.getDesgaste(), "el desgaste se conserva sin cambios");

        List<Evento> eventos = a.consultarBitacora();
        boolean motivoEnBitacora = false;
        for (Evento ev : eventos) {
            out.println("  Evento: [" + ev.getFecha() + "] " + ev.getDescripcion());
            if (ev.getDescripcion().contains("Consumo rechazado")) {
                motivoEnBitacora = true;
            }
        }
        exigir(motivoEnBitacora, "el motivo del rechazo debe constar en la bitacora");

        out.println("  VERIFICADO: Mision rechazada sin cambios parciales y motivo registrado en bitacora");
    }
}

