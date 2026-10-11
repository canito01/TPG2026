package app;

import java.io.PrintStream;

import modelo.asistente.AsistenteBase;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.Resultado;
import modelo.asistente.ResumenRecursos;
import modelo.fabrica.NaveFactory;
import modelo.nave.Nave;
import modelo.nave.RecursoFueraDeRangoException;
import modelo.nave.TipoNave;

/**
 * Escenario D - Contrato invalido (Ficha de Inicio).
 *  1. Intentar una carga o actualizacion que exceda una capacidad o deje un recurso fuera de rango.
 *  2. Verificar que el estado anterior se conserva.
 */
class EscenarioD extends EscenarioBase {

    @Override
    public String getNombre() {
        return "Escenario D - Contrato invalido";
    }

    @Override
    public void ejecutar(PrintStream out) {
        out.println("D.1 Estado inicial de la nave (Carguero):");
        NaveFactory fabrica = new NaveFactory();
        Nave nave = fabrica.crear(TipoNave.CARGUERO, "CAR-D");
        AsistenteDeComando a = new AsistenteBase(nave);

        ResumenRecursos inicial = a.consultarRecursos();
        out.println("  Combustible=" + inicial.getCombustible()
                + " (capacidad maxima 100), Energia=" + inicial.getEnergia()
                + ", Desgaste=" + inicial.getDesgaste());
        exigir(inicial.getCombustible() == 100, "el combustible inicial de carguero es 100");

        // Intento 1: Carga que excede la capacidad maxima en Recursos directamente
        out.println("\nD.2 Intentar carga que excede la capacidad maxima directamente en Recursos:");
        boolean excepcionCapacidad = false;
        try {
            nave.getRecursos().cargarCombustible(1);
            exigir(false, "debia rechazar carga que excede capacidad");
        } catch (RecursoFueraDeRangoException e) {
            excepcionCapacidad = true;
            out.println("  Capturada RecursoFueraDeRangoException: " + e.getMessage());
        }
        exigir(excepcionCapacidad, "debio lanzar RecursoFueraDeRangoException");
        exigir(nave.getRecursos().getCombustible() == 100, "el combustible se conserva en 100");

        // Intento 2: Carga que excede capacidad a traves del Asistente (devuelve Resultado de rechazo)
        out.println("\nD.3 Intentar carga que excede capacidad a traves del Asistente:");
        Resultado resCarga = a.cargarCombustible(50);
        out.println("  Resultado: exito=" + resCarga.esExito() + ", mensaje=" + resCarga.getMensaje());
        exigir(!resCarga.esExito(), "el asistente debia devolver rechazo");
        exigir(a.consultarRecursos().getCombustible() == 100, "el combustible sigue en 100");

        // Intento 3: Carga con cantidad negativa o cero (viola precondicion de entrada)
        out.println("\nD.4 Intentar carga con cantidad invalida (<= 0):");
        boolean argumentoInvalido = false;
        try {
            a.cargarCombustible(-10);
            exigir(false, "debia rechazar cantidad negativa");
        } catch (IllegalArgumentException e) {
            argumentoInvalido = true;
            out.println("  Capturada IllegalArgumentException: " + e.getMessage());
        }
        exigir(argumentoInvalido, "debio lanzar IllegalArgumentException");

        // Intento 4: Consumo superior a lo disponible
        out.println("\nD.5 Intentar consumo superior al disponible:");
        boolean consumoInvalido = false;
        try {
            nave.getRecursos().consumir(200, 0);
            exigir(false, "debia rechazar consumo excesivo");
        } catch (RecursoFueraDeRangoException e) {
            consumoInvalido = true;
            out.println("  Capturada RecursoFueraDeRangoException: " + e.getMessage());
        }
        exigir(consumoInvalido, "debio lanzar RecursoFueraDeRangoException");

        // Verificacion final de conservacion del estado
        out.println("\nD.6 Verificacion del estado final:");
        ResumenRecursos actual = a.consultarRecursos();
        out.println("  Estado final: Combustible=" + actual.getCombustible()
                + ", Energia=" + actual.getEnergia()
                + ", Desgaste=" + actual.getDesgaste());

        exigir(actual.getCombustible() == inicial.getCombustible(), "combustible conservado");
        exigir(actual.getEnergia() == inicial.getEnergia(), "energia conservada");
        exigir(actual.getDesgaste() == inicial.getDesgaste(), "desgaste conservado");

        out.println("  VERIFICADO: Todas las operaciones invalidas fueron rechazadas y el estado se conservo intacto");
    }
}

