package app;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import controlador.AuthController;
import controlador.CreacionController;
import controlador.MisionController;
import controlador.RecursosBitacoraController;
import controlador.TableroController;
import controlador.VisorController;
import modelo.asistente.AsistenteBase;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.PosicionNave;
import modelo.concurrencia.UniversoParalelo;
import modelo.fabrica.NaveFactory;
import modelo.misiones.HistorialDeInformes;
import modelo.nave.Nave;
import modelo.navegacion.Coordenada;
import modelo.tablero.AdaptadorTableroDestino;
import persistencia.dao.ConexionBD;
import persistencia.dao.UsuarioDAO;
import persistencia.dao.UsuarioDAOJdbc;
import servicio.AutenticacionService;
import vista.CreacionPanel;
import vista.MisionesPanel;
import vista.RecursosBitacoraPanel;
import vista.TemaVisual;
import vista.VentanaPrincipal;
import vista.VisorPanel;

/**
 * Punto de entrada grafico de la aplicacion para la Entrega 2.
 * Inicializa la persistencia H2, el servicio de autenticacion y la interfaz Swing (MVC).
 */
public class MainApp {

    public static void main(String[] args) {
        ConexionBD conexionBD = new ConexionBD();
        UsuarioDAO usuarioDAO = new UsuarioDAOJdbc(conexionBD);
        AutenticacionService authService = new AutenticacionService(usuarioDAO);

        SwingUtilities.invokeLater(() -> {
            TemaVisual.aplicarDefaultsSwing();
            VentanaPrincipal ventana = new VentanaPrincipal();
            conectarModulos(ventana);
            TemaVisual.aplicarATodoElArbol(ventana.getContentPane());
            new AuthController(
                    ventana.getLoginPanel(),
                    ventana.getRegistroPanel(),
                    authService,
                    ventana::mostrarLogin,
                    ventana::mostrarRegistro,
                    ventana::mostrarSistema
            );
            ventana.setVisible(true);
        });
    }

    private static void conectarModulos(VentanaPrincipal ventana) {
        NaveFactory naveFactory = new NaveFactory();
        AdaptadorTableroDestino tablero = AdaptadorTableroDestino.crearUniversoPorDefecto(naveFactory);
        TableroController controlTablero = new TableroController(tablero);

        RecursosBitacoraPanel recursos = new RecursosBitacoraPanel();
        RecursosBitacoraController controlRecursos = new RecursosBitacoraController(recursos);

        MisionesPanel misionesPanel = new MisionesPanel();
        HistorialDeInformes historialInformes = new HistorialDeInformes();

        final Nave[] referenciaNaveActiva = new Nave[1];
        MisionController controlMisiones = new MisionController(
                misionesPanel,
                historialInformes,
                () -> {
                    Coordenada objetivo = tablero.generarObjetivoAleatorio();
                    return new PosicionNave(objetivo.getX(), objetivo.getY());
                },
                () -> {
                    Nave actual = referenciaNaveActiva[0];
                    if (actual != null) {
                        ventana.getTripulantesPanel().actualizarTripulantes(actual.getTripulacion());
                    }
                }
        );

        JSplitPane panelDerecho = new JSplitPane(JSplitPane.VERTICAL_SPLIT, recursos, misionesPanel);
        panelDerecho.setResizeWeight(0.42);

        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlTablero.getVista(), panelDerecho);
        division.setResizeWeight(0.48);

        JPanel controlNave = new JPanel(new BorderLayout());
        controlNave.add(division, BorderLayout.CENTER);
        ventana.setPanelControlNave(controlNave);

        CreacionPanel creacion = new CreacionPanel();
        new CreacionController(creacion, naveFactory, new Consumer<Nave>() {
            private Nave naveActiva;
            private AsistenteDeComando asistenteActivo;

            @Override
            public void accept(Nave nave) {
                if (nave != naveActiva) {
                    if (tablero.getPosicionNave() != null) {
                        nave.setBaseDeOrigen(tablero.getPosicionNave());
                    }
                    asistenteActivo = AsistenteBase.conTablero(nave, tablero);
                    controlRecursos.seleccionarAsistente(asistenteActivo);
                    naveActiva = nave;
                    referenciaNaveActiva[0] = nave;
                }
                controlMisiones.actualizarNaveActiva(naveActiva, asistenteActivo);
                ventana.getTripulantesPanel().actualizarTripulantes(nave.getTripulacion());
            }
        });
        ventana.setPanelCreacion(creacion);

        VisorPanel visor = new VisorPanel();
        VisorController controlVisor = new VisorController(visor, new UniversoParalelo(3, 2_000, 3_000));
        ventana.setPanelVisor(visor);

        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                controlMisiones.cerrar();
                controlVisor.close();
                controlRecursos.seleccionarAsistente(null);
                controlTablero.cerrar();
            }
        });
    }
}
