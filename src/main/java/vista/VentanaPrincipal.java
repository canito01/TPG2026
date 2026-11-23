package vista;

import persistencia.dto.UsuarioDTO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Ventana principal del sistema (Swing).
 * Alterna entre el acceso (Login / Registro) y el panel principal con pestañas.
 * Cada pestaña es un JPanel independiente para que cada integrante enchufe su vista
 * sin conflictos de merge.
 */
public class VentanaPrincipal extends JFrame {

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_REGISTRO = "REGISTRO";
    private static final String CARD_SISTEMA = "SISTEMA";

    private static final int TAB_CREACION = 0;
    private static final int TAB_TRIPULANTES = 1;
    private static final int TAB_CONTROL_NAVE = 2;
    private static final int TAB_VISOR = 3;

    private final CardLayout cardLayout;
    private final JPanel contenedorPrincipal;

    private final LoginPanel loginPanel;
    private final RegistroPanel registroPanel;
    private final TripulantesPanel tripulantesPanel;
    private final JTabbedPane pestanas;
    private final JLabel lblUsuarioActivo;

    public VentanaPrincipal() {
        super("TPG 2026 — Sistema de Control de Nave Espacial");
        setIconImage(LogoNave.crearIconoVentana(64));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contenedorPrincipal = new JPanel(cardLayout);

        loginPanel = new LoginPanel();
        registroPanel = new RegistroPanel();
        tripulantesPanel = new TripulantesPanel();

        // Pantalla principal tras autenticarse
        JPanel panelSistema = new JPanel(new BorderLayout());

        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.putClientProperty("esHeader", Boolean.TRUE);
        barraSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, TemaVisual.ROSA_PASTEL),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        JPanel marcaIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        marcaIzquierda.setOpaque(false);
        marcaIzquierda.add(new LogoNave(82, 30, false));
        JLabel lblTituloApp = new JLabel("WEYLAND-YUTANI CORP · Control de Nave");
        lblTituloApp.setFont(TemaVisual.FUENTE_SUBTITULO);
        lblTituloApp.setForeground(TemaVisual.ROSA_PASTEL);
        marcaIzquierda.add(lblTituloApp);

        JPanel derechaHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        derechaHeader.setOpaque(false);
        lblUsuarioActivo = new JLabel("Operador: -");
        lblUsuarioActivo.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.addActionListener(e -> mostrarLogin());
        derechaHeader.add(lblUsuarioActivo);
        derechaHeader.add(btnCerrarSesion);

        barraSuperior.add(marcaIzquierda, BorderLayout.WEST);
        barraSuperior.add(derechaHeader, BorderLayout.EAST);

        pestanas = new JTabbedPane();
        pestanas.addTab("Creación", crearPlaceholder("Sección de Creación de Nave y Tripulación (en integración)"));
        pestanas.addTab("Tripulantes", tripulantesPanel);
        pestanas.addTab("Control de la Nave", crearPlaceholder("Sección de Control, Tablero, Recursos y Misiones (en integración)"));
        pestanas.addTab("Visor Recóndito", crearPlaceholder("Sección del Visor del Universo Paralelo (en integración)"));

        panelSistema.add(barraSuperior, BorderLayout.NORTH);
        panelSistema.add(pestanas, BorderLayout.CENTER);

        contenedorPrincipal.add(loginPanel, CARD_LOGIN);
        contenedorPrincipal.add(registroPanel, CARD_REGISTRO);
        contenedorPrincipal.add(panelSistema, CARD_SISTEMA);

        setContentPane(contenedorPrincipal);
        mostrarLogin();
    }

    private JPanel crearPlaceholder(String texto) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.ITALIC, 14));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    public void mostrarLogin() {
        cardLayout.show(contenedorPrincipal, CARD_LOGIN);
    }

    public void mostrarRegistro() {
        cardLayout.show(contenedorPrincipal, CARD_REGISTRO);
    }

    public void mostrarSistema(UsuarioDTO usuario) {
        lblUsuarioActivo.setText("Operador conectado: " + usuario.getNombreUsuario());
        cardLayout.show(contenedorPrincipal, CARD_SISTEMA);
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public RegistroPanel getRegistroPanel() {
        return registroPanel;
    }

    public TripulantesPanel getTripulantesPanel() {
        return tripulantesPanel;
    }

    // Metodos de integracion para enchufar los JPanel de cada integrante sin conflictos de merge
    public void setPanelCreacion(JPanel panel) {
        pestanas.setComponentAt(TAB_CREACION, panel);
    }

    public void setPanelControlNave(JPanel panel) {
        pestanas.setComponentAt(TAB_CONTROL_NAVE, panel);
    }

    public void setPanelVisor(JPanel panel) {
        pestanas.setComponentAt(TAB_VISOR, panel);
    }
}
