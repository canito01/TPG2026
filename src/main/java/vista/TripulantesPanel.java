package vista;

import modelo.tripulacion.ItemHaber;
import modelo.tripulacion.Tripulante;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Panel de la seccion Tripulantes (MVC).
 * Muestra la tabla con los datos y salario total de cada tripulante, y al seleccionar
 * una fila muestra el desglose detallado de haberes calculado con el Decorator de E1.
 */
public class TripulantesPanel extends JPanel {

    private final DefaultTableModel modeloTripulantes;
    private final JTable tablaTripulantes;
    private final DefaultTableModel modeloDesglose;
    private final JLabel lblResumenSeleccion;
    private final JLabel lblTotalSeleccion;
    private List<Tripulante> tripulantesActuales;

    public TripulantesPanel() {
        this.tripulantesActuales = Collections.emptyList();
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel lblTitulo = new JLabel("Tripulación y Haberes");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(lblTitulo, BorderLayout.NORTH);

        modeloTripulantes = new DefaultTableModel(
                new Object[]{"Identidad", "Cargo", "Origen", "Antigüedad", "Estado", "Salario Total (PG)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaTripulantes = new JTable(modeloTripulantes);
        tablaTripulantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaTripulantes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDesgloseSeleccionado();
            }
        });

        JPanel panelTabla = new JPanel(new BorderLayout(0, 6));
        panelTabla.add(new JLabel("Listado de tripulantes asignados a la nave:"), BorderLayout.NORTH);
        panelTabla.add(new JScrollPane(tablaTripulantes), BorderLayout.CENTER);

        modeloDesglose = new DefaultTableModel(new Object[]{"Concepto", "Aporte (PG)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tablaDesglose = new JTable(modeloDesglose);
        tablaDesglose.setRowSelectionAllowed(false);

        lblResumenSeleccion = new JLabel("Seleccioná un tripulante para ver el desglose de haberes.");
        lblTotalSeleccion = new JLabel("Total: -");
        lblTotalSeleccion.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel panelDesglose = new JPanel(new BorderLayout(0, 8));
        panelDesglose.setBorder(BorderFactory.createTitledBorder("Desglose del Haber (Decorator)"));
        panelDesglose.add(lblResumenSeleccion, BorderLayout.NORTH);
        panelDesglose.add(new JScrollPane(tablaDesglose), BorderLayout.CENTER);
        panelDesglose.add(lblTotalSeleccion, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTabla, panelDesglose);
        split.setResizeWeight(0.62);
        add(split, BorderLayout.CENTER);
    }

    /**
     * Actualiza la tabla de tripulantes de forma segura sobre el Event Dispatch Thread de Swing.
     */
    public void actualizarTripulantes(List<Tripulante> tripulantes) {
        List<Tripulante> copia = tripulantes != null ? new ArrayList<>(tripulantes) : Collections.emptyList();
        Runnable tarea = () -> {
            this.tripulantesActuales = copia;
            modeloTripulantes.setRowCount(0);
            for (Tripulante t : copia) {
                modeloTripulantes.addRow(new Object[]{
                        t.getIdentidad(),
                        t.getCargo(),
                        t.getOrigen(),
                        t.getAntiguedad() + " años",
                        t.estaVivo() ? "Vivo" : "Fallecido",
                        String.format("%.2f", t.calcularHaberTotal())
                });
            }
            limpiarDesglose();
            if (!copia.isEmpty()) {
                tablaTripulantes.setRowSelectionInterval(0, 0);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            tarea.run();
        } else {
            SwingUtilities.invokeLater(tarea);
        }
    }

    private void mostrarDesgloseSeleccionado() {
        int fila = tablaTripulantes.getSelectedRow();
        if (fila < 0 || fila >= tripulantesActuales.size()) {
            limpiarDesglose();
            return;
        }
        Tripulante seleccionado = tripulantesActuales.get(fila);
        lblResumenSeleccion.setText(
                seleccionado.getIdentidad() + " — " + seleccionado.getCargo()
                        + " (" + seleccionado.getOrigen() + ")");

        modeloDesglose.setRowCount(0);
        for (ItemHaber item : seleccionado.obtenerDesgloseHaber()) {
            modeloDesglose.addRow(new Object[]{
                    item.getConcepto(),
                    String.format("%.2f", item.getAporte())
            });
        }
        lblTotalSeleccion.setText(String.format("Total: %.2f PG", seleccionado.calcularHaberTotal()));
    }

    private void limpiarDesglose() {
        modeloDesglose.setRowCount(0);
        lblResumenSeleccion.setText("Seleccioná un tripulante para ver el desglose de haberes.");
        lblTotalSeleccion.setText("Total: -");
    }
}
