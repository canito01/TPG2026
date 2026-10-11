package app;

import java.io.PrintStream;
import java.util.List;

import modelo.asistente.AsistenteBase;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.Evento;
import modelo.asistente.ResumenRecursos;
import modelo.fabrica.NaveFactory;
import modelo.misiones.InformeMision;
import modelo.misiones.Mision1;
import modelo.misiones.Mision2;
import modelo.misiones.Mision3;
import modelo.nave.Nave;
import modelo.nave.TipoNave;
import modelo.tripulacion.BonoAntiguedad;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.ConceptoHaber;
import modelo.tripulacion.Origen;
import modelo.tripulacion.SubsidioOrigen;
import modelo.tripulacion.SueldoBase;
import modelo.tripulacion.Tripulante;
import modelo.universo.CentroDeControl;

/**
 * Escenario A - Ejecucion correcta (Ficha de Inicio).
 *  1. Crear mediante Factory una nave de cada tipo.
 *  2. Seleccionar una (centro.seleccionarActivo) y asignarle una tripulacion valida.
 *  3. Preparar, ejecutar, evaluar y cerrar M-01.
 *  4. Repetir el ciclo completo con M-02 y M-03.
 *  5. Mostrar para cada una los recursos finales, la Bitacora y el informe.
 */
class EscenarioA extends EscenarioBase {

    @Override
    public String getNombre() {
        return "Escenario A - Ejecucion correcta";
    }

    @Override
    public void ejecutar(PrintStream out) {
        // 1. Crear mediante Factory una nave de cada tipo
        out.println("A.1 Creacion de naves mediante Factory y registro en Centro de Control:");
        NaveFactory fabrica = new NaveFactory();
        Nave exp = fabrica.crear(TipoNave.EXPLORADORA, "EXP-01");
        Nave car = fabrica.crear(TipoNave.CARGUERO, "CAR-01");
        Nave com = fabrica.crear(TipoNave.COMBATE, "COM-01");

        CentroDeControl centro = new CentroDeControl();
        AsistenteDeComando aExp = new AsistenteBase(exp);
        AsistenteDeComando aCar = new AsistenteBase(car);
        AsistenteDeComando aCom = new AsistenteBase(com);

        centro.registrar(aExp);
        centro.registrar(aCar);
        centro.registrar(aCom);

        out.println("  Naves creadas y registradas: " + centro.ids());
        exigir(centro.cantidad() == 3, "deben haberse registrado 3 naves");

        // 2. Seleccionar una y asignarle una tripulacion valida
        out.println("\nA.2 Seleccionar nave activa y asignar tripulacion:");
        centro.seleccionarActivo("EXP-01");
        AsistenteDeComando activo = centro.getActivo();
        exigir(activo.getNaveId().equals("EXP-01"), "la nave activa debe ser EXP-01");

        Tripulante capitan = new Tripulante("James Kirk", Cargo.CAPITAN, Origen.TERRICOLA, 10);
        Tripulante oficial = new Tripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, 4);
        exp.agregarTripulante(capitan);
        exp.agregarTripulante(oficial);

        ConceptoHaber haberCapitan = new SubsidioOrigen(new BonoAntiguedad(new SueldoBase(capitan), capitan), capitan);
        out.println("  Nave activa: " + activo.getNaveId() + " (" + exp.getTipo() + ")");
        out.println("  Tripulante 1: " + capitan.getIdentidad() + " (" + capitan.getCargo() + ") - Haber: " + haberCapitan.calcularTotal() + " PG");
        out.println("  Tripulante 2: " + oficial.getIdentidad() + " (" + oficial.getCargo() + ")");
        exigir(exp.getTripulacion().size() == 2, "la nave debe contar con 2 tripulantes");

        // 3. Preparar, ejecutar, evaluar y cerrar M-01
        out.println("\nA.3 Ejecucion de Mision M-01:");
        Mision1 m1 = new Mision1();
        InformeMision inf1 = m1.ejecutarCiclo(activo);
        imprimirInforme(out, inf1);
        exigir(inf1 != null && inf1.getResultado().startsWith("Exito"), "M-01 debe completarse con exito");

        // 4. Repetir el ciclo completo con M-02 y M-03
        out.println("\nA.4 Ejecucion de Misiones M-02 y M-03:");
        Mision2 m2 = new Mision2();
        InformeMision inf2 = m2.ejecutarCiclo(activo);
        imprimirInforme(out, inf2);
        exigir(inf2 != null && inf2.getResultado().startsWith("Exito"), "M-02 debe completarse con exito");

        Mision3 m3 = new Mision3();
        InformeMision inf3 = m3.ejecutarCiclo(activo);
        imprimirInforme(out, inf3);
        exigir(inf3 != null && inf3.getResultado().startsWith("Exito"), "M-03 debe completarse con exito");

        // 5. Mostrar recursos finales, Bitacora e informes
        out.println("\nA.5 Recursos finales y Bitacora:");
        ResumenRecursos rec = activo.consultarRecursos();
        out.println("  Recursos finales: Combustible=" + rec.getCombustible()
                + ", Energia=" + rec.getEnergia()
                + ", Desgaste=" + rec.getDesgaste()
                + " (Requiere mant: " + rec.requiereMantenimiento() + ")");

        out.println("  Bitacora (" + activo.consultarBitacora().size() + " eventos):");
        for (Evento ev : activo.consultarBitacora()) {
            out.println("    [" + ev.getFecha() + "] " + ev.getDescripcion());
        }

        exigir(rec.getCombustible() == 48, "combustible final esperado es 48");
        exigir(rec.getEnergia() == 70, "energia final esperada es 70");
        exigir(rec.getDesgaste() == 12, "desgaste final esperado es 12");
        exigir(!activo.consultarBitacora().isEmpty(), "la bitacora no debe estar vacia");

        out.println("  VERIFICADO: Escenario A ejecutado correctamente con las tres misiones cumplidas");
    }

    private static void imprimirInforme(PrintStream out, InformeMision inf) {
        out.println("  Informe " + inf.getNombre() + ": " + inf.getResultado());
        out.println("    Recursos consumidos: " + inf.getRecursosConsumidos());
        out.println("    Estado de motor: " + inf.getEstadoNave());
        out.println("    Observaciones: " + inf.getObservaciones());
    }
}

