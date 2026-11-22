package app;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import controlador.TableroController;
import modelo.navegacion.Coordenada;
import modelo.tablero.TableroStub;

/**
 * Demostracion grafica del tablero en tiempo real: la nave viaja de (0,0) a
 * (19,19) con el motor Warp mientras la vista se actualiza sin congelarse. El
 * viaje corre en su propio hilo; la ventana solo dibuja. Sera reemplazada por
 * la pestana de Control de la nave de la ventana principal (Nicolas).
 */
public class MainTableroSwing {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                crearVentana();
            }
        });
    }

    private static void crearVentana() {
        final TableroStub tablero = TableroStub.conElementosDePrueba(42L, 30, 5, 5);
        final NaveSimulada nave = new NaveSimulada("NAVE-GUI", 100, 100, new Coordenada(0, 0), tablero);
        final TableroController controlador = new TableroController(tablero);

        JButton turno = new JButton("Turno del tablero");
        turno.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controlador.moverTableroUnaVez();
            }
        });

        final JButton viajar = new JButton("Iniciar viaje a (19,19)");
        viajar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viajar.setEnabled(false);
                Thread hilo = new Thread(new SimulacionDeViaje(tablero, nave, new Coordenada(19, 19), 250), "viaje");
                hilo.setDaemon(true);
                hilo.start();
            }
        });

        JPanel botones = new JPanel();
        botones.add(turno);
        botones.add(viajar);

        JFrame ventana = new JFrame("Tablero en tiempo real");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controlador.cerrar();
            }
        });
        ventana.add(controlador.getVista(), BorderLayout.CENTER);
        ventana.add(botones, BorderLayout.SOUTH);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }
}
