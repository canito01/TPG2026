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
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

/**
 * Vista Swing para el alta de nuevos operadores (MVC).
 * Valida formato basico en la vista y delega las reglas de negocio al AuthController.
 */
public class RegistroPanel extends JPanel {

    private final JTextField txtUsuario;
    private final JPasswordField txtPassword;
    private final JPasswordField txtConfirmarPassword;
    private final JButton btnRegistrar;
    private final JButton btnVolverLogin;
    private final JLabel lblMensaje;

    public RegistroPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JPanel tarjeta = new JPanel(new BorderLayout(0, 14));
        tarjeta.putClientProperty("esTarjeta", Boolean.TRUE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.ROSA_PASTEL, 2),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));
        tarjeta.setPreferredSize(new Dimension(440, 500));

        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false);
        JPanel envoltorioLogo = new JPanel();
        envoltorioLogo.setOpaque(false);
        envoltorioLogo.add(new LogoNave(220, 96, true));
        cabecera.add(envoltorioLogo, BorderLayout.NORTH);

        JLabel lblTitulo = new JLabel("Registro de Nuevo Operador", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        cabecera.add(lblTitulo, BorderLayout.SOUTH);
        tarjeta.add(cabecera, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
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

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        formulario.add(new JLabel("Repetir contraseña:"), gbc);

        txtConfirmarPassword = new JPasswordField(18);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        formulario.add(txtConfirmarPassword, gbc);

        lblMensaje = new JLabel(" ", SwingConstants.CENTER);
        lblMensaje.setForeground(new Color(180, 35, 24));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 4, 4, 4);
        formulario.add(lblMensaje, gbc);

        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new GridBagLayout());
        acciones.setOpaque(false);
        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.insets = new Insets(4, 4, 4, 4);
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        btnGbc.weightx = 1.0;

        btnRegistrar = new JButton("Registrar usuario");
        btnGbc.gridx = 0;
        btnGbc.gridy = 0;
        acciones.add(btnRegistrar, btnGbc);

        btnVolverLogin = new JButton("Volver al inicio de sesión");
        btnGbc.gridy = 1;
        acciones.add(btnVolverLogin, btnGbc);

        tarjeta.add(acciones, BorderLayout.SOUTH);

        add(tarjeta);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        TemaVisual.dibujarFondoGlobo(g, getWidth(), getHeight());
    }

    public String getNombreUsuario() {
        return txtUsuario.getText().trim();
    }

    public String getPassword() {
        return new String(txtPassword.getPassword());
    }

    public String getConfirmacionPassword() {
        return new String(txtConfirmarPassword.getPassword());
    }

    public boolean validarFormato() {
        if (getNombreUsuario().isEmpty()) {
            mostrarError("El nombre de usuario es obligatorio.");
            return false;
        }
        if (getPassword().isEmpty()) {
            mostrarError("La contraseña es obligatoria.");
            return false;
        }
        if (!getPassword().equals(getConfirmacionPassword())) {
            mostrarError("Las contraseñas no coinciden.");
            return false;
        }
        return true;
    }

    public void mostrarError(String mensaje) {
        lblMensaje.setForeground(TemaVisual.ERROR);
        lblMensaje.setText("<html><div style='text-align:center;'>" + mensaje + "</div></html>");
    }

    public void limpiarFormulario() {
        txtUsuario.setText("");
        txtPassword.setText("");
        txtConfirmarPassword.setText("");
        lblMensaje.setText(" ");
    }

    public void agregarListenerRegistrar(ActionListener listener) {
        btnRegistrar.addActionListener(listener);
        txtConfirmarPassword.addActionListener(listener);
    }

    public void agregarListenerVolverLogin(ActionListener listener) {
        btnVolverLogin.addActionListener(listener);
    }
}
