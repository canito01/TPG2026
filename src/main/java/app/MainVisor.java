package app;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import controlador.VisorController;
import modelo.concurrencia.UniversoParalelo;
import vista.VisorPanel;

/** Punto de entrada independiente para probar el visor del universo paralelo. */
public final class MainVisor {
    private MainVisor() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainVisor::abrirVisor);
    }

    private static void abrirVisor() {
        VisorPanel panel = new VisorPanel();
        UniversoParalelo universo = new UniversoParalelo(3, 900, 1_400);
        VisorController controlador = new VisorController(panel, universo);

        JFrame ventana = new JFrame("Universo paralelo");
        ventana.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        ventana.setContentPane(panel);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evento) {
                controlador.close();
            }
        });
        ventana.pack();
        ventana.setLocationByPlatform(true);
        ventana.setVisible(true);
    }
}
