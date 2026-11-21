package controlador;

import java.util.List;
import java.util.Objects;

import javax.swing.SwingUtilities;

import modelo.asistente.AsistenteDeComando;
import util.observer.Observador;
import vista.RecursosBitacoraPanel;

/** Vincula el panel con el asistente de la nave seleccionada. */
public final class RecursosBitacoraController {
    private final RecursosBitacoraPanel panel;
    private AsistenteDeComando asistente;
    private Observador observadorRecursos;
    private Observador observadorBitacora;

    public RecursosBitacoraController(RecursosBitacoraPanel panel) {
        this.panel = Objects.requireNonNull(panel);
    }

    /**
     * Vincula otro asistente y muestra su estado inicial.
     * Se llama en el hilo de Swing al cambiar la selección.
     * Pasar null limpia el panel y retira las suscripciones.
     */
    public void seleccionarAsistente(AsistenteDeComando seleccionado) {
        if (asistente == seleccionado) {
            return;
        }
        if (asistente != null) {
            asistente.quitarObservadorRecursos(observadorRecursos);
            asistente.quitarObservadorBitacora(observadorBitacora);
        }
        asistente = seleccionado;

        if (seleccionado == null) {
            observadorRecursos = null;
            observadorBitacora = null;
            panel.mostrarNave(null);
            panel.mostrarRecursos(null);
            panel.mostrarBitacora(List.of());
            return;
        }

        // Los avisos pueden venir del hilo que ejecuta una misión.
        observadorRecursos = mensaje -> SwingUtilities.invokeLater(() -> {
            if (asistente == seleccionado) {
                panel.mostrarRecursos(seleccionado.consultarRecursos());
            }
        });
        observadorBitacora = mensaje -> SwingUtilities.invokeLater(() -> {
            if (asistente == seleccionado) {
                panel.mostrarBitacora(seleccionado.consultarBitacora());
            }
        });
        seleccionado.agregarObservadorRecursos(observadorRecursos);
        seleccionado.agregarObservadorBitacora(observadorBitacora);

        panel.mostrarNave(seleccionado.getNaveId());
        panel.mostrarRecursos(seleccionado.consultarRecursos());
        panel.mostrarBitacora(seleccionado.consultarBitacora());
    }
}
