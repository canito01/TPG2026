package vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import modelo.asistente.Evento;
import modelo.asistente.ResumenRecursos;

/** Muestra los recursos y la bitácora de la nave seleccionada. */
public final class RecursosBitacoraPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final JLabel nave = new JLabel("Seleccioná una nave");
    private final JLabel combustible = new JLabel();
    private final JLabel energia = new JLabel();
    private final JLabel desgaste = new JLabel();
    private final JLabel mantenimiento = new JLabel();
    private final JTextArea bitacora = new JTextArea(12, 44);

    public RecursosBitacoraPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel cabecera = new JPanel(new BorderLayout(0, 8));
        cabecera.add(nave, BorderLayout.NORTH);
        JPanel recursos = new JPanel(new GridLayout(0, 2, 16, 6));
        recursos.setBorder(BorderFactory.createTitledBorder("Recursos"));
        recursos.add(combustible);
        recursos.add(energia);
        recursos.add(desgaste);
        recursos.add(mantenimiento);
        cabecera.add(recursos, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);

        bitacora.setEditable(false);
        bitacora.setLineWrap(true);
        bitacora.setWrapStyleWord(true);
        JScrollPane historial = new JScrollPane(bitacora);
        historial.setBorder(BorderFactory.createTitledBorder("Bitácora"));
        add(historial, BorderLayout.CENTER);

        mostrarRecursos(null);
    }

    public void mostrarNave(String id) {
        nave.setText(id == null ? "Seleccioná una nave" : "Nave " + id);
    }

    public void mostrarRecursos(ResumenRecursos recursos) {
        if (recursos == null) {
            combustible.setText("Combustible: —");
            energia.setText("Energía: —");
            desgaste.setText("Desgaste: —");
            mantenimiento.setText("Mantenimiento: —");
            return;
        }

        combustible.setText("Combustible: " + recursos.getCombustible() + " / 100");
        energia.setText("Energía: " + recursos.getEnergia() + " / 100");
        desgaste.setText("Desgaste: " + recursos.getDesgaste() + " / 100");
        mantenimiento.setText(recursos.requiereMantenimiento()
                ? "Mantenimiento requerido" : "Mantenimiento al día");
    }

    public void mostrarBitacora(List<Evento> eventos) {
        StringBuilder texto = new StringBuilder();
        for (Evento evento : eventos) {
            if (texto.length() > 0) {
                texto.append('\n');
            }
            texto.append(evento.getFecha().format(FORMATO_FECHA))
                    .append("  ").append(evento.getDescripcion());
        }
        bitacora.setText(texto.toString());
        bitacora.setCaretPosition(bitacora.getDocument().getLength());
    }
}
