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
import controlador.RecursosBitacoraController;
import controlador.TableroController;
import controlador.VisorController;
import modelo.asistente.AsistenteBase;
import modelo.concurrencia.UniversoParalelo;
import modelo.fabrica.NaveFactory;
import modelo.nave.Nave;
import modelo.tablero.Tablero;
import modelo.tablero.TableroStub;
import persistencia.dao.ConexionBD;
import persistencia.dao.UsuarioDAO;
import persistencia.dao.UsuarioDAOJdbc;
import servicio.AutenticacionService;
import vista.CreacionPanel;
import vista.RecursosBitacoraPanel;
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
            VentanaPrincipal ventana = new VentanaPrincipal();
            conectarModulos(ventana);
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
        Tablero tablero = new TableroStub(0L);
        TableroController controlTablero = new TableroController(tablero);
        RecursosBitacoraPanel recursos = new RecursosBitacoraPanel();
        RecursosBitacoraController controlRecursos = new RecursosBitacoraController(recursos);
        JSplitPane division = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlTablero.getVista(), recursos);
        division.setResizeWeight(0.55);
        JPanel controlNave = new JPanel(new BorderLayout());
        controlNave.add(division, BorderLayout.CENTER);
        ventana.setPanelControlNave(controlNave);

        CreacionPanel creacion = new CreacionPanel();
        new CreacionController(creacion, new NaveFactory(), new Consumer<Nave>() {
            private Nave naveActiva;

            @Override
            public void accept(Nave nave) {
                if (nave != naveActiva) {
                    controlRecursos.seleccionarAsistente(AsistenteBase.conTablero(nave, tablero));
                    naveActiva = nave;
                }
                ventana.getTripulantesPanel().actualizarTripulantes(nave.getTripulacion());
            }
        });
        ventana.setPanelCreacion(creacion);

        VisorPanel visor = new VisorPanel();
        VisorController controlVisor = new VisorController(visor, new UniversoParalelo(3, 900, 1_400));
        ventana.setPanelVisor(visor);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evento) {
                controlVisor.close();
                controlRecursos.seleccionarAsistente(null);
                controlTablero.cerrar();
            }
        });
    }
}
