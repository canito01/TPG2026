package controlador;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import modelo.concurrencia.UniversoParalelo;
import vista.VisorPanel;

/** Comprueba la simulación a través de los botones y los eventos reales del visor. */
public final class VerificacionVisor {
    private final UniversoParalelo universo = new UniversoParalelo(1, 10, 300);
    private final AtomicBoolean cambiosEnSwing = new AtomicBoolean(true);
    private VisorController controlador;
    private JButton crearNave;
    private JButton crearGeneradora;
    private JList<?> actividad;

    public static void main(String[] args) throws Exception {
        new VerificacionVisor().verificar();
        System.out.println("Visor: altas desde los botones, esperas, avisos en orden y cierre verificados.");
    }

    private void verificar() throws Exception {
        SwingUtilities.invokeAndWait(this::crearPanel);
        try {
            SwingUtilities.invokeAndWait(() -> crearNave.doClick(0));
            esperarEvento("Nave 1 espera porque la plataforma está vacía.");
            SwingUtilities.invokeAndWait(() -> crearGeneradora.doClick(0));
            esperarEvento("Nave 1 terminó de consumir una batería.");
            esperarEvento("Generadora 1 espera que se libere espacio en la plataforma.");

            List<String> mensajes = consultarMensajes();
            verificarOrden(mensajes, "Nave 1 ingresó", "Nave 1 solicita", "Nave 1 espera",
                    "Generadora 1 ingresó", "Generadora 1 produjo", "Generadora 1 depositó",
                    "Nave 1 retiró", "Nave 1 terminó de consumir");

            SwingUtilities.invokeAndWait(() -> {
                crearNave.doClick(0);
                crearGeneradora.doClick(0);
            });
            esperarEvento("Nave 2 ingresó al universo paralelo.");
            esperarEvento("Generadora 2 ingresó al universo paralelo.");
        } finally {
            SwingUtilities.invokeAndWait(controlador::close);
        }

        SwingUtilities.invokeAndWait(() ->
                comprobar(!crearNave.isEnabled() && !crearGeneradora.isEnabled(),
                        "Al cerrar el visor se deshabilitan ambos botones."));
        int eventosAlCerrar = consultarMensajes().size();
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (hayHilosDelVisor()) {
            comprobar(System.nanoTime() < limite, "El visor debe terminar los hilos al cerrar.");
            Thread.sleep(5);
        }
        comprobar(consultarMensajes().size() == eventosAlCerrar && cambiosEnSwing.get(),
                "El visor se actualiza en el hilo de Swing y deja de recibir avisos al cerrar.");
    }

    private void crearPanel() {
        VisorPanel panel = new VisorPanel();
        controlador = new VisorController(panel, universo);
        crearNave = buscarBoton(panel, "Crear nave consumidora");
        crearGeneradora = buscarBoton(panel, "Crear generadora de energía");
        actividad = buscarLista(panel);
        comprobar(crearNave != null && crearGeneradora != null && actividad != null,
                "El panel debe tener los dos controles y la lista de eventos.");
        actividad.getModel().addListDataListener(new ListDataListener() {
            @Override public void intervalAdded(ListDataEvent e) { verificarHilo(); }
            @Override public void intervalRemoved(ListDataEvent e) { verificarHilo(); }
            @Override public void contentsChanged(ListDataEvent e) { verificarHilo(); }
        });
    }

    private void verificarHilo() {
        if (!SwingUtilities.isEventDispatchThread()) {
            cambiosEnSwing.set(false);
        }
    }

    private void esperarEvento(String mensaje) throws Exception {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!consultarMensajes().contains(mensaje)) {
            comprobar(System.nanoTime() < limite, "Falta el evento " + mensaje);
            Thread.sleep(5);
        }
    }

    private List<String> consultarMensajes() throws Exception {
        AtomicReference<List<String>> resultado = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            List<String> mensajes = new ArrayList<>();
            for (int i = 0; i < actividad.getModel().getSize(); i++) {
                mensajes.add(actividad.getModel().getElementAt(i).toString());
            }
            resultado.set(mensajes);
        });
        return resultado.get();
    }

    private static boolean hayHilosDelVisor() {
        return Thread.getAllStackTraces().keySet().stream()
                .map(Thread::getName)
                .anyMatch(nombre -> nombre.startsWith("Nave ")
                        || nombre.startsWith("Generadora ")
                        || nombre.equals("Avisos del universo paralelo"));
    }

    private static void verificarOrden(List<String> mensajes, String... prefijos) {
        int siguiente = 0;
        for (String mensaje : mensajes) {
            if (mensaje.startsWith(prefijos[siguiente])) {
                siguiente++;
                if (siguiente == prefijos.length) {
                    return;
                }
            }
        }
        throw new AssertionError("Falta el evento en orden: " + prefijos[siguiente]);
    }

    private static JButton buscarBoton(Container contenedor, String texto) {
        for (Component hijo : contenedor.getComponents()) {
            if (hijo instanceof JButton boton && boton.getText().equals(texto)) {
                return boton;
            }
            if (hijo instanceof Container interior) {
                JButton encontrado = buscarBoton(interior, texto);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private static JList<?> buscarLista(Container contenedor) {
        for (Component hijo : contenedor.getComponents()) {
            if (hijo instanceof JList<?> lista) {
                return lista;
            }
            if (hijo instanceof Container interior) {
                JList<?> encontrada = buscarLista(interior);
                if (encontrada != null) {
                    return encontrada;
                }
            }
        }
        return null;
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
