package controlador;

import java.util.Objects;

import javax.swing.SwingUtilities;

import modelo.concurrencia.UniversoParalelo;
import util.observer.Observador;
import vista.VisorPanel;

/** Conecta los botones y los avisos del universo paralelo. Se usa en el hilo de Swing. */
public final class VisorController implements AutoCloseable {
    private final VisorPanel panel;
    private final UniversoParalelo universo;
    private final Observador observador;
    private boolean cerrado;

    public VisorController(VisorPanel panel, UniversoParalelo universo) {
        this.panel = Objects.requireNonNull(panel);
        this.universo = Objects.requireNonNull(universo);

        observador = mensaje -> SwingUtilities.invokeLater(() -> {
            if (!cerrado) {
                panel.mostrarEvento(mensaje);
            }
        });
        universo.agregarObservador(observador);
        panel.alCrearNave(() -> agregarEntidad(universo::agregarNave));
        panel.alCrearGeneradora(() -> agregarEntidad(universo::agregarGeneradora));
    }

    private void agregarEntidad(Runnable accion) {
        try {
            accion.run();
        } catch (IllegalStateException e) {
            panel.mostrarEstado(e.getMessage());
        }
    }

    @Override
    public void close() {
        if (cerrado) {
            return;
        }
        cerrado = true;
        universo.quitarObservador(observador);
        universo.close();
        panel.deshabilitarControles();
    }
}
