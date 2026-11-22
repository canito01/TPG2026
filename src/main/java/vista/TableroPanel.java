package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import modelo.navegacion.Coordenada;
import modelo.tablero.InstantaneaDelTablero;
import modelo.tablero.Tablero;
import util.observer.Observador;

/**
 * Vista del tablero en tiempo real (MVC + Observer). La vista SOLO dibuja:
 *  - se suscribe al tablero como Observador;
 *  - cada aviso llega desde el hilo que cambio el modelo (la mision), asi que
 *    NO toca la pantalla ahi: pide con SwingUtilities.invokeLater que el hilo
 *    de Swing tome una COPIA del tablero y repinte;
 *  - dibuja siempre la copia, nunca la estructura que mueve el motor.
 */
public class TableroPanel extends JPanel implements Observador {

    static final int CELDA = 26;
    static final Color VACIA = new Color(245, 247, 250);
    static final Color ASTEROIDE = new Color(120, 120, 120);
    static final Color ESTACION = new Color(46, 160, 90);
    static final Color ENEMIGA = new Color(200, 60, 60);
    static final Color OTRO = new Color(230, 150, 40);
    private static final Color NAVE = new Color(30, 90, 200);
    private static final Color DESTINO = new Color(20, 140, 70);

    private final Tablero modelo;
    private InstantaneaDelTablero instantanea;   // solo se lee y escribe en el hilo de Swing

    /** Pre: modelo != null. Post: la vista queda suscripta al tablero y muestra su estado actual. */
    public TableroPanel(Tablero modelo) {
        if (modelo == null) {
            throw new IllegalArgumentException("El tablero no puede ser null");
        }
        this.modelo = modelo;
        this.instantanea = modelo.copiaParaDibujar();
        setPreferredSize(new Dimension(Tablero.DIMENSION * CELDA, Tablero.DIMENSION * CELDA));
        modelo.agregarObservador(this);
    }

    /** Aviso del tablero (puede llegar desde cualquier hilo). */
    @Override
    public void actualizar(String mensaje) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                instantanea = modelo.copiaParaDibujar();
                repaint();
            }
        });
    }

    /** Deja de observar el tablero (al cerrar la ventana). */
    public void desconectar() {
        modelo.quitarObservador(this);
    }

    /** Para pruebas: la copia que se esta dibujando. */
    InstantaneaDelTablero getInstantanea() { return instantanea; }

    static Color colorDe(String etiqueta) {
        if (etiqueta.equals("Asteroide")) {
            return ASTEROIDE;
        }
        if (etiqueta.equals("EstacionDeCarga")) {
            return ESTACION;
        }
        if (etiqueta.equals("NaveEnemiga")) {
            return ENEMIGA;
        }
        return OTRO;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        InstantaneaDelTablero foto = instantanea;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        int n = foto.getDimension();
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                String etiqueta = foto.getEtiquetaEn(x, y);
                g2.setColor(etiqueta == null ? VACIA : colorDe(etiqueta));
                g2.fillRect(x * CELDA, y * CELDA, CELDA, CELDA);
                g2.setColor(new Color(210, 215, 222));
                g2.drawRect(x * CELDA, y * CELDA, CELDA, CELDA);
                if (etiqueta != null) {
                    g2.setColor(Color.WHITE);
                    g2.drawString(etiqueta.substring(0, 1), x * CELDA + 9, y * CELDA + 17);
                }
            }
        }
        Coordenada destino = foto.getDestino();
        if (destino != null) {
            g2.setColor(DESTINO);
            g2.drawRect(destino.getX() * CELDA + 2, destino.getY() * CELDA + 2, CELDA - 4, CELDA - 4);
            g2.drawRect(destino.getX() * CELDA + 3, destino.getY() * CELDA + 3, CELDA - 6, CELDA - 6);
        }
        Coordenada nave = foto.getNave();
        if (nave != null) {
            g2.setColor(NAVE);
            g2.fillOval(nave.getX() * CELDA + 4, nave.getY() * CELDA + 4, CELDA - 8, CELDA - 8);
        }
        g2.dispose();
    }
}
