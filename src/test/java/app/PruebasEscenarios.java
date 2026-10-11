package app;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import pruebas.Verificador;

/**
 * Pruebas de los escenarios: se ejecutan contra un PrintStream en memoria
 * (el modelo no imprime) y se comprueba que cumplan sus verificaciones.
 * Estan en el paquete app porque los escenarios son internos de la demostracion.
 */
public class PruebasEscenarios {

    public static void correrTodas() {
        System.out.println("Escenarios");
        try { escenarioCFunciona(); Verificador.ok("Escenario C: secuencia valida, rechazos registrados y flujo de mision"); }
        catch (Throwable t) { Verificador.fallo("Escenario C: secuencia valida, rechazos registrados y flujo de mision", t); }
        try { escenarioUniversoFunciona(); Verificador.ok("Escenario del centro de control: alta, una activa, rechazos"); }
        catch (Throwable t) { Verificador.fallo("Escenario del centro de control: alta, una activa, rechazos", t); }
        try { pendienteListaPasosYDuenos(); Verificador.ok("escenario pendiente lista los pasos de la Ficha y quien los aporta"); }
        catch (Throwable t) { Verificador.fallo("escenario pendiente lista los pasos de la Ficha y quien los aporta", t); }
    }

    private static String correr(Escenario e) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        e.ejecutar(out);
        out.flush();
        return bytes.toString();
    }

    private static void escenarioCFunciona() {
        String salida = correr(new EscenarioC());
        Verificador.verificar(salida.contains("CAMBIO    Disponible -> Preparando salto (prepararSalto)"), "registra el primer cambio");
        Verificador.verificar(salida.contains("En warp -> Enfriamiento (enfriar)"), "recorre Enfriamiento");
        Verificador.verificar(salida.contains("RECHAZADA saltar en 'Disponible'"), "registra el rechazo de saltar");
        Verificador.verificar(salida.contains("RECHAZADA enfriar en 'Disponible'"), "registra el rechazo de enfriar");
        Verificador.verificar(salida.contains("VERIFICADO"), "la verificacion se cumple");
        Verificador.verificar(salida.contains("En warp -> Disponible (terminarSalto)"), "el salto termina en Disponible");
    }

    private static void escenarioUniversoFunciona() {
        String salida = correr(new EscenarioCentroDeControl());
        Verificador.verificar(salida.contains("Registradas: [NAVE-1, NAVE-2, NAVE-3]"), "alta de tres naves");
        Verificador.verificar(salida.contains("Activa tras seleccionar NAVE-2: NAVE-2"), "una sola activa");
        Verificador.verificar(salida.contains("VERIFICADO"), "la verificacion se cumple");
    }

    private static void pendienteListaPasosYDuenos() {
        Escenario e = new EscenarioPendiente("X", new String[] {"paso uno", "paso dos"}, new String[] {"Eneas: algo"});
        String salida = correr(e);
        Verificador.verificar(salida.contains("PENDIENTE DE INTEGRACION"), "se declara pendiente");
        Verificador.verificar(salida.contains("1. paso uno") && salida.contains("2. paso dos"), "lista los pasos");
        Verificador.verificar(salida.contains("Eneas: algo"), "indica quien lo aporta");
    }
}
