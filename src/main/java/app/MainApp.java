package app;

import controlador.AuthController;
import persistencia.dao.ConexionBD;
import persistencia.dao.UsuarioDAO;
import persistencia.dao.UsuarioDAOJdbc;
import servicio.AutenticacionService;
import vista.VentanaPrincipal;

import javax.swing.SwingUtilities;

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
}
