package vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Muestra la actividad del universo paralelo y los controles para agregar entidades. */
public final class VisorPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int MAXIMO_EVENTOS_VISIBLES = 500;

    private final JButton crearNave = new JButton("Crear nave consumidora");
    private final JButton crearGeneradora = new JButton("Crear generadora de energía");
    private final DefaultListModel<String> eventos = new DefaultListModel<>();
    private final JList<String> actividad = new JList<>(eventos);
    private final JLabel estado = new JLabel();

    public VisorPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel controles = new JPanel(new GridLayout(1, 2, 8, 0));
        controles.add(crearNave);
        controles.add(crearGeneradora);
        add(controles, BorderLayout.NORTH);

        JScrollPane historial = new JScrollPane(actividad);
        historial.setPreferredSize(new Dimension(580, 280));
        historial.setBorder(BorderFactory.createTitledBorder("Actividad de la plataforma"));
        actividad.getAccessibleContext().setAccessibleName("Eventos de la plataforma");
        add(historial, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);
    }

    public void alCrearNave(Runnable accion) {
        crearNave.addActionListener(evento -> accion.run());
    }

    public void alCrearGeneradora(Runnable accion) {
        crearGeneradora.addActionListener(evento -> accion.run());
    }

    public void mostrarEvento(String mensaje) {
        if (eventos.size() == MAXIMO_EVENTOS_VISIBLES) {
            eventos.remove(0);
        }
        eventos.addElement(mensaje);
        actividad.ensureIndexIsVisible(eventos.size() - 1);
    }

    public void mostrarEstado(String mensaje) {
        estado.setText(mensaje);
    }

    public void deshabilitarControles() {
        crearNave.setEnabled(false);
        crearGeneradora.setEnabled(false);
    }
}
