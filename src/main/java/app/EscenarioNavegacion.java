package app;

import java.io.PrintStream;

import modelo.navegacion.Coordenada;
import modelo.navegacion.RadarCortoAlcance;
import modelo.navegacion.RadarLargoAlcance;
import modelo.navegacion.Ruta;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.InstantaneaDelTablero;
import modelo.tablero.TableroStub;

/**
 * Tablero y radares (plan E2, Joaquin): ruta del radar de largo alcance, escaneo
 * del radar de corto alcance y un viaje completo que usa motor, radares y
 * tablero juntos. Todo con semilla fija: la salida es siempre la misma.
 */
class EscenarioNavegacion extends EscenarioBase {

    @Override
    public String getNombre() { return "Tablero y radares (ruta, escaneo y viaje)"; }

    @Override
    public void ejecutar(PrintStream out) {
        TableroStub tablero = TableroStub.conElementosDePrueba(7L, 12, 3, 3);
        RadarLargoAlcance largo = new RadarLargoAlcance(tablero);
        RadarCortoAlcance corto = new RadarCortoAlcance(tablero);
        Coordenada origen = new Coordenada(0, 0);
        Coordenada destino = new Coordenada(6, 4);

        out.println("N.1 Radar de largo alcance: ruta de " + origen + " a " + destino);
        Ruta ruta = largo.calcularRuta(origen, destino);
        out.println("  " + ruta);
        exigir(ruta.pasos() == 10, "la ruta minima tiene 6 + 4 = 10 pasos");
        exigir(ruta.getOrigen().equals(origen) && ruta.getDestino().equals(destino), "la ruta une origen y destino");
        exigir(largo.getRutaActual() == ruta, "el radar almacena la ruta");

        out.println("N.2 Radar de corto alcance sobre la casilla siguiente:");
        Coordenada siguiente = ruta.siguienteDe(origen);
        tablero.retirar(siguiente);
        ElementoDePrueba nada = corto.escanearSiguiente(ruta, origen);
        out.println("  " + siguiente + " sin elemento -> " + (nada == null ? "despejado" : nada.getNombre()));
        exigir(nada == null, "la casilla despejada no informa elementos");
        tablero.colocar(new ElementoDePrueba("Asteroide"), siguiente);
        ElementoDePrueba algo = corto.escanearSiguiente(ruta, origen);
        out.println("  " + siguiente + " con asteroide -> " + algo.getNombre());
        exigir(algo != null && algo.getNombre().equals("Asteroide"), "el radar detecta el asteroide");
        tablero.retirar(siguiente);

        out.println("N.3 Copia del tablero para dibujar (@ nave, X destino, letras = elementos):");
        tablero.moverNave(origen);
        tablero.marcarDestino(destino);
        InstantaneaDelTablero foto = tablero.copiaParaDibujar();
        out.println(foto.comoTexto());
        exigir(foto.cantidadDeElementos() == 18, "la copia tiene los 18 elementos de prueba");
        tablero.moverElementos();
        exigir(foto.cantidadDeElementos() == 18 && tablero.copiaParaDibujar().cantidadDeElementos() == 18,
                "mover elementos no pierde ninguno y la copia anterior no cambia");

        out.println("N.4 Viaje de (0,0) a (19,19): saltar si esta despejado, saltarWarp si hay obstaculo");
        TableroStub universo = TableroStub.conElementosDePrueba(42L, 30, 5, 5);
        NaveSimulada nave = new NaveSimulada("NAVE-N", 100, 100, new Coordenada(0, 0), universo);
        SimulacionDeViaje viaje = new SimulacionDeViaje(universo, nave, new Coordenada(19, 19), 0);
        viaje.run();
        java.util.List<String> lineas = viaje.getRegistro();
        for (int i = 0; i < Math.min(8, lineas.size()); i++) {
            out.println("  " + lineas.get(i));
        }
        out.println("  ...");
        out.println("  " + lineas.get(lineas.size() - 1));
        out.println("  Resumen: " + viaje.getTurnos() + " turnos, " + viaje.getSaltosWarp() + " saltos warp, combustible "
                + nave.getCombustible() + ", energia " + nave.getEnergia() + ", desgaste " + nave.getDesgaste());
        exigir(viaje.haLlegado(), "la nave llego al destino");
        exigir(viaje.getSaltosWarp() > 0, "hubo al menos un salto warp esquivando obstaculos");
        out.println("  VERIFICADO: llego a (19,19)");
    }
}
