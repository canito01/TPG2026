package pruebas;

import java.util.ArrayList;
import java.util.List;

import modelo.navegacion.Coordenada;
import modelo.navegacion.PlanificadorDeRuta;
import modelo.navegacion.RadarCortoAlcance;
import modelo.navegacion.RadarLargoAlcance;
import modelo.navegacion.Ruta;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.TableroStub;

/** Pruebas de coordenadas, rutas, planificador (BFS) y radares. */
public class PruebasNavegacion {

    public static void correrTodas() {
        System.out.println("Navegacion");
        try { coordenadaIgualdadYAdyacencia(); Verificador.ok("coordenada: igualdad, hash y adyacencia ortogonal"); }
        catch (Throwable t) { Verificador.fallo("coordenada", t); }
        try { rutaValidaEInvalida(); Verificador.ok("ruta: rechaza vacia, nula y saltos no adyacentes; es inmutable"); }
        catch (Throwable t) { Verificador.fallo("ruta valida e invalida", t); }
        try { rutaAvanzar(); Verificador.ok("ruta: avanzar, siguiente y tope en el destino"); }
        catch (Throwable t) { Verificador.fallo("ruta avanzar", t); }
        try { planificadorLongitudMinima(); Verificador.ok("planificador: ruta sin obstaculos de longitud minima (Manhattan)"); }
        catch (Throwable t) { Verificador.fallo("planificador", t); }
        try { planificadorCasosBorde(); Verificador.ok("planificador: origen = destino y coordenadas fuera del tablero"); }
        catch (Throwable t) { Verificador.fallo("planificador casos borde", t); }
        try { radarLargoAlmacenaLaRuta(); Verificador.ok("radar de largo alcance calcula y almacena la ruta"); }
        catch (Throwable t) { Verificador.fallo("radar largo", t); }
        try { radarCortoEscanea(); Verificador.ok("radar de corto alcance: despejado, obstaculo y destino"); }
        catch (Throwable t) { Verificador.fallo("radar corto", t); }
    }

    private static void coordenadaIgualdadYAdyacencia() {
        Verificador.verificar(new Coordenada(3, 4).equals(new Coordenada(3, 4)), "iguales");
        Verificador.igual(new Coordenada(3, 4).hashCode(), new Coordenada(3, 4).hashCode(), "hash");
        Verificador.verificar(!new Coordenada(3, 4).equals(new Coordenada(4, 3)), "distintas");
        Verificador.verificar(new Coordenada(3, 4).esAdyacenteA(new Coordenada(3, 5)), "vertical adyacente");
        Verificador.verificar(!new Coordenada(3, 4).esAdyacenteA(new Coordenada(4, 5)), "la diagonal no es adyacente");
        Verificador.verificar(!new Coordenada(3, 4).esAdyacenteA(new Coordenada(3, 4)), "la misma no es adyacente");
        Verificador.igual("(3,4)", new Coordenada(3, 4).toString(), "toString");
    }

    private static void rutaValidaEInvalida() {
        try { new Ruta(null); Verificador.fallar("ruta nula"); } catch (IllegalArgumentException e) { /* esperado */ }
        try { new Ruta(new ArrayList<Coordenada>()); Verificador.fallar("ruta vacia"); } catch (IllegalArgumentException e) { /* esperado */ }
        List<Coordenada> salto = new ArrayList<Coordenada>();
        salto.add(new Coordenada(0, 0));
        salto.add(new Coordenada(2, 0));
        try { new Ruta(salto); Verificador.fallar("salto de 2 casillas"); } catch (IllegalArgumentException e) { /* esperado */ }
        List<Coordenada> bien = new ArrayList<Coordenada>();
        bien.add(new Coordenada(0, 0));
        bien.add(new Coordenada(1, 0));
        Ruta r = new Ruta(bien);
        bien.add(new Coordenada(2, 0));   // modificar la lista original no cambia la ruta
        Verificador.igual(1, r.pasos(), "la ruta copio la lista");
        try { r.getCamino().add(new Coordenada(2, 0)); Verificador.fallar("camino de solo lectura"); }
        catch (UnsupportedOperationException e) { /* esperado */ }
    }

    private static void rutaAvanzar() {
        TableroStub t = new TableroStub(0L);
        Ruta r = new PlanificadorDeRuta(t).planificar(new Coordenada(0, 0), new Coordenada(4, 0));
        Verificador.igual(new Coordenada(1, 0), r.siguienteDe(new Coordenada(0, 0)), "siguiente");
        Verificador.igual(new Coordenada(2, 0), r.avanzarDesde(new Coordenada(0, 0), 2), "avanzar 2");
        Verificador.igual(new Coordenada(4, 0), r.avanzarDesde(new Coordenada(3, 0), 2), "tope en el destino");
        Verificador.igual(new Coordenada(4, 0), r.siguienteDe(new Coordenada(4, 0)), "en el destino sigue en el destino");
        Verificador.igual(3, r.indiceDe(new Coordenada(3, 0)), "indice");
        try { r.siguienteDe(new Coordenada(9, 9)); Verificador.fallar("fuera de la ruta"); } catch (IllegalArgumentException e) { /* esperado */ }
        try { r.avanzarDesde(new Coordenada(0, 0), -1); Verificador.fallar("negativo"); } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void planificadorLongitudMinima() {
        PlanificadorDeRuta p = new PlanificadorDeRuta(new TableroStub(0L));
        int[][] pares = {{0, 0, 19, 19}, {0, 0, 19, 0}, {19, 19, 0, 0}, {5, 7, 2, 3}, {0, 19, 19, 0}, {10, 10, 11, 10}};
        for (int i = 0; i < pares.length; i++) {
            Coordenada o = new Coordenada(pares[i][0], pares[i][1]);
            Coordenada d = new Coordenada(pares[i][2], pares[i][3]);
            Ruta r = p.planificar(o, d);
            int manhattan = Math.abs(o.getX() - d.getX()) + Math.abs(o.getY() - d.getY());
            Verificador.igual(manhattan, r.pasos(), "pasos de " + o + " a " + d);
            Verificador.igual(o, r.getOrigen(), "origen");
            Verificador.igual(d, r.getDestino(), "destino");
        }
    }

    private static void planificadorCasosBorde() {
        PlanificadorDeRuta p = new PlanificadorDeRuta(new TableroStub(0L));
        Ruta misma = p.planificar(new Coordenada(5, 5), new Coordenada(5, 5));
        Verificador.igual(0, misma.pasos(), "origen = destino: 0 pasos");
        Verificador.igual(1, misma.getCamino().size(), "una sola coordenada");
        try { p.planificar(new Coordenada(0, 0), new Coordenada(20, 0)); Verificador.fallar("destino fuera"); }
        catch (IllegalArgumentException e) { /* esperado */ }
        try { p.planificar(null, new Coordenada(0, 0)); Verificador.fallar("origen nulo"); }
        catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void radarLargoAlmacenaLaRuta() {
        RadarLargoAlcance radar = new RadarLargoAlcance(new TableroStub(0L));
        Verificador.verificar(radar.getRutaActual() == null, "sin ruta al principio");
        Ruta r1 = radar.calcularRuta(new Coordenada(0, 0), new Coordenada(3, 3));
        Verificador.verificar(radar.getRutaActual() == r1, "almacena la ruta calculada");
        Ruta r2 = radar.calcularRuta(new Coordenada(3, 3), new Coordenada(0, 0));
        Verificador.verificar(radar.getRutaActual() == r2 && r2 != r1, "una ruta nueva reemplaza a la anterior");
    }

    private static void radarCortoEscanea() {
        TableroStub t = new TableroStub(0L);
        RadarCortoAlcance radar = new RadarCortoAlcance(t);
        Ruta ruta = new RadarLargoAlcance(t).calcularRuta(new Coordenada(0, 0), new Coordenada(3, 0));
        Verificador.verificar(radar.escanearSiguiente(ruta, new Coordenada(0, 0)) == null, "casilla despejada");
        t.colocar(new ElementoDePrueba("Asteroide"), new Coordenada(1, 0));
        ElementoDePrueba e = radar.escanearSiguiente(ruta, new Coordenada(0, 0));
        Verificador.verificar(e != null && e.getNombre().equals("Asteroide"), "detecta el asteroide adelante");
        Verificador.verificar(radar.escanearSiguiente(ruta, new Coordenada(2, 0)) == null, "mas atras no hay nada adelante");
        Verificador.verificar(radar.escanearSiguiente(ruta, new Coordenada(3, 0)) == null, "en el destino no hay casilla siguiente");
        Verificador.verificar(radar.escanear(new Coordenada(1, 0)) != null, "escanear una casilla ocupada");
        try { radar.escanear(new Coordenada(-1, 0)); Verificador.fallar("fuera del tablero"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
    }
}
