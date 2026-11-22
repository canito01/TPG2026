package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import modelo.nave.Nave;
import modelo.nave.Recursos;
import modelo.nave.TipoNave;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.Origen;
import modelo.tripulacion.Tripulante;

/**
 * Vista de la Sección de Creación (MVC):
 *  - Permite seleccionar el {@link TipoNave} e instanciar la nave.
 *  - Permite crear tripulantes y asignarlos a la nave.
 *  - Realiza las validaciones de formato (campos vacíos y formato numérico entero)
 *    antes de delegar la acción en el controlador.
 */
public class CreacionPanel extends JPanel {

    /** Callback para la acción de alta de tripulante validada en formato. */
    public interface AccionAgregarTripulante {
        void ejecutar(String identidad, Cargo cargo, Origen origen, int antiguedad, int consejos);
    }

    private static final Color COLOR_EXITO = new Color(25, 135, 84);
    private static final Color COLOR_ERROR = new Color(200, 45, 45);

    private final JComboBox<TipoNave> cmbTipoNave;
    private final JTextField txtIdNave;
    private final JButton btnCrearNave;
    private final JLabel lblResumenNave;
    private final JLabel lblEstadoTripulacion;

    private final JTextField txtIdentidad;
    private final JComboBox<Cargo> cmbCargo;
    private final JComboBox<Origen> cmbOrigen;
    private final JTextField txtAntiguedad;
    private final JTextField txtConsejos;
    private final JButton btnAgregarTripulante;

    private final DefaultTableModel modeloTablaTripulantes;
    private final JLabel lblMensaje;

    private BiConsumer<TipoNave, String> accionCrearNave;
    private AccionAgregarTripulante accionAgregarTripulante;

    public CreacionPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel lblTitulo = new JLabel("Creación de Nave y Conformación de Tripulación");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        add(lblTitulo, BorderLayout.NORTH);

        // 1. Panel superior: selección e instanciación de Nave
        JPanel panelNave = new JPanel(new BorderLayout(8, 8));
        panelNave.setBorder(BorderFactory.createTitledBorder("1. Configurar e Instanciar Nave"));

        JPanel controlesNave = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        cmbTipoNave = new JComboBox<>(TipoNave.values());
        txtIdNave = new JTextField("EXP-01", 10);
        btnCrearNave = new JButton("Instanciar Nave");

        controlesNave.add(new JLabel("Tipo de Nave:"));
        controlesNave.add(cmbTipoNave);
        controlesNave.add(new JLabel("Identificador:"));
        controlesNave.add(txtIdNave);
        controlesNave.add(btnCrearNave);

        JPanel infoNave = new JPanel(new BorderLayout(4, 4));
        infoNave.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 10));
        lblResumenNave = new JLabel("Nave actual: ninguna instanciada.");
        lblResumenNave.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEstadoTripulacion = new JLabel("Requisitos de misión: pendiente (se requiere 1 Capitán y 4 tripulantes adicionales).");
        infoNave.add(lblResumenNave, BorderLayout.NORTH);
        infoNave.add(lblEstadoTripulacion, BorderLayout.SOUTH);

        panelNave.add(controlesNave, BorderLayout.NORTH);
        panelNave.add(infoNave, BorderLayout.SOUTH);

        // 2. Panel central: formulario de alta de tripulante + listado
        JPanel panelCentro = new JPanel(new BorderLayout(10, 10));

        JPanel formTripulante = new JPanel(new GridBagLayout());
        formTripulante.setBorder(BorderFactory.createTitledBorder("2. Nuevo Tripulante"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtIdentidad = new JTextField(14);
        cmbCargo = new JComboBox<>(Cargo.values());
        cmbOrigen = new JComboBox<>(Origen.values());
        txtAntiguedad = new JTextField("0", 5);
        txtConsejos = new JTextField("0", 5);
        btnAgregarTripulante = new JButton("Asignar Tripulante");

        gbc.gridx = 0; gbc.gridy = 0;
        formTripulante.add(new JLabel("Identidad:"), gbc);
        gbc.gridx = 1;
        formTripulante.add(txtIdentidad, gbc);

        gbc.gridx = 2;
        formTripulante.add(new JLabel("Cargo:"), gbc);
        gbc.gridx = 3;
        formTripulante.add(cmbCargo, gbc);

        gbc.gridx = 4;
        formTripulante.add(new JLabel("Origen:"), gbc);
        gbc.gridx = 5;
        formTripulante.add(cmbOrigen, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formTripulante.add(new JLabel("Antigüedad (años):"), gbc);
        gbc.gridx = 1;
        formTripulante.add(txtAntiguedad, gbc);

        gbc.gridx = 2;
        formTripulante.add(new JLabel("Consejos (si es Consejero):"), gbc);
        gbc.gridx = 3;
        formTripulante.add(txtConsejos, gbc);

        gbc.gridx = 4; gbc.gridwidth = 2;
        formTripulante.add(btnAgregarTripulante, gbc);

        modeloTablaTripulantes = new DefaultTableModel(
                new Object[]{"Identidad", "Cargo", "Origen", "Antigüedad", "Consejos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloTablaTripulantes);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Tripulación asignada a la nave"));

        panelCentro.add(formTripulante, BorderLayout.NORTH);
        panelCentro.add(scrollTabla, BorderLayout.CENTER);

        // 3. Barra inferior de mensajes
        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel contenedor = new JPanel(new BorderLayout(10, 10));
        contenedor.add(panelNave, BorderLayout.NORTH);
        contenedor.add(panelCentro, BorderLayout.CENTER);
        contenedor.add(lblMensaje, BorderLayout.SOUTH);

        add(contenedor, BorderLayout.CENTER);

        configurarListenersDeVista();
    }

    private void configurarListenersDeVista() {
        btnCrearNave.addActionListener(e -> {
            String id = txtIdNave.getText() != null ? txtIdNave.getText().trim() : "";
            if (id.isEmpty()) {
                mostrarError("Error de formato: el identificador de la nave no puede estar vacío.");
                return;
            }
            TipoNave tipo = (TipoNave) cmbTipoNave.getSelectedItem();
            if (tipo == null) {
                mostrarError("Error de formato: debe seleccionar un tipo de nave.");
                return;
            }
            if (accionCrearNave != null) {
                accionCrearNave.accept(tipo, id);
            }
        });

        btnAgregarTripulante.addActionListener(e -> {
            String identidad = txtIdentidad.getText() != null ? txtIdentidad.getText().trim() : "";
            if (identidad.isEmpty()) {
                mostrarError("Error de formato: el nombre/identidad del tripulante no puede estar vacío.");
                return;
            }
            int antiguedad;
            try {
                antiguedad = Integer.parseInt(txtAntiguedad.getText().trim());
            } catch (NumberFormatException ex) {
                mostrarError("Error de formato: la antigüedad debe ser un número entero válido.");
                return;
            }
            int consejos;
            try {
                String textoConsejos = txtConsejos.getText() != null ? txtConsejos.getText().trim() : "0";
                consejos = textoConsejos.isEmpty() ? 0 : Integer.parseInt(textoConsejos);
            } catch (NumberFormatException ex) {
                mostrarError("Error de formato: la cantidad de consejos debe ser un número entero válido.");
                return;
            }
            Cargo cargo = (Cargo) cmbCargo.getSelectedItem();
            Origen origen = (Origen) cmbOrigen.getSelectedItem();
            if (accionAgregarTripulante != null) {
                accionAgregarTripulante.ejecutar(identidad, cargo, origen, antiguedad, consejos);
            }
        });
    }

    public void setAccionCrearNave(BiConsumer<TipoNave, String> accionCrearNave) {
        this.accionCrearNave = accionCrearNave;
    }

    public void setAccionAgregarTripulante(AccionAgregarTripulante accionAgregarTripulante) {
        this.accionAgregarTripulante = accionAgregarTripulante;
    }

    public void mostrarNave(Nave nave, boolean tripulacionValida) {
        ejecutarEnEDT(() -> {
            if (nave == null) {
                lblResumenNave.setText("Nave actual: ninguna instanciada.");
                lblEstadoTripulacion.setText("Requisitos de misión: pendiente.");
                modeloTablaTripulantes.setRowCount(0);
                return;
            }
            Recursos r = nave.getRecursos();
            lblResumenNave.setText("Nave activa: " + nave.getId() + " [" + nave.getTipo()
                    + "] — Combustible: " + r.getCombustible()
                    + ", Energía: " + r.getEnergia()
                    + ", Desgaste: " + r.getDesgaste());
            actualizarTablaYEstadoTripulacion(nave.getTripulacion(), tripulacionValida);
        });
    }

    public void actualizarTripulacion(List<Tripulante> tripulantes, boolean tripulacionValida) {
        ejecutarEnEDT(() -> actualizarTablaYEstadoTripulacion(tripulantes, tripulacionValida));
    }

    private void actualizarTablaYEstadoTripulacion(List<Tripulante> tripulantes, boolean tripulacionValida) {
        modeloTablaTripulantes.setRowCount(0);
        if (tripulantes != null) {
            for (Tripulante t : tripulantes) {
                modeloTablaTripulantes.addRow(new Object[]{
                        t.getIdentidad(),
                        t.getCargo(),
                        t.getOrigen(),
                        t.getAntiguedad(),
                        t.getCantidadConsejos()
                });
            }
        }
        int total = tripulantes != null ? tripulantes.size() : 0;
        if (tripulacionValida) {
            lblEstadoTripulacion.setText("Requisitos de misión: COMPLETO (" + total + " tripulantes con Capitán a bordo).");
            lblEstadoTripulacion.setForeground(COLOR_EXITO);
        } else {
            lblEstadoTripulacion.setText("Requisitos de misión: INCOMPLETO (" + total
                    + "/5 tripulantes; requiere al menos 1 CAPITAN y 4 adicionales).");
            lblEstadoTripulacion.setForeground(COLOR_ERROR);
        }
    }

    public void limpiarFormularioTripulante() {
        txtIdentidad.setText("");
        txtAntiguedad.setText("0");
        txtConsejos.setText("0");
    }

    public void mostrarExito(String mensaje) {
        ejecutarEnEDT(() -> {
            lblMensaje.setForeground(COLOR_EXITO);
            lblMensaje.setText(mensaje);
        });
    }

    public void mostrarError(String mensaje) {
        ejecutarEnEDT(() -> {
            lblMensaje.setForeground(COLOR_ERROR);
            lblMensaje.setText(mensaje);
        });
    }

    public String getMensajeActual() {
        return lblMensaje.getText();
    }

    public int getCantidadTripulantesEnTabla() {
        return modeloTablaTripulantes.getRowCount();
    }

    public void seleccionarTipoNave(TipoNave tipo) {
        cmbTipoNave.setSelectedItem(tipo);
    }

    public void setTextoIdNave(String id) {
        txtIdNave.setText(id);
    }

    public void completarFormularioTripulante(String identidad, Cargo cargo, Origen origen, String antiguedad, String consejos) {
        txtIdentidad.setText(identidad);
        cmbCargo.setSelectedItem(cargo);
        cmbOrigen.setSelectedItem(origen);
        txtAntiguedad.setText(antiguedad);
        txtConsejos.setText(consejos);
    }

    public void clickCrearNave() {
        btnCrearNave.doClick();
    }

    public void clickAgregarTripulante() {
        btnAgregarTripulante.doClick();
    }

    private static void ejecutarEnEDT(Runnable tarea) {
        if (SwingUtilities.isEventDispatchThread()) {
            tarea.run();
        } else {
            SwingUtilities.invokeLater(tarea);
        }
    }
}
