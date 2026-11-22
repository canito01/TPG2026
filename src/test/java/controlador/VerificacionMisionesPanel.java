package controlador;

import java.util.List;

import javax.swing.SwingUtilities;

import modelo.asistente.AsistenteBase;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.PosicionNave;
import modelo.fabrica.NaveFactory;
import modelo.misiones.HistorialDeInformes;
import modelo.misiones.InformeMision;
import modelo.nave.Nave;
import modelo.nave.TipoNave;
import modelo.navegacion.Coordenada;
import modelo.tablero.TableroStub;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.Origen;
import modelo.tripulacion.Tripulante;
import vista.MisionesPanel;
import vista.MisionesPanel.TipoMisionUI;

/**
 * Verifica MisionesPanel, MisionController e HistorialDeInformes cubriendo
 * los escenarios V2 (M-01), V3 (M-02), V4 (M-03), V5 (falla en preparar),
 * V6 (falla al llegar sin auxilio) y la validacion de tripulacion minima.
 */
public final class VerificacionMisionesPanel {

    private VerificacionMisionesPanel() {
    }

    public static void main(String[] args) throws Exception {
        verificarRechazoSinTripulacionValida();
        verificarEscenariosV2V3V4();
        verificarEscenarioV5FallaEnPreparar();
        verificarEscenarioV6FallaAlLlegarSinAuxilio();
        System.out.println("MisionesPanel y MisionController (V2-V6): todos los casos pasaron.");
    }

    private static Nave crearNaveTripulada(TipoNave tipo, String id) {
        Nave nave = new NaveFactory().crear(tipo, id);
        nave.setBaseDeOrigen(new Coordenada(5, 5));
        nave.agregarTripulante(new Tripulante("Kirk", Cargo.CAPITAN, Origen.TERRICOLA, 10));
        nave.agregarTripulante(new Tripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, 4, 3));
        nave.agregarTripulante(new Tripulante("Uhura", Cargo.TENIENTE, Origen.TERRICOLA, 5));
        nave.agregarTripulante(new Tripulante("Sulu", Cargo.TENIENTE, Origen.TERRICOLA, 3));
        nave.agregarTripulante(new Tripulante("Chekov", Cargo.ALFEREZ, Origen.MARCIANO, 1));
        return nave;
    }

    private static void esperarEDT() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
    }

    private static void verificarRechazoSinTripulacionValida() throws Exception {
        MisionesPanel[] panelRef = new MisionesPanel[1];
        SwingUtilities.invokeAndWait(() -> panelRef[0] = new MisionesPanel());
        HistorialDeInformes historial = new HistorialDeInformes();
        MisionController ctrl = new MisionController(
                panelRef[0], historial, () -> new PosicionNave(8, 5), null, 0);

        if (ctrl.iniciarMisionDesdeVista()) {
            throw new AssertionError("No debe iniciar sin nave.");
        }

        Nave incompleta = new NaveFactory().crear(TipoNave.EXPLORADORA, "EXP-INC");
        incompleta.agregarTripulante(new Tripulante("Solo", Cargo.CAPITAN, Origen.TERRICOLA, 2));
        AsistenteDeComando asistente = AsistenteBase.conTablero(incompleta, new TableroStub(1L));
        ctrl.actualizarNaveActiva(incompleta, asistente);

        if (ctrl.iniciarMisionDesdeVista()) {
            throw new AssertionError("No debe iniciar con menos de 5 tripulantes.");
        }
        if (historial.cantidad() != 0) {
            throw new AssertionError("No debe registrar informes ante rechazo previo.");
        }
    }

    private static void verificarEscenariosV2V3V4() throws Exception {
        MisionesPanel[] panelRef = new MisionesPanel[1];
        SwingUtilities.invokeAndWait(() -> panelRef[0] = new MisionesPanel());
        MisionesPanel panel = panelRef[0];
        HistorialDeInformes historial = new HistorialDeInformes();

        Nave nave = crearNaveTripulada(TipoNave.CARGUERO, "CAR-V234");
        TableroStub tablero = new TableroStub(2L);
        AsistenteDeComando asistente = AsistenteBase.conTablero(nave, tablero);

        MisionController ctrl = new MisionController(
                panel, historial, () -> new PosicionNave(8, 5), null, 0);
        ctrl.actualizarNaveActiva(nave, asistente);

        // V2: M-01 exitosa con transferencia de auxilio en cierre
        SwingUtilities.invokeAndWait(() -> {
            panel.seleccionarTipoMision(TipoMisionUI.M01);
            panel.setDestinoManual(8, 5);
            panel.setAuxilio(10, 10);
        });
        if (!ctrl.iniciarMisionDesdeVista()) {
            throw new AssertionError("V2: debio iniciar M-01.");
        }
        ctrl.esperarFinDeMision(5_000);
        esperarEDT();

        InformeMision inf1 = historial.obtenerUltimo().orElseThrow();
        if (!inf1.isExitosa() || !"M-01".equals(inf1.getNombre())) {
            throw new AssertionError("V2: M-01 debio resultar exitosa: " + inf1.getObservaciones());
        }

        // V3: M-02 exitosa con gemas entre 20 y 100
        SwingUtilities.invokeAndWait(() -> {
            panel.seleccionarTipoMision(TipoMisionUI.M02);
            panel.setDestinoManual(10, 5);
        });
        if (!ctrl.iniciarMisionDesdeVista()) {
            throw new AssertionError("V3: debio iniciar M-02.");
        }
        ctrl.esperarFinDeMision(5_000);
        esperarEDT();

        InformeMision inf2 = historial.obtenerUltimo().orElseThrow();
        int gemas = nave.getRecursos().getGemas();
        if (!inf2.isExitosa() || gemas < 20 || gemas > 100) {
            throw new AssertionError("V3: M-02 debio recolectar entre 20 y 100 gemas (obtenidas: " + gemas + ")");
        }

        // V4: M-03 exitosa: retorno a base (5,5), recarga total y desgaste 0
        SwingUtilities.invokeAndWait(() -> panel.seleccionarTipoMision(TipoMisionUI.M03));
        if (!ctrl.iniciarMisionDesdeVista()) {
            throw new AssertionError("V4: debio iniciar M-03.");
        }
        ctrl.esperarFinDeMision(5_000);
        esperarEDT();

        InformeMision inf3 = historial.obtenerUltimo().orElseThrow();
        if (!inf3.isExitosa()
                || nave.getRecursos().getCombustible() != 100
                || nave.getRecursos().getEnergia() != 100
                || nave.getRecursos().getDesgaste() != 0) {
            throw new AssertionError("V4: M-03 debio recargar al 100% y desgaste 0.");
        }
        if (historial.cantidad() != 3 || panel.getCantidadInformesMostrados() != 3) {
            throw new AssertionError("El historial debe contener exactamente 3 informes.");
        }
    }

    private static void verificarEscenarioV5FallaEnPreparar() throws Exception {
        MisionesPanel[] panelRef = new MisionesPanel[1];
        SwingUtilities.invokeAndWait(() -> panelRef[0] = new MisionesPanel());
        MisionesPanel panel = panelRef[0];
        HistorialDeInformes historial = new HistorialDeInformes();

        Nave nave = crearNaveTripulada(TipoNave.EXPLORADORA, "EXP-V5");
        // Dejar combustible en 2 (insuficiente para despegue que pide > 4)
        nave.getRecursos().consumir(58, 0);
        int combAntes = nave.getRecursos().getCombustible();
        int enerAntes = nave.getRecursos().getEnergia();

        AsistenteDeComando asistente = AsistenteBase.conTablero(nave, new TableroStub(3L));
        MisionController ctrl = new MisionController(
                panel, historial, () -> new PosicionNave(7, 5), null, 0);
        ctrl.actualizarNaveActiva(nave, asistente);

        SwingUtilities.invokeAndWait(() -> {
            panel.seleccionarTipoMision(TipoMisionUI.M01);
            panel.setDestinoManual(7, 5);
            panel.setAuxilio(5, 5);
        });
        ctrl.iniciarMisionDesdeVista();
        ctrl.esperarFinDeMision(5_000);
        esperarEDT();

        InformeMision inf = historial.obtenerUltimo().orElseThrow();
        if (inf.isExitosa()) {
            throw new AssertionError("V5: debio fallar en preparar por combustible insuficiente.");
        }
        if (nave.getRecursos().getCombustible() != combAntes || nave.getRecursos().getEnergia() != enerAntes) {
            throw new AssertionError("V5: no debio aplicar cambios parciales a los recursos.");
        }
    }

    private static void verificarEscenarioV6FallaAlLlegarSinAuxilio() throws Exception {
        MisionesPanel[] panelRef = new MisionesPanel[1];
        SwingUtilities.invokeAndWait(() -> panelRef[0] = new MisionesPanel());
        MisionesPanel panel = panelRef[0];
        HistorialDeInformes historial = new HistorialDeInformes();

        Nave nave = crearNaveTripulada(TipoNave.EXPLORADORA, "EXP-V6");
        // Combustible 60 -> dejamos en 20; despegue (-4) + 2 pasos (-4) = 12, pero pide auxilio 50
        nave.getRecursos().consumir(40, 0);

        AsistenteDeComando asistente = AsistenteBase.conTablero(nave, new TableroStub(4L));
        MisionController ctrl = new MisionController(
                panel, historial, () -> new PosicionNave(7, 5), null, 0);
        ctrl.actualizarNaveActiva(nave, asistente);

        SwingUtilities.invokeAndWait(() -> {
            panel.seleccionarTipoMision(TipoMisionUI.M01);
            panel.setDestinoManual(7, 5);
            panel.setAuxilio(0, 50);
        });
        ctrl.iniciarMisionDesdeVista();
        ctrl.esperarFinDeMision(5_000);
        esperarEDT();

        List<InformeMision> todos = historial.obtenerTodos();
        InformeMision inf = todos.get(todos.size() - 1);
        if (inf.isExitosa()) {
            throw new AssertionError("V6: debio fallar al llegar porque no conserva los 50 de combustible de auxilio.");
        }
        if (!inf.getObservaciones().contains("auxilio")) {
            throw new AssertionError("V6: el informe debe indicar que fallo la verificacion de auxilio: "
                    + inf.getObservaciones());
        }
    }
}
