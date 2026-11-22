package pruebas;

import modelo.navegacion.Coordenada;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.InstantaneaDelTablero;
import modelo.tablero.Tablero;
import modelo.tablero.TableroStub;
import util.observer.Observador;

/** Pruebas del tablero (stub): reglas, determinismo, copia independiente, observadores y varios hilos. */
public class PruebasTablero {

    /** Observador de prueba: cuenta los avisos. */
    private static class Contador implements Observador {
        volatile int avisos;
        @Override
        public void actualizar(String mensaje) { avisos++; }
    }

    public static void correrTodas() {
        System.out.println("Tablero");
        try { dimensionYValidez(); Verificador.ok("20x20: validez de coordenadas"); }
        catch (Throwable t) { Verificador.fallo("20x20", t); }
        try { colocarRetirarYOcupada(); Verificador.ok("colocar, retirar y rechazo de casilla ocupada o fuera del tablero"); }
        catch (Throwable t) { Verificador.fallo("colocar y retirar", t); }
        try { conElementosDePrueba(); Verificador.ok("elementos de prueba: cantidades y esquinas libres"); }
        catch (Throwable t) { Verificador.fallo("elementos de prueba", t); }
        try { moverConservaElementosYEsReproducible(); Verificador.ok("mover: conserva los elementos y es reproducible con la misma semilla"); }
        catch (Throwable t) { Verificador.fallo("mover", t); }
        try { copiaIndependiente(); Verificador.ok("la copia para dibujar no cambia cuando cambia el tablero"); }
        catch (Throwable t) { Verificador.fallo("copia independiente", t); }
        try { observadoresNotificados(); Verificador.ok("observadores: avisos por cada cambio y baja"); }
        catch (Throwable t) { Verificador.fallo("observadores", t); }
        try { variosHilos(); Verificador.ok("varios hilos moviendo y copiando: nunca se pierde ni duplica un elemento"); }
        catch (Throwable t) { Verificador.fallo("varios hilos", t); }
    }

    private static void dimensionYValidez() {
        TableroStub t = new TableroStub(1L);
        Verificador.igual(20, Tablero.DIMENSION, "dimension");
        Verificador.verificar(t.esValida(new Coordenada(0, 0)) && t.esValida(new Coordenada(19, 19)), "esquinas validas");
        Verificador.verificar(!t.esValida(new Coordenada(20, 0)) && !t.esValida(new Coordenada(0, -1)), "fuera");
        Verificador.verificar(!t.esValida(null), "null no es valida");
    }

    private static void colocarRetirarYOcupada() {
        TableroStub t = new TableroStub(1L);
        ElementoDePrueba e = new ElementoDePrueba("Asteroide");
        t.colocar(e, new Coordenada(2, 3));
        Verificador.verificar(t.getElementoEn(new Coordenada(2, 3)) == e, "queda en la casilla");
        try { t.colocar(new ElementoDePrueba("Estacion"), new Coordenada(2, 3)); Verificador.fallar("ocupada"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
        try { t.colocar(e, new Coordenada(20, 3)); Verificador.fallar("fuera"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
        try { t.colocar(null, new Coordenada(1, 1)); Verificador.fallar("elemento nulo"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
        try { t.getElementoEn(new Coordenada(-1, 0)); Verificador.fallar("consultar fuera"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
        Verificador.verificar(t.retirar(new Coordenada(2, 3)) == e, "retirar devuelve lo que habia");
        Verificador.verificar(t.getElementoEn(new Coordenada(2, 3)) == null, "queda vacia");
        Verificador.verificar(t.retirar(new Coordenada(2, 3)) == null, "retirar de una casilla vacia devuelve null");
    }

    private static void conElementosDePrueba() {
        TableroStub t = TableroStub.conElementosDePrueba(5L, 10, 3, 4);
        InstantaneaDelTablero f = t.copiaParaDibujar();
        Verificador.igual(17, f.cantidadDeElementos(), "10 + 3 + 4");
        Verificador.verificar(f.getEtiquetaEn(0, 0) == null && f.getEtiquetaEn(19, 19) == null, "las esquinas quedan libres");
        try { TableroStub.conElementosDePrueba(5L, 90, 20, 0); Verificador.fallar("mas de 100"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
        try { TableroStub.conElementosDePrueba(5L, -1, 0, 0); Verificador.fallar("negativo"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
    }

    private static void moverConservaElementosYEsReproducible() {
        TableroStub a = TableroStub.conElementosDePrueba(9L, 25, 5, 5);
        TableroStub b = TableroStub.conElementosDePrueba(9L, 25, 5, 5);
        Verificador.igual(a.copiaParaDibujar().comoTexto(), b.copiaParaDibujar().comoTexto(), "misma semilla, mismo tablero inicial");
        for (int i = 0; i < 100; i++) {
            a.moverElementos();
            b.moverElementos();
            Verificador.igual(35, a.copiaParaDibujar().cantidadDeElementos(), "elementos tras el turno " + i);
        }
        Verificador.igual(a.copiaParaDibujar().comoTexto(), b.copiaParaDibujar().comoTexto(), "misma semilla, mismo recorrido");
    }

    private static void copiaIndependiente() {
        TableroStub t = TableroStub.conElementosDePrueba(3L, 20, 0, 0);
        t.moverNave(new Coordenada(1, 1));
        t.marcarDestino(new Coordenada(8, 8));
        InstantaneaDelTablero antes = t.copiaParaDibujar();
        String textoAntes = antes.comoTexto();
        for (int i = 0; i < 10; i++) {
            t.moverElementos();
        }
        t.moverNave(new Coordenada(2, 2));
        Verificador.igual(textoAntes, antes.comoTexto(), "la copia vieja no cambio");
        Verificador.igual(new Coordenada(1, 1), antes.getNave(), "la copia guarda la nave de ese instante");
        Verificador.igual(new Coordenada(8, 8), antes.getDestino(), "y el destino");
        Verificador.verificar(!textoAntes.equals(t.copiaParaDibujar().comoTexto()), "el tablero si cambio");
    }

    private static void observadoresNotificados() {
        TableroStub t = new TableroStub(1L);
        Contador c = new Contador();
        t.agregarObservador(c);
        t.agregarObservador(c);   // dos veces: se avisa una sola
        t.colocar(new ElementoDePrueba("Asteroide"), new Coordenada(1, 1));
        Verificador.igual(1, c.avisos, "tras colocar");
        t.moverElementos();
        Verificador.igual(2, c.avisos, "tras mover elementos");
        t.moverNave(new Coordenada(0, 0));
        t.marcarDestino(new Coordenada(5, 5));
        Verificador.igual(4, c.avisos, "tras mover la nave y marcar el destino");
        t.retirar(new Coordenada(1, 1));
        Verificador.verificar(c.avisos >= 5, "tras retirar");
        int antes = c.avisos;
        t.quitarObservador(c);
        t.moverElementos();
        Verificador.igual(antes, c.avisos, "tras la baja ya no recibe avisos");
        try { t.agregarObservador(null); Verificador.fallar("observador nulo"); }
        catch (IllegalArgumentException ex) { /* esperado */ }
    }

    private static void variosHilos() throws Exception {
        final TableroStub t = TableroStub.conElementosDePrueba(11L, 40, 8, 8);
        Thread[] hilos = new Thread[4];
        for (int i = 0; i < hilos.length; i++) {
            hilos[i] = new Thread(new Runnable() {
                public void run() {
                    for (int k = 0; k < 150; k++) {
                        t.moverElementos();
                        t.moverNave(new Coordenada(k % 20, 0));
                    }
                }
            });
            hilos[i].start();
        }
        for (int k = 0; k < 300; k++) {   // mientras tanto, copias desde este hilo
            Verificador.igual(56, t.copiaParaDibujar().cantidadDeElementos(), "elementos en la copia " + k);
        }
        for (int i = 0; i < hilos.length; i++) {
            hilos[i].join();
        }
        Verificador.igual(56, t.copiaParaDibujar().cantidadDeElementos(), "elementos al final");
    }
}
