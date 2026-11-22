package modelo.tablero;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import destinoytablero.CantidadDeElementosSuperaCasillaException;
import destinoytablero.Destino;
import destinoytablero.DimensionesDeTableroInconsistentesException;
import destinoytablero.Posicion;
import modelo.elementos.Asteroide;
import modelo.elementos.ElementoDelUniverso;
import modelo.elementos.EstacionDeCarga;
import modelo.fabrica.NaveFactory;
import modelo.nave.TipoNave;
import modelo.navegacion.Coordenada;
import util.observer.Sujeto;

/**
 * Patron Adapter entre la interfaz {@link modelo.tablero.Tablero} del dominio y las
 * clases provistas por la catedra ({@link destinoytablero.Tablero} y {@link destinoytablero.Destino}).
 *
 * Permite que radares, asistente, misiones y TableroPanel trabajen contra el tablero
 * oficial sin acoplarse a el ni modificar el codigo provisto por la catedra.
 *
 * Seguridad para hilos: las operaciones sobre el tablero de la catedra se sincronizan
 * con un candado interno y las notificaciones a observadores se despachan fuera de el.
 */
public class AdaptadorTableroDestino extends Sujeto implements Tablero {

    private final Object candado = new Object();
    private final destinoytablero.Tablero tableroCatedra;
    private final Destino destinoCatedra;
    private final List<String> mensajesPendientes = new ArrayList<>();
    private boolean capturandoMensajes;
    private Coordenada destinoMarcado;

    public AdaptadorTableroDestino(destinoytablero.Tablero tableroCatedra, Destino destinoCatedra) {
        this.tableroCatedra = Objects.requireNonNull(tableroCatedra, "El tablero de la catedra no puede ser null");
        this.destinoCatedra = Objects.requireNonNull(destinoCatedra, "Destino no puede ser null");
        this.tableroCatedra.agregarObservador(this::recibirNotificacionCatedra);
    }

    /**
     * Construye un universo 20x20 con elementos reales del dominio (asteroides,
     * estaciones de carga y naves enemigas de los tres tipos creadas por fabrica).
     */
    public static AdaptadorTableroDestino crearUniversoPorDefecto(NaveFactory factory) {
        return crearUniverso(factory, 18, 6, 9);
    }

    /**
     * Construye un tablero oficial 20x20 poblado con la cantidad indicada de
     * asteroides, estaciones de carga y naves enemigas.
     */
    public static AdaptadorTableroDestino crearUniverso(
            NaveFactory factory, int asteroides, int estaciones, int navesEnemigas) {
        Objects.requireNonNull(factory, "La NaveFactory no puede ser null");
        if (asteroides < 0 || estaciones < 0 || navesEnemigas < 0) {
            throw new IllegalArgumentException("Las cantidades de elementos no pueden ser negativas");
        }
        ArrayList<ElementoDelUniverso> elementos = new ArrayList<>();
        for (int i = 0; i < asteroides; i++) {
            elementos.add(new Asteroide());
        }
        for (int i = 0; i < estaciones; i++) {
            elementos.add(i % 2 == 0 ? new EstacionDeCarga() : EstacionDeCarga.porPorcentaje(30));
        }
        TipoNave[] tipos = TipoNave.values();
        for (int i = 0; i < navesEnemigas; i++) {
            TipoNave tipo = tipos[i % tipos.length];
            elementos.add(factory.crearNaveEnemiga(tipo, "ENE-" + (i + 1)));
        }
        return desdeElementos(elementos);
    }

    /**
     * Construye el adaptador a partir de una lista explicita de elementos del dominio.
     */
    public static AdaptadorTableroDestino desdeElementos(List<ElementoDelUniverso> elementos) {
        Objects.requireNonNull(elementos, "La lista de elementos no puede ser null");
        try {
            destinoytablero.Tablero tablero = new destinoytablero.Tablero(
                    DIMENSION, DIMENSION, new ArrayList<>(elementos));
            Destino destino = new Destino(tablero);
            return new AdaptadorTableroDestino(tablero, destino);
        } catch (DimensionesDeTableroInconsistentesException | CantidadDeElementosSuperaCasillaException e) {
            throw new IllegalArgumentException("No se pudo construir el tablero oficial: " + e.getMessage(), e);
        }
    }

    private void recibirNotificacionCatedra(String mensaje) {
        boolean despacharAhora;
        synchronized (candado) {
            if (capturandoMensajes) {
                mensajesPendientes.add(mensaje);
                despacharAhora = false;
            } else {
                despacharAhora = true;
            }
        }
        if (despacharAhora) {
            notificarObservadores(mensaje);
        }
    }

    private void despacharPendientes(List<String> mensajes) {
        for (String mensaje : mensajes) {
            notificarObservadores(mensaje);
        }
    }

    private List<String> ejecutarConNotificacionesDiferidas(Runnable accion) {
        List<String> mensajes;
        synchronized (candado) {
            capturandoMensajes = true;
            try {
                accion.run();
            } finally {
                capturandoMensajes = false;
                mensajes = new ArrayList<>(mensajesPendientes);
                mensajesPendientes.clear();
            }
        }
        despacharPendientes(mensajes);
        return mensajes;
    }

    /**
     * Solicita al tablero de la catedra elegir una nueva casilla objetivo libre
     * y la devuelve traducida a {@link Coordenada}.
     */
    public Coordenada generarObjetivoAleatorio() {
        final Coordenada[] elegida = new Coordenada[1];
        ejecutarConNotificacionesDiferidas(() -> {
            Posicion objetivo = tableroCatedra.obtenerObjetivo();
            Coordenada coord = aCoordenada(objetivo);
            destinoMarcado = coord;
            elegida[0] = coord;
        });
        return elegida[0];
    }

    public destinoytablero.Tablero getTableroCatedra() {
        return tableroCatedra;
    }

    @Override
    public boolean esValida(Coordenada c) {
        return c != null
                && c.getX() >= 0 && c.getX() < tableroCatedra.getX()
                && c.getY() >= 0 && c.getY() < tableroCatedra.getY();
    }

    @Override
    public ElementoDePrueba getElementoEn(Coordenada c) {
        exigirValida(c);
        ElementoDelUniverso elemento;
        synchronized (candado) {
            Posicion posNave = tableroCatedra.getPosicionNave();
            int dx = c.getX() - posNave.getX();
            int dy = c.getY() - posNave.getY();
            if (dx == 0 && dy == 0) {
                elemento = tableroCatedra.inspeccionarPosicion();
            } else if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
                ElementoDelUniverso[][] alrededores = tableroCatedra.observarAlrededores();
                elemento = alrededores[dx + 1][dy + 1];
            } else {
                ElementoDelUniverso[][] matriz = tableroCatedra.verTablero();
                elemento = matriz[c.getX()][c.getY()];
            }
        }
        return elemento == null ? null : ElementoDePrueba.representar(elemento);
    }

    @Override
    public void colocar(ElementoDePrueba elemento, Coordenada c) {
        throw new UnsupportedOperationException(
                "El tablero oficial de la catedra define sus elementos en el constructor.");
    }

    @Override
    public ElementoDePrueba retirar(Coordenada c) {
        throw new UnsupportedOperationException(
                "El tablero oficial de la catedra no admite retirar elementos manualmente.");
    }

    @Override
    public void moverElementos() {
        ejecutarConNotificacionesDiferidas(destinoCatedra::turnoDestino);
    }

    @Override
    public void moverNave(Coordenada c) {
        exigirValida(c);
        ejecutarConNotificacionesDiferidas(() -> tableroCatedra.moverNave(aPosicion(c)));
    }

    @Override
    public Coordenada getPosicionNave() {
        synchronized (candado) {
            Posicion p = tableroCatedra.getPosicionNave();
            return p == null ? null : aCoordenada(p);
        }
    }

    @Override
    public void marcarDestino(Coordenada c) {
        exigirValida(c);
        synchronized (candado) {
            destinoMarcado = c;
        }
        notificarObservadores("Destino fijado en (" + c.getX() + ", " + c.getY() + ").");
    }

    @Override
    public Coordenada getDestino() {
        synchronized (candado) {
            if (destinoMarcado != null) {
                return destinoMarcado;
            }
            Posicion objetivo = tableroCatedra.getObjetivo();
            return objetivo == null ? null : aCoordenada(objetivo);
        }
    }

    @Override
    public InstantaneaDelTablero copiaParaDibujar() {
        synchronized (candado) {
            ElementoDelUniverso[][] matriz = tableroCatedra.verTablero();
            int filas = tableroCatedra.getX();
            int columnas = tableroCatedra.getY();
            String[][] etiquetas = new String[filas][columnas];
            for (int x = 0; x < filas; x++) {
                for (int y = 0; y < columnas; y++) {
                    if (matriz[x][y] != null) {
                        etiquetas[x][y] = matriz[x][y].getNombre();
                    }
                }
            }
            Posicion posNave = tableroCatedra.getPosicionNave();
            Coordenada nave = posNave == null ? null : aCoordenada(posNave);
            Coordenada destino = destinoMarcado != null
                    ? destinoMarcado
                    : (tableroCatedra.getObjetivo() == null ? null : aCoordenada(tableroCatedra.getObjetivo()));
            return new InstantaneaDelTablero(etiquetas, nave, destino);
        }
    }

    private void exigirValida(Coordenada c) {
        if (!esValida(c)) {
            throw new IllegalArgumentException("Coordenada fuera del tablero: " + c);
        }
    }

    private static Posicion aPosicion(Coordenada c) {
        return new Posicion(c.getX(), c.getY());
    }

    private static Coordenada aCoordenada(Posicion p) {
        return new Coordenada(p.getX(), p.getY());
    }
}
