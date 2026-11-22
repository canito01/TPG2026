package pruebas;

import java.util.ArrayList;
import java.util.List;

import modelo.asistente.AsistenteBase;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.PosicionNave;
import modelo.elementos.Asteroide;
import modelo.elementos.ElementoDelUniverso;
import modelo.elementos.EstacionDeCarga;
import modelo.fabrica.NaveFactory;
import modelo.misiones.InformeMision;
import modelo.misiones.Mision2;
import modelo.nave.Nave;
import modelo.nave.TipoNave;
import modelo.navegacion.Coordenada;
import modelo.navegacion.RadarCortoAlcance;
import modelo.navegacion.RadarLargoAlcance;
import modelo.navegacion.Ruta;
import modelo.tablero.AdaptadorTableroDestino;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.InstantaneaDelTablero;
import modelo.tablero.Tablero;
import util.observer.Observador;

/**
 * Verifica la integracion del modulo oficial Destino/Tablero de la catedra
 * a traves de {@link AdaptadorTableroDestino}.
 */
public final class PruebasAdaptadorDestino {

    private PruebasAdaptadorDestino() {
    }

    public static void correrTodas() {
        System.out.println("Adaptador Tablero/Destino (catedra)");
        try { universoPorDefecto(); Verificador.ok("universo por defecto: nave en el centro y elementos reales distribuidos"); }
        catch (Throwable t) { Verificador.fallo("universo por defecto", t); }
        try { turnoDestinoNotifica(); Verificador.ok("turno de Destino conserva los elementos y notifica a observadores"); }
        catch (Throwable t) { Verificador.fallo("turno de Destino", t); }
        try { objetivoYMovimiento(); Verificador.ok("obtener objetivo aleatorio y mover nave actualizan la instantanea"); }
        catch (Throwable t) { Verificador.fallo("objetivo y movimiento", t); }
        try { interaccionConElementoReal(); Verificador.ok("interaccion real con Asteroide a traves del adaptador"); }
        catch (Throwable t) { Verificador.fallo("interaccion real", t); }
        try { radaresYMisionSobreAdaptador(); Verificador.ok("radares y mision completa funcionan sobre el tablero oficial adaptado"); }
        catch (Throwable t) { Verificador.fallo("radares y mision sobre adaptador", t); }
    }

    private static void universoPorDefecto() {
        NaveFactory factory = new NaveFactory();
        AdaptadorTableroDestino tablero = AdaptadorTableroDestino.crearUniverso(factory, 10, 4, 6);
        Verificador.igual(new Coordenada(10, 10), tablero.getPosicionNave(), "nave inicia en (10,10)");
        InstantaneaDelTablero foto = tablero.copiaParaDibujar();
        Verificador.igual(Tablero.DIMENSION, foto.getDimension(), "dimension 20x20");
        Verificador.igual(20, foto.cantidadDeElementos(), "20 elementos distribuidos sin pisarse");
    }

    private static void turnoDestinoNotifica() {
        NaveFactory factory = new NaveFactory();
        AdaptadorTableroDestino tablero = AdaptadorTableroDestino.crearUniverso(factory, 8, 4, 4);
        List<String> avisos = new ArrayList<>();
        Observador obs = avisos::add;
        tablero.agregarObservador(obs);

        tablero.moverElementos();

        Verificador.igual(16, tablero.copiaParaDibujar().cantidadDeElementos(), "no se pierden ni duplican elementos");
        Verificador.verificar(!avisos.isEmpty(), "notifico al menos un aviso");
        Verificador.verificar(avisos.get(0).contains("Destino"), "el mensaje proviene de Destino.turnoDestino()");
    }

    private static void objetivoYMovimiento() {
        NaveFactory factory = new NaveFactory();
        AdaptadorTableroDestino tablero = AdaptadorTableroDestino.crearUniverso(factory, 5, 2, 2);
        List<String> avisos = new ArrayList<>();
        tablero.agregarObservador(avisos::add);

        Coordenada objetivo = tablero.generarObjetivoAleatorio();
        Verificador.verificar(tablero.esValida(objetivo), "objetivo valido");
        Verificador.igual(objetivo, tablero.getDestino(), "getDestino refleja el objetivo generado");

        Coordenada nuevaPos = new Coordenada(10, 11);
        tablero.moverNave(nuevaPos);
        Verificador.igual(nuevaPos, tablero.getPosicionNave(), "posicion de la nave actualizada");
        Verificador.igual(nuevaPos, tablero.copiaParaDibujar().getNave(), "instantanea refleja la nave");
        Verificador.igual(2, avisos.size(), "un aviso por objetivo y uno por movimiento");
    }

    private static void interaccionConElementoReal() {
        List<ElementoDelUniverso> elementos = new ArrayList<>();
        elementos.add(new Asteroide(15));
        elementos.add(new EstacionDeCarga(10, 10));
        AdaptadorTableroDestino tablero = AdaptadorTableroDestino.desdeElementos(elementos);

        Coordenada posAsteroide = null;
        for (int x = 0; x < Tablero.DIMENSION && posAsteroide == null; x++) {
            for (int y = 0; y < Tablero.DIMENSION && posAsteroide == null; y++) {
                ElementoDePrueba e = tablero.getElementoEn(new Coordenada(x, y));
                if (e != null && Asteroide.NOMBRE.equals(e.getNombre())) {
                    posAsteroide = new Coordenada(x, y);
                }
            }
        }
        Verificador.verificar(posAsteroide != null, "se encontro el asteroide en el tablero");

        Nave nave = new NaveFactory().crear(TipoNave.EXPLORADORA, "EXP-ADAPT");
        int energiaInicial = nave.getRecursos().getEnergia();
        tablero.moverNave(posAsteroide);
        ElementoDePrueba enCasilla = tablero.getElementoEn(posAsteroide);
        String descripcion = enCasilla.interactuarCon(nave);

        Verificador.verificar(descripcion != null && descripcion.contains("Asteroide"), "descripcion del impacto");
        Verificador.igual(energiaInicial - 15, nave.getRecursos().getEnergia(), "asteroide desconto 15 de energia");
    }

    private static void radaresYMisionSobreAdaptador() {
        NaveFactory factory = new NaveFactory();
        AdaptadorTableroDestino tablero = AdaptadorTableroDestino.crearUniverso(factory, 0, 2, 0);
        Nave nave = factory.crear(TipoNave.COMBATE, "COM-ADAPT");
        nave.setBaseDeOrigen(tablero.getPosicionNave());

        RadarLargoAlcance radarLargo = new RadarLargoAlcance(tablero);
        RadarCortoAlcance radarCorto = new RadarCortoAlcance(tablero);
        Coordenada objetivo = tablero.generarObjetivoAleatorio();
        Ruta ruta = radarLargo.calcularRuta(nave.getPosicion(), objetivo);
        Verificador.verificar(ruta.getCamino().size() >= 2, "radar largo calcula ruta sobre adaptador");
        radarCorto.escanearSiguiente(ruta, nave.getPosicion());

        AsistenteDeComando asistente = AsistenteBase.conTablero(nave, tablero);
        Mision2 mision = new Mision2(new PosicionNave(objetivo.getX(), objetivo.getY()), 40) {
            @Override
            protected int pausaPorTurnoMs() {
                return 0;
            }
        };
        InformeMision informe = mision.ejecutarCiclo(asistente);
        Verificador.verificar(informe.isExitosa(), "M-02 exitosa sobre el tablero de la catedra");
        Verificador.igual(40, nave.getRecursos().getGemas(), "la nave recolecto 40 gemas");
    }
}
