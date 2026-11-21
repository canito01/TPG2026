package vista;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

/**
 * Vista Swing para el inicio de sesion de operadores (MVC).
 * Solo captura datos, valida formato basico y delega la accion al AuthController.
 */
public class LoginPanel extends JPanel {

    private final JTextField txtUsuario;
    private final JPasswordField txtPassword;
    private final JButton btnIngresar;
    private final JButton btnIrARegistro;
    private final JLabel lblMensaje;

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel tarjeta = new JPanel(new BorderLayout(0, 16));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215)),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));
        tarjeta.setPreferredSize(new Dimension(380, 310));

        JLabel lblTitulo = new JLabel("Acceso al Sistema de la Nave", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        tarjeta.add(lblTitulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        formulario.add(new JLabel("Usuario:"), gbc);

        txtUsuario = new JTextField(18);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(txtUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formulario.add(new JLabel("Contraseña:"), gbc);

        txtPassword = new JPasswordField(18);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(txtPassword, gbc);

        lblMensaje = new JLabel(" ", SwingConstants.CENTER);
        lblMensaje.setForeground(new Color(180, 35, 24));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 4, 4, 4);
        formulario.add(lblMensaje, gbc);

        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new GridBagLayout());
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.insets = new Insets(4, 4, 4, 4);
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        btnGbc.weightx = 1.0;

        btnIngresar = new JButton("Iniciar sesión");
        btnGbc.gridx = 0;
        btnGbc.gridy = 0;
        acciones.add(btnIngresar, btnGbc);

        btnIrARegistro = new JButton("Crear cuenta nueva");
        btnGbc.gridy = 1;
        acciones.add(btnIrARegistro, btnGbc);

        tarjeta.add(acciones, BorderLayout.SOUTH);

        add(tarjeta);
    }

    public String getNombreUsuario() {
        return txtUsuario.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public boolean validarFormato() {
        if (getNombreUsuario().isEmpty()) {
            mostrarError("Ingresá tu nombre de usuario.");
            return false;
        }
        if (getPassword().isEmpty()) {
            mostrarError("Ingresá tu contraseña.");
            return false;
        }
        return true;
    }

    public void mostrarError(String mensaje) {
        lblMensaje.setForeground(new Color(180, 35, 24));
        lblMensaje.setText(mensaje);
    }

    public void mostrarExito(String mensaje) {
        lblMensaje.setForeground(new Color(22, 120, 56));
        lblMensaje.setText(mensaje);
    }

    public void limpiarFormulario() {
        txtUsuario.setText("");
        txtPassword.setText("");
        lblMensaje.setText(" ");
    }

    public void agregarListenerLogin(ActionListener listener) {
        btnIngresar.addActionListener(listener);
        txtPassword.addActionListener(listener);
    }

    public void agregarListenerIrARegistro(ActionListener listener) {
        btnIrARegistro.addActionListener(listener);
    }
}
