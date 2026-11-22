package controlador;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import modelo.asistente.AsistenteDeComando;
import modelo.asistente.PosicionNave;
import modelo.misiones.HistorialDeInformes;
import modelo.misiones.InformeMision;
import modelo.misiones.Mision;
import modelo.misiones.Mision1;
import modelo.misiones.Mision2;
import modelo.misiones.Mision3;
import modelo.nave.Nave;
import vista.MisionesPanel;
import vista.MisionesPanel.TipoMisionUI;

/**
 * Controlador MVC para la gestion y ejecucion de misiones desde la GUI.
 *
 * Reglas que aplica:
 * - Solo inicia una mision si existe una nave operativa con tripulacion valida (1 Capitan + 4 tripulantes).
 * - Permite una sola mision activa a la vez.
 * - Ejecuta cada mision en su propio hilo (nunca en el Event Dispatch Thread de Swing).
 * - Registra todos los informes en {@link HistorialDeInformes} (solo en memoria) y actualiza
 *   la vista mediante {@link SwingUtilities#invokeLater(Runnable)}.
 */
public class MisionController {

    private static final int PAUSA_POR_DEFECTO_MS = 300;

    private final MisionesPanel vista;
    private final HistorialDeInformes historial;
    private final Supplier<PosicionNave> generadorObjetivo;
    private final Runnable alFinalizarMision;
    private final int pausaTurnoMs;

    private final AtomicBoolean enEjecucion = new AtomicBoolean(false);
    private volatile Nave naveActiva;
    private volatile AsistenteDeComando asistenteActivo;
    private volatile Thread hiloMisionActual;

    public MisionController(
            MisionesPanel vista,
            HistorialDeInformes historial,
            Supplier<PosicionNave> generadorObjetivo,
            Runnable alFinalizarMision) {
        this(vista, historial, generadorObjetivo, alFinalizarMision, PAUSA_POR_DEFECTO_MS);
    }

    public MisionController(
            MisionesPanel vista,
            HistorialDeInformes historial,
            Supplier<PosicionNave> generadorObjetivo,
            Runnable alFinalizarMision,
            int pausaTurnoMs) {
        this.vista = Objects.requireNonNull(vista, "La vista de misiones es obligatoria.");
        this.historial = Objects.requireNonNull(historial, "El historial de informes es obligatorio.");
        this.generadorObjetivo = Objects.requireNonNull(generadorObjetivo, "El generador de objetivos es obligatorio.");
        this.alFinalizarMision = alFinalizarMision;
        if (pausaTurnoMs < 0) {
            throw new IllegalArgumentException("La pausa entre turnos no puede ser negativa.");
        }
        this.pausaTurnoMs = pausaTurnoMs;

        this.vista.agregarListenerIniciar(e -> iniciarMisionDesdeVista());
        this.vista.agregarListenerCancelar(e -> cancelarMisionActual());
        refrescarEstadoControles();
    }

    public HistorialDeInformes getHistorial() {
        return historial;
    }

    public boolean hayMisionEnCurso() {
        return enEjecucion.get();
    }

    /**
     * Actualiza la referencia a la nave y su asistente activo (al crear nave o asignar tripulacion).
     */
    public void actualizarNaveActiva(Nave nave, AsistenteDeComando asistente) {
        if (nave != this.naveActiva && enEjecucion.get()) {
            cancelarMisionActual();
        }
        this.naveActiva = nave;
        this.asistenteActivo = asistente;
        refrescarEstadoControles();
    }

    /**
     * Valida precondiciones de dominio e inicia la mision seleccionada en un hilo propio.
     * Devuelve {@code true} si la mision se lanzo, o {@code false} si fue rechazada antes de arrancar.
     */
    public boolean iniciarMisionDesdeVista() {
        Nave nave = this.naveActiva;
        AsistenteDeComando asistente = this.asistenteActivo;

        if (nave == null || asistente == null) {
            enEDT(() -> vista.mostrarEstadoError("Primero creá una nave en la pestaña Creación."));
            return false;
        }
        if (nave.estaDestruida()) {
            enEDT(() -> {
                vista.mostrarEstadoError("La nave está destruida y no puede iniciar nuevas misiones.");
                vista.actualizarControles(false, false);
            });
            return false;
        }
        if (!nave.tieneTripulacionValida()) {
            enEDT(() -> {
                vista.mostrarEstadoError("Tripulación incompleta: se requiere al menos 1 Capitán y 4 tripulantes vivos.");
                vista.actualizarControles(false, false);
            });
            return false;
        }
        if (!enEjecucion.compareAndSet(false, true)) {
            enEDT(() -> vista.mostrarEstadoError("Ya hay una misión en curso para esta nave."));
            return false;
        }

        Mision mision;
        try {
            mision = construirMisionSeleccionada();
        } catch (RuntimeException ex) {
            enEjecucion.set(false);
            enEDT(() -> {
                vista.mostrarEstadoError("No se pudo preparar la misión: " + ex.getMessage());
                refrescarEstadoControles();
            });
            return false;
        }

        enEDT(() -> {
            vista.actualizarControles(false, true);
            vista.mostrarEstadoInfo("Ejecutando " + mision.getNombre() + " en curso...");
        });

        Thread hilo = new Thread(() -> ejecutarEnSegundoPlano(mision, asistente),
                "Mision-" + mision.getNombre() + "-" + nave.getId());
        hilo.setDaemon(true);
        this.hiloMisionActual = hilo;
        hilo.start();
        return true;
    }

    private Mision construirMisionSeleccionada() {
        TipoMisionUI tipo = vista.getTipoMisionSeleccionada();
        int pausa = this.pausaTurnoMs;

        if (tipo == TipoMisionUI.M03) {
            return new Mision3() {
                @Override
                protected int pausaPorTurnoMs() {
                    return pausa;
                }
            };
        }

        PosicionNave destino = vista.usaDestinoAutomatico()
                ? Objects.requireNonNull(generadorObjetivo.get(), "No se pudo obtener un objetivo del tablero.")
                : new PosicionNave(vista.getDestinoX(), vista.getDestinoY());

        if (tipo == TipoMisionUI.M01) {
            int energia = vista.getAuxilioEnergia();
            int combustible = vista.getAuxilioCombustible();
            return new Mision1(destino, energia, combustible) {
                @Override
                protected int pausaPorTurnoMs() {
                    return pausa;
                }
            };
        }

        return new Mision2(destino) {
            @Override
            protected int pausaPorTurnoMs() {
                return pausa;
            }
        };
    }

    private void ejecutarEnSegundoPlano(Mision mision, AsistenteDeComando asistente) {
        try {
            InformeMision informe = mision.ejecutarCiclo(asistente);
            historial.agregar(informe);

            enEDT(() -> {
                vista.mostrarHistorial(historial.obtenerTodos());
                if (informe.isExitosa()) {
                    vista.mostrarEstadoExito(informe.getNombre() + " completada: " + informe.getResultado());
                } else {
                    vista.mostrarEstadoError(informe.getNombre() + " fallida: " + informe.getObservaciones());
                }
            });
        } catch (RuntimeException ex) {
            enEDT(() -> vista.mostrarEstadoError("Error al ejecutar la misión: " + ex.getMessage()));
        } finally {
            hiloMisionActual = null;
            enEjecucion.set(false);
            if (alFinalizarMision != null) {
                alFinalizarMision.run();
            }
            refrescarEstadoControles();
        }
    }

    /** Interrumpe la mision activa si hubiera una en ejecucion. */
    public void cancelarMisionActual() {
        Thread hilo = this.hiloMisionActual;
        if (hilo != null && hilo.isAlive()) {
            hilo.interrupt();
        }
    }

    /** Espera a que termine el hilo activo (util para pruebas sincronizadas). */
    public void esperarFinDeMision(long timeoutMs) throws InterruptedException {
        Thread hilo = this.hiloMisionActual;
        if (hilo != null) {
            hilo.join(timeoutMs);
        }
    }

    /** Detiene ordenadamente cualquier mision activa al cerrar la aplicacion. */
    public void cerrar() {
        cancelarMisionActual();
    }

    private void refrescarEstadoControles() {
        Nave nave = this.naveActiva;
        boolean corriendo = enEjecucion.get();
        boolean lista = nave != null && !nave.estaDestruida() && nave.tieneTripulacionValida();

        enEDT(() -> {
            vista.actualizarControles(lista, corriendo);
            if (!corriendo) {
                if (nave == null) {
                    vista.mostrarEstadoInfo("Creá una nave con al menos 1 Capitán y 4 tripulantes para iniciar misiones.");
                } else if (nave.estaDestruida()) {
                    vista.mostrarEstadoError("Nave " + nave.getId() + " destruida. Creá una nueva nave para continuar.");
                } else if (!nave.tieneTripulacionValida()) {
                    vista.mostrarEstadoInfo("Nave " + nave.getId() + " sin tripulación completa (falta Capitán o llegar a 5 tripulantes).");
                } else if (historial.cantidad() == 0) {
                    vista.mostrarEstadoExito("Nave " + nave.getId() + " lista para despegar.");
                }
            }
        });
    }

    private static void enEDT(Runnable tarea) {
        if (SwingUtilities.isEventDispatchThread()) {
            tarea.run();
        } else {
            SwingUtilities.invokeLater(tarea);
        }
    }
}
