package pruebas;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.NaveSimulada;
import modelo.motor.MotorWarp;
import modelo.motor.ResultadoDeTurno;
import modelo.navegacion.Coordenada;
import modelo.navegacion.PlanificadorDeRuta;
import modelo.navegacion.Ruta;
import modelo.tablero.TableroStub;

/** Pruebas del Motor Warp reescrito (State): ciclo completo, sin energia, aborto, enfriamiento y API publica. */
public class PruebasMotorWarp {

    public static void correrTodas() {
        System.out.println("MotorWarp");
        try { cicloCompleto(); Verificador.ok("ciclo completo: Disponible > Preparando > En warp > Enfriamiento > Disponible"); }
        catch (Throwable t) { Verificador.fallo("ciclo completo", t); }
        try { saltoSimpleEnDisponible(); Verificador.ok("salto simple: avanza 1 y consume 2 de combustible"); }
        catch (Throwable t) { Verificador.fallo("salto simple", t); }
        try { saltoSimpleEnWarpSigueListo(); Verificador.ok("saltar() en warp avanza 1 y el motor sigue listo"); }
        catch (Throwable t) { Verificador.fallo("saltar() en warp", t); }
        try { preparacionSinEnergia(); Verificador.ok("preparacion sin energia: se queda preparando sin avanzar"); }
        catch (Throwable t) { Verificador.fallo("preparacion sin energia", t); }
        try { abortoDelSalto(); Verificador.ok("aborto del salto: vuelve a preparar y la preparacion se reinicia"); }
        catch (Throwable t) { Verificador.fallo("aborto del salto", t); }
        try { enfriamientoConAmbasOperaciones(); Verificador.ok("enfriamiento: saltar() y saltarWarp() cumplen el turno (3 de desgaste)"); }
        catch (Throwable t) { Verificador.fallo("enfriamiento con ambas operaciones", t); }
        try { saltoSimpleSinCombustible(); Verificador.ok("sin combustible el salto simple no mueve la nave"); }
        catch (Throwable t) { Verificador.fallo("sin combustible", t); }
        try { warpNoPasaDelDestino(); Verificador.ok("el warp se detiene en el destino (avanza solo lo que falta)"); }
        catch (Throwable t) { Verificador.fallo("warp en el destino", t); }
        try { constructorRechazaNaveNula(); Verificador.ok("el motor exige una nave"); }
        catch (Throwable t) { Verificador.fallo("el motor exige una nave", t); }
        try { apiPublicaSoloDosOperaciones(); Verificador.ok("la API publica del motor es solo saltar() y saltarWarp()"); }
        catch (Throwable t) { Verificador.fallo("API publica", t); }
    }

    /** Nave de prueba en (0,0) con una ruta recta hasta (19,0). */
    static NaveSimulada nave(int combustible, int energia) {
        TableroStub t = new TableroStub(0L);
        NaveSimulada n = new NaveSimulada("T", combustible, energia, new Coordenada(0, 0), t);
        n.setRuta(new PlanificadorDeRuta(t).planificar(new Coordenada(0, 0), new Coordenada(19, 0)));
        return n;
    }

    private static void estados(ResultadoDeTurno r, String antes, String despues, int casillas) {
        Verificador.igual(antes, r.getEstadoAnterior(), "estado anterior");
        Verificador.igual(despues, r.getEstadoActual(), "estado actual");
        Verificador.igual(casillas, r.getCasillasAvanzadas(), "casillas avanzadas");
    }

    private static void cicloCompleto() {
        NaveSimulada n = nave(100, 100);
        MotorWarp m = new MotorWarp(n);
        estados(m.saltarWarp(), "Disponible", "Preparando salto", 0);
        estados(m.saltarWarp(), "Preparando salto", "En warp", 0);
        estados(m.saltarWarp(), "En warp", "Enfriamiento", 2);
        estados(m.saltarWarp(), "Enfriamiento", "Disponible", 0);
        Verificador.igual(2, n.getPosicion().getX(), "la nave avanzo 2 casillas");
        Verificador.igual(95, n.getEnergia(), "el salto consumio 5 de energia");
        Verificador.igual(100, n.getCombustible(), "el warp no consume combustible");
        Verificador.igual(3, n.getDesgaste(), "el enfriamiento genera 3 de desgaste");
    }

    private static void saltoSimpleEnDisponible() {
        NaveSimulada n = nave(100, 100);
        ResultadoDeTurno r = new MotorWarp(n).saltar();
        estados(r, "Disponible", "Disponible", 1);
        Verificador.igual(98, n.getCombustible(), "combustible");
        Verificador.igual(100, n.getEnergia(), "el salto simple no consume energia");
        Verificador.verificar(r.avanzo() && !r.cambioDeEstado(), "avanzo sin cambiar de estado");
    }

    private static void saltoSimpleEnWarpSigueListo() {
        NaveSimulada n = nave(100, 100);
        MotorWarp m = new MotorWarp(n);
        m.saltarWarp();
        m.saltarWarp();
        estados(m.saltar(), "En warp", "En warp", 1);
        Verificador.igual(98, n.getCombustible(), "combustible tras el salto simple");
        estados(m.saltarWarp(), "En warp", "Enfriamiento", 2);
        Verificador.igual(3, n.getPosicion().getX(), "1 casilla simple + 2 del warp");
    }

    private static void preparacionSinEnergia() {
        NaveSimulada n = nave(100, 4);
        MotorWarp m = new MotorWarp(n);
        estados(m.saltarWarp(), "Disponible", "Preparando salto", 0);
        for (int i = 0; i < 3; i++) {
            estados(m.saltarWarp(), "Preparando salto", "Preparando salto", 0);
        }
        estados(m.saltar(), "Preparando salto", "Preparando salto", 0);
        Verificador.igual(4, n.getEnergia(), "no se consumio energia");
        Verificador.igual(0, n.getPosicion().getX(), "la nave no avanzo");
        n.setEnergia(5);   // exactamente 5 alcanza
        estados(m.saltarWarp(), "Preparando salto", "En warp", 0);
    }

    private static void abortoDelSalto() {
        NaveSimulada n = nave(100, 5);
        MotorWarp m = new MotorWarp(n);
        m.saltarWarp();
        m.saltarWarp();                  // En warp, listo
        n.consumirEnergia(5);            // se pierde la energia antes de saltar
        estados(m.saltarWarp(), "En warp", "Preparando salto", 0);
        Verificador.igual(0, n.getPosicion().getX(), "el aborto no mueve la nave");
        n.setEnergia(10);
        estados(m.saltarWarp(), "Preparando salto", "Preparando salto", 0);   // turno 1 de 2 otra vez
        estados(m.saltarWarp(), "Preparando salto", "En warp", 0);
    }

    private static void enfriamientoConAmbasOperaciones() {
        for (int caso = 0; caso < 2; caso++) {
            NaveSimulada n = nave(100, 100);
            MotorWarp m = new MotorWarp(n);
            m.saltarWarp();
            m.saltarWarp();
            m.saltarWarp();
            ResultadoDeTurno r = (caso == 0) ? m.saltar() : m.saltarWarp();
            estados(r, "Enfriamiento", "Disponible", 0);
            Verificador.igual(3, n.getDesgaste(), "desgaste del enfriamiento (caso " + caso + ")");
            Verificador.igual(2, n.getPosicion().getX(), "no avanza mientras se enfria (caso " + caso + ")");
        }
    }

    private static void saltoSimpleSinCombustible() {
        NaveSimulada n = nave(1, 100);
        ResultadoDeTurno r = new MotorWarp(n).saltar();
        estados(r, "Disponible", "Disponible", 0);
        Verificador.igual(1, n.getCombustible(), "no se consumio nada");
        Verificador.igual(0, n.getPosicion().getX(), "la nave no avanzo");
        Verificador.verificar(r.getDescripcion().contains("combustible"), "la descripcion explica el motivo");
    }

    private static void warpNoPasaDelDestino() {
        TableroStub t = new TableroStub(0L);
        NaveSimulada n = new NaveSimulada("T", 100, 100, new Coordenada(0, 0), t);
        List<Coordenada> camino = new ArrayList<Coordenada>();
        camino.add(new Coordenada(0, 0));
        camino.add(new Coordenada(1, 0));
        n.setRuta(new Ruta(camino));
        MotorWarp m = new MotorWarp(n);
        m.saltarWarp();
        m.saltarWarp();
        estados(m.saltarWarp(), "En warp", "Enfriamiento", 1);
        Verificador.verificar(n.haLlegado(), "llego al destino");
    }

    private static void constructorRechazaNaveNula() {
        try {
            new MotorWarp(null);
            Verificador.fallar("debia rechazar la nave nula");
        } catch (IllegalArgumentException e) {
            // esperado
        }
    }

    private static void apiPublicaSoloDosOperaciones() {
        List<String> publicos = new ArrayList<String>();
        Method[] metodos = MotorWarp.class.getDeclaredMethods();
        for (int i = 0; i < metodos.length; i++) {
            if (Modifier.isPublic(metodos[i].getModifiers())) {
                publicos.add(metodos[i].getName());
            }
        }
        Collections.sort(publicos);
        Verificador.igual("[saltar, saltarWarp]", publicos.toString(), "metodos publicos del motor");
    }
}
