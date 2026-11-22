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
        try { escenarioAFunciona(); Verificador.ok("Escenario A: naves, tripulacion, asistente y 3 misiones"); }
        catch (Throwable t) { Verificador.fallo("Escenario A: naves, tripulacion, asistente y 3 misiones", t); }
        try { escenarioBFunciona(); Verificador.ok("Escenario B: recursos insuficientes, rechazo sin cambios y bitacora"); }
        catch (Throwable t) { Verificador.fallo("Escenario B: recursos insuficientes, rechazo sin cambios y bitacora", t); }
        try { escenarioCFunciona(); Verificador.ok("Escenario C (V7): ciclo warp, sin energia, aborto y enfriamiento"); }
        catch (Throwable t) { Verificador.fallo("Escenario C (V7)", t); }
        try { escenarioDFunciona(); Verificador.ok("Escenario D: violacion de contrato, rechazo y estado conservado"); }
        catch (Throwable t) { Verificador.fallo("Escenario D: violacion de contrato, rechazo y estado conservado", t); }
        try { escenarioNavegacionFunciona(); Verificador.ok("Escenario de tablero y radares: ruta, escaneo y viaje completo"); }
        catch (Throwable t) { Verificador.fallo("Escenario de tablero y radares", t); }
        try { escenarioUniversoFunciona(); Verificador.ok("Escenario del centro de control: alta, una activa, lista, rechazos"); }
        catch (Throwable t) { Verificador.fallo("Escenario del centro de control", t); }
        try { pendienteListaPasosYDuenos(); Verificador.ok("escenario pendiente lista los pasos de la Ficha y quien los aporta"); }
        catch (Throwable t) { Verificador.fallo("escenario pendiente", t); }
        try { viajeEsReproducible(); Verificador.ok("el viaje simulado con la misma semilla da el mismo resultado"); }
        catch (Throwable t) { Verificador.fallo("viaje reproducible", t); }
    }

    private static String correr(Escenario e) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        e.ejecutar(out);
        out.flush();
        return bytes.toString();
    }

    private static void escenarioAFunciona() {
        String salida = correr(new EscenarioA());
        Verificador.verificar(salida.contains("Naves creadas y registradas: [EXP-01, CAR-01, COM-01]"), "crea y registra las 3 naves");
        Verificador.verificar(salida.contains("Nave activa: EXP-01"), "activa EXP-01");
        Verificador.verificar(salida.contains("James Kirk"), "asigna tripulante capitan");
        Verificador.verificar(salida.contains("Informe M-01: Exito"), "M-01 completada con exito");
        Verificador.verificar(salida.contains("Informe M-02: Exito"), "M-02 completada con exito");
        Verificador.verificar(salida.contains("Informe M-03: Exito"), "M-03 completada con exito");
        Verificador.verificar(salida.contains("VERIFICADO"), "verificacion cumplida");
    }

    private static void escenarioBFunciona() {
        String salida = correr(new EscenarioB());
        Verificador.verificar(salida.contains("Rechazo capturado correctamente"), "captura el rechazo por recursos insuficientes");
        Verificador.verificar(salida.contains("Consumo rechazado"), "queda registrado en bitacora");
        Verificador.verificar(salida.contains("VERIFICADO"), "verificacion cumplida");
    }

    private static void escenarioCFunciona() {
        String salida = correr(new EscenarioC());
        Verificador.verificar(salida.contains("[Disponible -> Preparando salto]"), "inicia la preparacion");
        Verificador.verificar(salida.contains("[En warp -> Enfriamiento] Salto warp: consume 5 de energia y avanza 2 casillas"), "salto warp");
        Verificador.verificar(salida.contains("[Enfriamiento -> Disponible]"), "vuelve a Disponible");
        Verificador.verificar(salida.contains("Preparacion detenida: faltan 5 de energia"), "sin energia se queda preparando");
        Verificador.verificar(salida.contains("Salto warp abortado"), "aborto del salto");
        Verificador.verificar(salida.contains("VERIFICADO: abortado"), "el aborto se verifica");
        Verificador.verificar(salida.contains("VERIFICADO: desgaste 3"), "enfriamiento verificado");
    }

    private static void escenarioDFunciona() {
        String salida = correr(new EscenarioD());
        Verificador.verificar(salida.contains("Capturada RecursoFueraDeRangoException"), "captura excepcion de rango");
        Verificador.verificar(salida.contains("Capturada IllegalArgumentException"), "captura argumento invalido");
        Verificador.verificar(salida.contains("VERIFICADO"), "verificacion cumplida");
    }

    private static void escenarioNavegacionFunciona() {
        String salida = correr(new EscenarioNavegacion());
        Verificador.verificar(salida.contains("(1,0) con asteroide -> Asteroide"), "el radar detecta el asteroide");
        Verificador.verificar(salida.contains("VERIFICADO: llego a (19,19)"), "el viaje llega al destino");
    }

    private static void escenarioUniversoFunciona() {
        String salida = correr(new EscenarioCentroDeControl());
        Verificador.verificar(salida.contains("Registradas: [NAVE-1, NAVE-2, NAVE-3]"), "alta de tres naves");
        Verificador.verificar(salida.contains("Activa tras seleccionar NAVE-2: NAVE-2"), "una sola activa");
        Verificador.verificar(salida.contains("Lista de solo lectura: 3 naves"), "lista para los controladores");
        Verificador.verificar(salida.contains("VERIFICADO"), "la verificacion se cumple");
    }

    private static void pendienteListaPasosYDuenos() {
        Escenario e = new EscenarioPendiente("X", new String[] {"paso uno", "paso dos"}, new String[] {"Eneas: algo"});
        String salida = correr(e);
        Verificador.verificar(salida.contains("PENDIENTE DE INTEGRACION"), "se declara pendiente");
        Verificador.verificar(salida.contains("1. paso uno") && salida.contains("2. paso dos"), "lista los pasos");
        Verificador.verificar(salida.contains("Eneas: algo"), "indica quien lo aporta");
    }

    private static void viajeEsReproducible() {
        String[] resumen = new String[2];
        for (int i = 0; i < 2; i++) {
            modelo.tablero.TableroStub t = modelo.tablero.TableroStub.conElementosDePrueba(42L, 30, 5, 5);
            NaveSimulada nave = new NaveSimulada("N", 100, 100, new modelo.navegacion.Coordenada(0, 0), t);
            SimulacionDeViaje v = new SimulacionDeViaje(t, nave, new modelo.navegacion.Coordenada(19, 19), 0);
            v.run();
            resumen[i] = v.getTurnos() + "/" + v.getSaltosWarp() + "/" + nave.getCombustible() + "/" + nave.getEnergia() + "/" + nave.getDesgaste();
        }
        Verificador.igual(resumen[0], resumen[1], "dos viajes con la misma semilla");
    }
}
