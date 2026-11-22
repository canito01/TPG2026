package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import modelo.asistente.PosicionNave;
import modelo.misiones.InformeMision;
import modelo.tablero.Tablero;

/**
 * Vista Swing para iniciar misiones (M-01, M-02, M-03) y consultar el historial
 * de informes finales en memoria (MVC).
 * Solo captura parametros y delega la ejecucion y reglas en {@link controlador.MisionController}.
 */
public class MisionesPanel extends JPanel {

    public enum TipoMisionUI {
        M01("M-01 — Intercepción y asistencia"),
        M02("M-02 — Recolección de gemas"),
        M03("M-03 — Retorno a punto seguro");

        private final String etiqueta;

        TipoMisionUI(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    private final JComboBox<TipoMisionUI> cmbMision;
    private final JCheckBox chkDestinoAutomatico;
    private final JSpinner spnDestinoX;
    private final JSpinner spnDestinoY;
    private final JSpinner spnAuxilioEnergia;
    private final JSpinner spnAuxilioCombustible;
    private final JButton btnIniciar;
    private final JButton btnCancelar;
    private final JLabel lblEstado;

    private final DefaultListModel<String> modeloListaInformes;
    private final JList<String> listaInformes;
    private final JTextArea txtDetalleInforme;
    private List<InformeMision> informesMostrados = Collections.emptyList();

    public MisionesPanel() {
        super(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        // Panel superior: controles de lanzamiento de mision
        JPanel panelControles = new JPanel(new GridBagLayout());
        panelControles.setBorder(BorderFactory.createTitledBorder("Control de Misiones"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelControles.add(new JLabel("Misión:"), gbc);

        cmbMision = new JComboBox<>(TipoMisionUI.values());
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        panelControles.add(cmbMision, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        chkDestinoAutomatico = new JCheckBox("Destino aleatorio del tablero", true);
        gbc.gridwidth = 2;
        panelControles.add(chkDestinoAutomatico, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 2;
        JPanel coords = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        coords.add(new JLabel("X:"));
        spnDestinoX = new JSpinner(new SpinnerNumberModel(15, 0, Tablero.DIMENSION - 1, 1));
        coords.add(spnDestinoX);
        coords.add(new JLabel("Y:"));
        spnDestinoY = new JSpinner(new SpinnerNumberModel(15, 0, Tablero.DIMENSION - 1, 1));
        coords.add(spnDestinoY);
        gbc.gridwidth = 2;
        panelControles.add(coords, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelControles.add(new JLabel("Auxilio M-01:"), gbc);

        JPanel panelAuxilio = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panelAuxilio.add(new JLabel("Energía (0-50):"));
        spnAuxilioEnergia = new JSpinner(new SpinnerNumberModel(15, 0, 50, 5));
        panelAuxilio.add(spnAuxilioEnergia);
        panelAuxilio.add(new JLabel("Comb. (0-50):"));
        spnAuxilioCombustible = new JSpinner(new SpinnerNumberModel(15, 0, 50, 5));
        panelAuxilio.add(spnAuxilioCombustible);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panelControles.add(panelAuxilio, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnIniciar = new JButton("Iniciar Misión");
        btnIniciar.setEnabled(false);
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setEnabled(false);
        panelBotones.add(btnIniciar);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        panelControles.add(panelBotones, gbc);

        lblEstado = new JLabel("Creá una nave con al menos 1 Capitán y 4 tripulantes para iniciar misiones.");
        lblEstado.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblEstado.setForeground(new Color(80, 90, 105));
        gbc.gridy = 4;
        panelControles.add(lblEstado, gbc);

        add(panelControles, BorderLayout.NORTH);

        // Panel central: historial de informes finales + detalle
        modeloListaInformes = new DefaultListModel<>();
        listaInformes = new JList<>(modeloListaInformes);
        listaInformes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaInformes.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarInformeSeleccionado();
            }
        });

        JScrollPane scrollLista = new JScrollPane(listaInformes);
        scrollLista.setBorder(BorderFactory.createTitledBorder("Historial de Informes"));

        txtDetalleInforme = new JTextArea(8, 34);
        txtDetalleInforme.setEditable(false);
        txtDetalleInforme.setLineWrap(true);
        txtDetalleInforme.setWrapStyleWord(true);
        txtDetalleInforme.setText("Todavía no se completó ninguna misión.");
        JScrollPane scrollDetalle = new JScrollPane(txtDetalleInforme);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del Informe Final"));

        JSplitPane splitHistorial = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLista, scrollDetalle);
        splitHistorial.setResizeWeight(0.35);
        add(splitHistorial, BorderLayout.CENTER);

        cmbMision.addActionListener(e -> actualizarHabilitacionCampos());
        chkDestinoAutomatico.addActionListener(e -> actualizarHabilitacionCampos());
        actualizarHabilitacionCampos();
    }

    private void actualizarHabilitacionCampos() {
        TipoMisionUI tipo = getTipoMisionSeleccionada();
        boolean esM01 = tipo == TipoMisionUI.M01;
        boolean esM03 = tipo == TipoMisionUI.M03;

        spnAuxilioEnergia.setEnabled(esM01);
        spnAuxilioCombustible.setEnabled(esM01);

        chkDestinoAutomatico.setEnabled(!esM03);
        boolean habilitarCoords = !esM03 && !chkDestinoAutomatico.isSelected();
        spnDestinoX.setEnabled(habilitarCoords);
        spnDestinoY.setEnabled(habilitarCoords);
    }

    public TipoMisionUI getTipoMisionSeleccionada() {
        return (TipoMisionUI) cmbMision.getSelectedItem();
    }

    public boolean usaDestinoAutomatico() {
        return chkDestinoAutomatico.isSelected();
    }

    public int getDestinoX() {
        return (Integer) spnDestinoX.getValue();
    }

    public int getDestinoY() {
        return (Integer) spnDestinoY.getValue();
    }

    public int getAuxilioEnergia() {
        return (Integer) spnAuxilioEnergia.getValue();
    }

    public int getAuxilioCombustible() {
        return (Integer) spnAuxilioCombustible.getValue();
    }

    public void seleccionarTipoMision(TipoMisionUI tipo) {
        cmbMision.setSelectedItem(tipo);
    }

    public void setDestinoManual(int x, int y) {
        chkDestinoAutomatico.setSelected(false);
        spnDestinoX.setValue(x);
        spnDestinoY.setValue(y);
        actualizarHabilitacionCampos();
    }

    public void setAuxilio(int energia, int combustible) {
        spnAuxilioEnergia.setValue(energia);
        spnAuxilioCombustible.setValue(combustible);
    }

    public void agregarListenerIniciar(ActionListener listener) {
        btnIniciar.addActionListener(listener);
    }

    public void agregarListenerCancelar(ActionListener listener) {
        btnCancelar.addActionListener(listener);
    }

    public void actualizarControles(boolean puedeIniciar, boolean enEjecucion) {
        btnIniciar.setEnabled(puedeIniciar && !enEjecucion);
        btnCancelar.setEnabled(enEjecucion);
        cmbMision.setEnabled(!enEjecucion);
        if (enEjecucion) {
            chkDestinoAutomatico.setEnabled(false);
            spnDestinoX.setEnabled(false);
            spnDestinoY.setEnabled(false);
            spnAuxilioEnergia.setEnabled(false);
            spnAuxilioCombustible.setEnabled(false);
        } else {
            actualizarHabilitacionCampos();
        }
    }

    public void mostrarEstadoInfo(String texto) {
        lblEstado.setForeground(new Color(40, 75, 130));
        lblEstado.setText(texto);
    }

    public void mostrarEstadoExito(String texto) {
        lblEstado.setForeground(new Color(22, 120, 56));
        lblEstado.setText(texto);
    }

    public void mostrarEstadoError(String texto) {
        lblEstado.setForeground(new Color(180, 35, 24));
        lblEstado.setText(texto);
    }

    public void mostrarHistorial(List<InformeMision> informes) {
        this.informesMostrados = informes != null ? new ArrayList<>(informes) : Collections.emptyList();
        modeloListaInformes.clear();
        for (int i = 0; i < informesMostrados.size(); i++) {
            InformeMision inf = informesMostrados.get(i);
            String estado = inf.isExitosa() ? "EXITOSA" : "FALLIDA";
            modeloListaInformes.addElement("#" + (i + 1) + " " + inf.getNombre() + " — " + estado);
        }
        if (!informesMostrados.isEmpty()) {
            listaInformes.setSelectedIndex(informesMostrados.size() - 1);
            mostrarInformeSeleccionado();
        } else {
            txtDetalleInforme.setText("Todavía no se completó ninguna misión.");
        }
    }

    private void mostrarInformeSeleccionado() {
        int idx = listaInformes.getSelectedIndex();
        if (idx < 0 || idx >= informesMostrados.size()) {
            return;
        }
        InformeMision inf = informesMostrados.get(idx);
        StringBuilder sb = new StringBuilder();
        sb.append("Misión: ").append(inf.getNombre()).append(" (")
                .append(inf.isExitosa() ? "EXITOSA" : "FALLIDA").append(")\n");
        sb.append("Objetivo: ").append(inf.getObjetivo()).append('\n');
        sb.append("Resultado: ").append(inf.getResultado()).append('\n');
        sb.append("Balance: ").append(inf.getRecursosConsumidos()).append('\n');
        sb.append("Estado final motor/nave: ").append(inf.getEstadoNave()).append('\n');
        sb.append("Observaciones: ").append(inf.getObservaciones()).append('\n');

        List<PosicionNave> ruta = inf.getRuta();
        if (!ruta.isEmpty()) {
            sb.append("Ruta (").append(ruta.size()).append(" pasos): ");
            for (int i = 0; i < ruta.size(); i++) {
                if (i > 0) sb.append(" -> ");
                sb.append('(').append(ruta.get(i).columna()).append(',').append(ruta.get(i).fila()).append(')');
            }
            sb.append('\n');
        }

        sb.append("\nEventos de la misión:\n");
        for (String accion : inf.getAcciones()) {
            sb.append(" • ").append(accion).append('\n');
        }
        txtDetalleInforme.setText(sb.toString());
        txtDetalleInforme.setCaretPosition(0);
    }

    public String getTextoEstado() {
        return lblEstado.getText();
    }

    public int getCantidadInformesMostrados() {
        return modeloListaInformes.size();
    }
}
