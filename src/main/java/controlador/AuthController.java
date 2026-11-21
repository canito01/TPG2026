package controlador;

import persistencia.dao.PersistenciaException;
import persistencia.dto.UsuarioDTO;
import servicio.AutenticacionException;
import servicio.AutenticacionService;
import vista.LoginPanel;
import vista.RegistroPanel;

import java.util.function.Consumer;

/**
 * Controlador MVC para las pantallas de Login y Registro.
 * Recibe eventos de las vistas, invoca al AutenticacionService y coordina la respuesta.
 */
public class AuthController {

    private final LoginPanel loginPanel;
    private final RegistroPanel registroPanel;
    private final AutenticacionService authService;
    private final Runnable mostrarLogin;
    private final Runnable mostrarRegistro;
    private final Consumer<UsuarioDTO> onLoginExitoso;

    public AuthController(
            LoginPanel loginPanel,
            RegistroPanel registroPanel,
            AutenticacionService authService,
            Runnable mostrarLogin,
            Runnable mostrarRegistro,
            Consumer<UsuarioDTO> onLoginExitoso) {
        this.loginPanel = loginPanel;
        this.registroPanel = registroPanel;
        this.authService = authService;
        this.mostrarLogin = mostrarLogin;
        this.mostrarRegistro = mostrarRegistro;
        this.onLoginExitoso = onLoginExitoso;

        conectarEventos();
    }

    private void conectarEventos() {
        loginPanel.agregarListenerLogin(e -> manejarLogin());
        loginPanel.agregarListenerIrARegistro(e -> {
            registroPanel.limpiarFormulario();
            mostrarRegistro.run();
        });

        registroPanel.agregarListenerRegistrar(e -> manejarRegistro());
        registroPanel.agregarListenerVolverLogin(e -> {
            loginPanel.limpiarFormulario();
            mostrarLogin.run();
        });
    }

    private void manejarLogin() {
        if (!loginPanel.validarFormato()) {
            return;
        }
        try {
            UsuarioDTO usuario = authService.iniciarSesion(
                    loginPanel.getNombreUsuario(),
                    loginPanel.getPassword()
            );
            loginPanel.limpiarFormulario();
            onLoginExitoso.accept(usuario);
        } catch (AutenticacionException ex) {
            loginPanel.mostrarError(ex.getMessage());
        } catch (PersistenciaException ex) {
            loginPanel.mostrarError("Error al acceder a la base de datos.");
        }
    }

    private void manejarRegistro() {
        if (!registroPanel.validarFormato()) {
            return;
        }
        try {
            UsuarioDTO creado = authService.registrar(
                    registroPanel.getNombreUsuario(),
                    registroPanel.getPassword()
            );
            registroPanel.limpiarFormulario();
            loginPanel.limpiarFormulario();
            loginPanel.mostrarExito("Usuario '" + creado.getNombreUsuario() + "' registrado. Ya podés iniciar sesión.");
            mostrarLogin.run();
        } catch (AutenticacionException ex) {
            registroPanel.mostrarError(ex.getMessage());
        } catch (PersistenciaException ex) {
            registroPanel.mostrarError("Error al guardar el usuario en la base de datos.");
        }
    }
}
