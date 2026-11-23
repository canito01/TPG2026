package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.JTableHeader;

/**
 * Paleta y helpers visuales compartidos para toda la interfaz Swing:
 * fondo azul electrico, superficies azul profundo, botones planos rosa pastel (sin degrade)
 * y aplicacion recursiva para unificar las 5 secciones sin modificar la logica de los paneles.
 */
public final class TemaVisual {

    // Paleta Azul Electrico + Rosa Pastel
    public static final Color AZUL_ELECTRICO = new Color(56, 64, 214);      // #3840D6
    public static final Color AZUL_PROFUNDO = new Color(36, 42, 162);       // #242AA2
    public static final Color AZUL_OSCURO = new Color(23, 27, 114);         // #171B72
    public static final Color AZUL_TABLA_ALT = new Color(31, 37, 140);      // #1F258C
    public static final Color AZUL_SELECCION = new Color(92, 99, 240);      // #5C63F0
    public static final Color BORDE_SUAVE = new Color(116, 125, 242);       // #747DF2

    public static final Color ROSA_PASTEL = new Color(255, 179, 217);       // #FFB3D9
    public static final Color ROSA_HOVER = new Color(255, 206, 231);        // #FFCEE7
    public static final Color ROSA_PRESSED = new Color(242, 145, 194);      // #F291C2
    public static final Color ROSA_DESHABILITADO = new Color(152, 134, 178);

    public static final Color TEXTO_BLANCO = new Color(255, 255, 255);
    public static final Color TEXTO_SUAVE = new Color(218, 224, 255);
    public static final Color TEXTO_BOTON = new Color(26, 20, 74);

    public static final Color EXITO = new Color(118, 245, 176);
    public static final Color ERROR = new Color(255, 152, 172);

    // Tipografias
    public static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 19);
    public static final Font FUENTE_SUBTITULO = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FUENTE_NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FUENTE_BOLD = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FUENTE_PEQUENA = new Font("SansSerif", Font.PLAIN, 12);

    private TemaVisual() {
    }

    /**
     * Configura Look & Feel multiplataforma sin degrades Metal y colores base globales.
     */
    public static void aplicarDefaultsSwing() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        // Anular degrades de Metal en botones, pestañas y barras
        UIManager.put("Button.gradient", null);
        UIManager.put("ToggleButton.gradient", null);
        UIManager.put("CheckBox.gradient", null);
        UIManager.put("TabbedPane.contentAreaColor", AZUL_ELECTRICO);
        UIManager.put("Panel.background", AZUL_ELECTRICO);
        UIManager.put("Panel.foreground", TEXTO_BLANCO);
        UIManager.put("Label.foreground", TEXTO_BLANCO);
        UIManager.put("Label.font", FUENTE_NORMAL);
        UIManager.put("Button.background", ROSA_PASTEL);
        UIManager.put("Button.foreground", TEXTO_BOTON);
        UIManager.put("Button.font", FUENTE_BOLD);
        UIManager.put("CheckBox.background", AZUL_ELECTRICO);
        UIManager.put("CheckBox.foreground", TEXTO_BLANCO);
        UIManager.put("ComboBox.background", AZUL_OSCURO);
        UIManager.put("ComboBox.foreground", TEXTO_BLANCO);
        UIManager.put("ComboBox.selectionBackground", ROSA_PASTEL);
        UIManager.put("ComboBox.selectionForeground", TEXTO_BOTON);
        UIManager.put("TextField.background", AZUL_OSCURO);
        UIManager.put("TextField.foreground", TEXTO_BLANCO);
        UIManager.put("TextField.caretForeground", ROSA_PASTEL);
        UIManager.put("PasswordField.background", AZUL_OSCURO);
        UIManager.put("PasswordField.foreground", TEXTO_BLANCO);
        UIManager.put("PasswordField.caretForeground", ROSA_PASTEL);
        UIManager.put("TextArea.background", AZUL_OSCURO);
        UIManager.put("TextArea.foreground", TEXTO_BLANCO);
        UIManager.put("List.background", AZUL_OSCURO);
        UIManager.put("List.foreground", TEXTO_BLANCO);
        UIManager.put("List.selectionBackground", ROSA_PASTEL);
        UIManager.put("List.selectionForeground", TEXTO_BOTON);
        UIManager.put("Table.background", AZUL_OSCURO);
        UIManager.put("Table.foreground", TEXTO_BLANCO);
        UIManager.put("Table.selectionBackground", AZUL_SELECCION);
        UIManager.put("Table.selectionForeground", TEXTO_BLANCO);
        UIManager.put("Table.gridColor", BORDE_SUAVE);
        UIManager.put("TableHeader.background", AZUL_PROFUNDO);
        UIManager.put("TableHeader.foreground", ROSA_PASTEL);
        UIManager.put("ScrollPane.background", AZUL_PROFUNDO);
        UIManager.put("Viewport.background", AZUL_OSCURO);
        UIManager.put("SplitPane.background", AZUL_ELECTRICO);
        UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
        UIManager.put("OptionPane.background", AZUL_PROFUNDO);
        UIManager.put("OptionPane.messageForeground", TEXTO_BLANCO);
    }

    /**
     * Dibuja un globo terraqueo wireframe sutil y difuminado (meridianos y paralelos
     * en doble pasada semitransparente) sobre el fondo azul electrico.
     */
    public static void dibujarFondoGlobo(Graphics g, int ancho, int alto) {
        if (ancho <= 0 || alto <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Fondo base azul electrico con resplandor radial muy suave en el centro
        float cx = ancho / 2.0f;
        float cy = alto / 2.0f;
        float radioGlow = Math.max(ancho, alto) * 0.65f;
        RadialGradientPaint fondo = new RadialGradientPaint(
                new Point2D.Float(cx, cy),
                radioGlow,
                new float[]{0.0f, 0.65f, 1.0f},
                new Color[]{
                        new Color(74, 82, 232),
                        AZUL_ELECTRICO,
                        new Color(40, 47, 182)
                });
        g2.setPaint(fondo);
        g2.fillRect(0, 0, ancho, alto);

        double rx = ancho * 0.47;
        double ry = alto * 0.47;
        Ellipse2D.Double contornoGlobo = new Ellipse2D.Double(cx - rx, cy - ry, rx * 2.0, ry * 2.0);

        // Armar contorno + meridianos y paralelos por separado para recortar paralelos al globo
        Path2D.Double meridianos = new Path2D.Double();
        meridianos.append(contornoGlobo, false);

        double[] factoresMeridiano = {0.22, 0.45, 0.67, 0.86};
        for (double f : factoresMeridiano) {
            double mx = rx * f;
            meridianos.append(new Ellipse2D.Double(cx - mx, cy - ry, mx * 2.0, ry * 2.0), false);
        }
        meridianos.moveTo(cx, cy - ry);
        meridianos.lineTo(cx, cy + ry);

        Path2D.Double paralelos = new Path2D.Double();
        double[] latitudes = {-0.76, -0.52, -0.26, 0.0, 0.26, 0.52, 0.76};
        for (double lat : latitudes) {
            double yCentro = cy + ry * lat;
            double anchoLat = rx * Math.sqrt(Math.max(0.0, 1.0 - lat * lat)) * 1.15;
            double curvatura = ry * 0.14 * (1.0 - Math.abs(lat) * 0.35);
            paralelos.moveTo(cx - anchoLat, yCentro - curvatura * 0.55);
            paralelos.quadTo(cx, yCentro + curvatura, cx + anchoLat, yCentro - curvatura * 0.55);
        }

        // Triple pasada suave para dar efecto difuminado sin lineas duras
        Color[] colores = {
                new Color(255, 195, 228, 16),
                new Color(220, 226, 255, 26),
                new Color(235, 240, 255, 50)
        };
        float[] grosores = {6.0f, 2.6f, 1.05f};
        Shape clipOriginal = g2.getClip();
        for (int i = 0; i < colores.length; i++) {
            g2.setColor(colores[i]);
            g2.setStroke(new BasicStroke(grosores[i], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setClip(clipOriginal);
            g2.draw(meridianos);
            g2.clip(contornoGlobo);
            g2.draw(paralelos);
        }
        g2.setClip(clipOriginal);

        g2.dispose();
    }

    /**
     * Estiliza un boton con relleno rosa pastel totalmente plano (sin degrade) y bordes redondeados.
     */
    public static void estilizarBotonPlano(JButton boton) {
        boton.setUI(new FlatButtonUI());
        boton.setBackground(ROSA_PASTEL);
        boton.setForeground(TEXTO_BOTON);
        boton.setFont(FUENTE_BOLD);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setRolloverEnabled(true);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setMargin(new Insets(8, 16, 8, 16));
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        if (boton.getClientProperty("temaHoverInstalado") == null) {
            boton.putClientProperty("temaHoverInstalado", Boolean.TRUE);
            boton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    boton.repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    boton.repaint();
                }
            });
        }
    }

    public static void estilizarCampoTexto(JTextField campo) {
        campo.setBackground(AZUL_OSCURO);
        campo.setForeground(TEXTO_BLANCO);
        campo.setCaretColor(ROSA_PASTEL);
        campo.setFont(FUENTE_NORMAL);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_SUAVE, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    public static void estilizarTarjeta(JPanel tarjeta) {
        tarjeta.setBackground(AZUL_PROFUNDO);
        tarjeta.setOpaque(true);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ROSA_PASTEL, 2),
                BorderFactory.createEmptyBorder(22, 26, 22, 26)
        ));
    }

    public static void estilizarTabbedPane(JTabbedPane tabs) {
        tabs.setBackground(AZUL_ELECTRICO);
        tabs.setForeground(TEXTO_BLANCO);
        tabs.setFont(FUENTE_BOLD);
        tabs.setUI(new FlatTabbedPaneUI());
    }

    public static void estilizarTabla(JTable tabla) {
        tabla.setBackground(AZUL_OSCURO);
        tabla.setForeground(TEXTO_BLANCO);
        tabla.setSelectionBackground(AZUL_SELECCION);
        tabla.setSelectionForeground(TEXTO_BLANCO);
        tabla.setGridColor(BORDE_SUAVE);
        tabla.setFont(FUENTE_NORMAL);
        tabla.setRowHeight(26);
        tabla.setFillsViewportHeight(true);

        JTableHeader header = tabla.getTableHeader();
        if (header != null) {
            header.setBackground(AZUL_PROFUNDO);
            header.setForeground(ROSA_PASTEL);
            header.setFont(FUENTE_BOLD);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ROSA_PASTEL));
        }
    }

    /**
     * Recorre recursivamente un contenedor Swing y aplica la estetica Azul Electrico + Rosa Pastel
     * a todos los paneles del equipo, respetando el lienzo propio de TableroPanel y LogoNave.
     */
    public static void aplicarATodoElArbol(Component componente) {
        if (componente == null || componente instanceof TableroPanel || componente instanceof LogoNave) {
            return;
        }

        if (componente instanceof JButton boton) {
            estilizarBotonPlano(boton);
            return;
        }
        if (componente instanceof JTabbedPane tabs) {
            estilizarTabbedPane(tabs);
        } else if (componente instanceof JPasswordField pass) {
            estilizarCampoTexto(pass);
        } else if (componente instanceof JTextField campo) {
            estilizarCampoTexto(campo);
        } else if (componente instanceof JTextArea area) {
            area.setBackground(AZUL_OSCURO);
            area.setForeground(TEXTO_BLANCO);
            area.setCaretColor(ROSA_PASTEL);
            area.setFont(FUENTE_NORMAL);
        } else if (componente instanceof JList<?> lista) {
            lista.setBackground(AZUL_OSCURO);
            lista.setForeground(TEXTO_BLANCO);
            lista.setSelectionBackground(ROSA_PASTEL);
            lista.setSelectionForeground(TEXTO_BOTON);
            lista.setFont(FUENTE_NORMAL);
        } else if (componente instanceof JTable tabla) {
            estilizarTabla(tabla);
        } else if (componente instanceof JComboBox<?> combo) {
            combo.setBackground(AZUL_OSCURO);
            combo.setForeground(TEXTO_BLANCO);
            combo.setFont(FUENTE_NORMAL);
        } else if (componente instanceof JSpinner spinner) {
            spinner.setBackground(AZUL_OSCURO);
            JComponent editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor defEditor) {
                JFormattedTextField tf = defEditor.getTextField();
                tf.setBackground(AZUL_OSCURO);
                tf.setForeground(TEXTO_BLANCO);
                tf.setCaretColor(ROSA_PASTEL);
            }
        } else if (componente instanceof JCheckBox check) {
            check.setOpaque(false);
            check.setForeground(TEXTO_BLANCO);
            check.setFont(FUENTE_NORMAL);
            check.setFocusPainted(false);
        } else if (componente instanceof JLabel lbl) {
            Color actual = lbl.getForeground();
            if (actual == null || esColorOscuroDefault(actual)) {
                lbl.setForeground(TEXTO_BLANCO);
            } else if (esRojoError(actual)) {
                lbl.setForeground(ERROR);
            } else if (esVerdeExito(actual)) {
                lbl.setForeground(EXITO);
            }
        } else if (componente instanceof JScrollPane scroll) {
            scroll.setBackground(AZUL_PROFUNDO);
            scroll.getViewport().setBackground(AZUL_OSCURO);
            estilizarBordeSiEsTitulado(scroll);
        } else if (componente instanceof JSplitPane split) {
            split.setBackground(AZUL_ELECTRICO);
            split.setBorder(BorderFactory.createEmptyBorder());
        } else if (componente instanceof JPanel panel) {
            if (Boolean.TRUE.equals(panel.getClientProperty("esTarjeta"))) {
                estilizarTarjeta(panel);
            } else if (Boolean.TRUE.equals(panel.getClientProperty("esHeader"))) {
                panel.setBackground(AZUL_OSCURO);
                panel.setOpaque(true);
            } else if (panel.getBorder() instanceof TitledBorder) {
                panel.setBackground(AZUL_PROFUNDO);
                panel.setOpaque(true);
                estilizarBordeSiEsTitulado(panel);
            } else if (panel.isOpaque()) {
                panel.setBackground(AZUL_ELECTRICO);
            }
        }

        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                aplicarATodoElArbol(hijo);
            }
        }
    }

    private static void estilizarBordeSiEsTitulado(JComponent comp) {
        Border borde = comp.getBorder();
        if (borde instanceof TitledBorder tb) {
            TitledBorder nuevo = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(ROSA_PASTEL, 1),
                    tb.getTitle(),
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    FUENTE_BOLD,
                    ROSA_PASTEL
            );
            comp.setBorder(BorderFactory.createCompoundBorder(
                    nuevo,
                    BorderFactory.createEmptyBorder(4, 6, 6, 6)
            ));
        }
    }

    private static boolean esColorOscuroDefault(Color c) {
        return c.getRed() < 115 && c.getGreen() < 120 && c.getBlue() < 140;
    }

    private static boolean esRojoError(Color c) {
        return c.getRed() > 150 && c.getGreen() < 90 && c.getBlue() < 90;
    }

    private static boolean esVerdeExito(Color c) {
        return c.getGreen() > 100 && c.getRed() < 80;
    }

    /** ButtonUI plano: elimina por completo el degrade de Metal y dibuja esquinas redondeadas. */
    private static final class FlatButtonUI extends BasicButtonUI {
        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel model = b.getModel();

            Color relleno;
            Color texto;
            if (!model.isEnabled()) {
                relleno = ROSA_DESHABILITADO;
                texto = AZUL_OSCURO;
            } else if (model.isPressed()) {
                relleno = ROSA_PRESSED;
                texto = TEXTO_BOTON;
            } else if (model.isRollover()) {
                relleno = ROSA_HOVER;
                texto = TEXTO_BOTON;
            } else {
                relleno = ROSA_PASTEL;
                texto = TEXTO_BOTON;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(relleno);
            g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
            g2.dispose();

            b.setForeground(texto);
            super.paint(g, c);
        }
    }

    /** TabbedPaneUI plano en azul electrico con pestaña activa en rosa pastel. */
    private static final class FlatTabbedPaneUI extends BasicTabbedPaneUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
            tabAreaInsets = new Insets(6, 10, 0, 10);
            tabInsets = new Insets(8, 16, 8, 16);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            contentBorderInsets = new Insets(2, 0, 0, 0);
        }

        @Override
        protected void paintTabBackground(
                Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isSelected ? ROSA_PASTEL : AZUL_PROFUNDO);
            g2.fillRoundRect(x + 2, y + 2, w - 4, h, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTabBorder(
                Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            // Sin bordes grises Metal
        }

        @Override
        protected void paintFocusIndicator(
                Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
                Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // Sin recuadro punteado de foco
        }

        @Override
        protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            g.setColor(ROSA_PASTEL);
            int top = calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
            g.fillRect(0, top, tabPane.getWidth(), 2);
        }

        @Override
        protected void paintText(
                Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                int tabIndex, String title, Rectangle textRect, boolean isSelected) {
            g.setFont(font);
            g.setColor(isSelected ? TEXTO_BOTON : TEXTO_SUAVE);
            g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
        }
    }
}
