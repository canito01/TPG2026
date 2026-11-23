package vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

/**
 * Emblema vectorial estilo Weyland-Yutani Corp recolorado con la paleta
 * azul electrico + rosa pastel + blanco suave del sistema.
 */
public class LogoNave extends JComponent {

    private static final Color COLOR_CUNAS = new Color(230, 234, 255);

    private final Color colorPrincipal;
    private final Color colorCunas;
    private final Color colorTexto;
    private final boolean conTextos;

    public LogoNave(int ancho, int alto) {
        this(ancho, alto, TemaVisual.ROSA_PASTEL, COLOR_CUNAS, TemaVisual.TEXTO_BLANCO, true);
    }

    public LogoNave(int ancho, int alto, boolean conTextos) {
        this(ancho, alto, TemaVisual.ROSA_PASTEL, COLOR_CUNAS, TemaVisual.TEXTO_BLANCO, conTextos);
    }

    public LogoNave(int ancho, int alto, Color colorPrincipal, Color colorCunas, Color colorTexto, boolean conTextos) {
        this.colorPrincipal = colorPrincipal;
        this.colorCunas = colorCunas;
        this.colorTexto = colorTexto;
        this.conTextos = conTextos;
        Dimension dim = new Dimension(ancho, alto);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        dibujarLogo(g2, getWidth(), getHeight(), colorPrincipal, colorCunas, colorTexto,
                TemaVisual.AZUL_ELECTRICO, conTextos, false);
        g2.dispose();
    }

    /** Genera el icono cuadrado de ventana para la barra de tareas. */
    public static Image crearIconoVentana(int tamano) {
        BufferedImage img = new BufferedImage(tamano, tamano, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        dibujarLogo(g2, tamano, tamano, TemaVisual.ROSA_PASTEL, COLOR_CUNAS, TemaVisual.TEXTO_BLANCO,
                TemaVisual.AZUL_ELECTRICO, false, true);
        g2.dispose();
        return img;
    }

    private static void dibujarLogo(
            Graphics2D g2,
            int ancho,
            int alto,
            Color colorW,
            Color colorCunas,
            Color colorTexto,
            Color colorFondo,
            boolean incluirTextos,
            boolean pintarFondoRedondeado) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (pintarFondoRedondeado) {
            g2.setColor(colorFondo);
            int arc = Math.max(8, Math.min(ancho, alto) / 4);
            g2.fillRoundRect(0, 0, ancho, alto, arc, arc);
        }

        double refW = 240.0;
        double refH = incluirTextos ? 112.0 : 80.0;
        double pad = pintarFondoRedondeado ? Math.min(ancho, alto) * 0.12 : 2.0;
        double dispW = Math.max(1.0, ancho - 2 * pad);
        double dispH = Math.max(1.0, alto - 2 * pad);
        double escala = Math.min(dispW / refW, dispH / refH);
        double offsetX = (ancho - refW * escala) / 2.0;
        double offsetY = (alto - refH * escala) / 2.0;

        g2.translate(offsetX, offsetY);
        g2.scale(escala, escala);

        double yBase = incluirTextos ? 22.0 : 5.0;

        if (incluirTextos) {
            dibujarTextoEspaciado(
                    g2,
                    "WEYLAND-YUTANI CORP",
                    new Font("SansSerif", Font.BOLD, 14),
                    colorTexto,
                    6.0,
                    234.0,
                    14.0);
        }

        // Emblema W geometrico en rosa pastel (sin degrade)
        Path2D.Double wPath = new Path2D.Double();
        wPath.moveTo(6.0, yBase + 0.0);
        wPath.lineTo(42.0, yBase + 0.0);
        wPath.lineTo(74.0, yBase + 38.0);
        wPath.lineTo(106.0, yBase + 0.0);
        wPath.lineTo(134.0, yBase + 0.0);
        wPath.lineTo(166.0, yBase + 38.0);
        wPath.lineTo(198.0, yBase + 0.0);
        wPath.lineTo(234.0, yBase + 0.0);
        wPath.lineTo(234.0, yBase + 10.0);
        wPath.lineTo(182.0, yBase + 68.0);
        wPath.lineTo(150.0, yBase + 68.0);
        wPath.lineTo(120.0, yBase + 33.0);
        wPath.lineTo(90.0, yBase + 68.0);
        wPath.lineTo(58.0, yBase + 68.0);
        wPath.lineTo(6.0, yBase + 10.0);
        wPath.closePath();

        g2.setColor(colorW);
        g2.fill(wPath);

        // Tres cunas geometricas en blanco azulado suave
        g2.setColor(colorCunas);

        // Cuna superior izquierda
        Path2D.Double cunaIzq = new Path2D.Double();
        cunaIzq.moveTo(57.0, yBase + 0.0);
        cunaIzq.lineTo(91.0, yBase + 0.0);
        cunaIzq.lineTo(91.0, yBase + 7.5);
        cunaIzq.lineTo(74.0, yBase + 26.0);
        cunaIzq.lineTo(57.0, yBase + 7.5);
        cunaIzq.closePath();
        g2.fill(cunaIzq);

        // Cuna superior derecha
        Path2D.Double cunaDer = new Path2D.Double();
        cunaDer.moveTo(149.0, yBase + 0.0);
        cunaDer.lineTo(183.0, yBase + 0.0);
        cunaDer.lineTo(183.0, yBase + 7.5);
        cunaDer.lineTo(166.0, yBase + 26.0);
        cunaDer.lineTo(149.0, yBase + 7.5);
        cunaDer.closePath();
        g2.fill(cunaDer);

        // Cuna inferior central
        Path2D.Double cunaCentro = new Path2D.Double();
        cunaCentro.moveTo(120.0, yBase + 44.0);
        cunaCentro.lineTo(136.5, yBase + 62.0);
        cunaCentro.lineTo(136.5, yBase + 68.0);
        cunaCentro.lineTo(103.5, yBase + 68.0);
        cunaCentro.lineTo(103.5, yBase + 62.0);
        cunaCentro.closePath();
        g2.fill(cunaCentro);

        if (incluirTextos) {
            dibujarTextoEspaciado(
                    g2,
                    "BUILDING BETTER WORLDS",
                    new Font("SansSerif", Font.PLAIN, 10),
                    colorCunas,
                    22.0,
                    218.0,
                    yBase + 85.0);
        }
    }

    private static void dibujarTextoEspaciado(
            Graphics2D g2, String texto, Font fuente, Color color, double xInicio, double xFin, double yBase) {
        g2.setFont(fuente);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        int totalAncho = 0;
        for (int i = 0; i < texto.length(); i++) {
            totalAncho += fm.charWidth(texto.charAt(i));
        }
        double espacioDisponible = (xFin - xInicio) - totalAncho;
        double pasoExtra = texto.length() > 1 ? Math.max(0.0, espacioDisponible / (texto.length() - 1)) : 0.0;
        double x = xInicio;
        for (int i = 0; i < texto.length(); i++) {
            String ch = String.valueOf(texto.charAt(i));
            g2.drawString(ch, (float) x, (float) yBase);
            x += fm.charWidth(texto.charAt(i)) + pasoExtra;
        }
    }
}
