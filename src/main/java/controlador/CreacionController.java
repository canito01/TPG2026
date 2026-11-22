package controlador;

import java.util.function.Consumer;

import modelo.fabrica.NaveFactory;
import modelo.nave.Nave;
import modelo.nave.TipoNave;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.Origen;
import modelo.tripulacion.Tripulante;
import vista.CreacionPanel;

/**
 * Controlador de la Sección de Creación (MVC):
 *  - Recibe los eventos validados en formato desde {@link CreacionPanel}.
 *  - Instancia la nave solicitada exclusivamente a través de {@link NaveFactory}.
 *  - Crea y asigna los tripulantes aplicando las reglas de dominio (nave existente,
 *    antigüedad y consejos no negativos, y verificación de tripulación mínima:
 *    1 capitán + 4 tripulantes adicionales antes de habilitar una misión).
 */
public class CreacionController {

    private final CreacionPanel vista;
    private final NaveFactory fabrica;
    private final Consumer<Nave> alActualizarNave;
    private Nave naveActual;

    public CreacionController(CreacionPanel vista, NaveFactory fabrica) {
        this(vista, fabrica, null);
    }

    public CreacionController(CreacionPanel vista, NaveFactory fabrica, Consumer<Nave> alActualizarNave) {
        if (vista == null || fabrica == null) {
            throw new IllegalArgumentException("La vista y la fábrica de naves son obligatorias");
        }
        this.vista = vista;
        this.fabrica = fabrica;
        this.alActualizarNave = alActualizarNave;

        this.vista.setAccionCrearNave(this::crearNave);
        this.vista.setAccionAgregarTripulante(this::agregarTripulante);
    }

    public CreacionPanel getVista() {
        return vista;
    }

    public Nave getNaveActual() {
        return naveActual;
    }

    /**
     * Indica si la nave actual existe, está operativa y cumple la regla de
     * tripulación mínima (al menos 1 Capitán y 4 tripulantes adicionales).
     */
    public boolean puedeIniciarMision() {
        return naveActual != null && naveActual.tieneTripulacionValida();
    }

    public void crearNave(TipoNave tipo, String id) {
        try {
            this.naveActual = fabrica.crear(tipo, id);
            vista.mostrarNave(naveActual, naveActual.tieneTripulacionValida());
            vista.mostrarExito("Nave '" + naveActual.getId() + "' (" + naveActual.getTipo() + ") creada correctamente.");
            notificarActualizacion();
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError("No se pudo crear la nave: " + e.getMessage());
        }
    }

    public void agregarTripulante(String identidad, Cargo cargo, Origen origen, int antiguedad, int consejos) {
        if (naveActual == null) {
            vista.mostrarError("Regla de negocio: primero debe instanciar una nave antes de asignar tripulantes.");
            return;
        }
        if (naveActual.estaDestruida()) {
            vista.mostrarError("Regla de negocio: no se pueden asignar tripulantes a una nave destruida.");
            return;
        }
        try {
            int consejosEfectivos = (cargo == Cargo.CONSEJERO) ? consejos : 0;
            Tripulante nuevo = new Tripulante(identidad, cargo, origen, antiguedad, consejosEfectivos);
            naveActual.agregarTripulante(nuevo);
            boolean lista = naveActual.tieneTripulacionValida();
            vista.actualizarTripulacion(naveActual.getTripulacion(), lista);
            vista.limpiarFormularioTripulante();
            vista.mostrarExito("Tripulante '" + nuevo.getIdentidad() + "' (" + nuevo.getCargo() + ") asignado a " + naveActual.getId() + ".");
            notificarActualizacion();
        } catch (IllegalArgumentException e) {
            vista.mostrarError("Datos de tripulante inválidos: " + e.getMessage());
        }
    }

    private void notificarActualizacion() {
        if (alActualizarNave != null && naveActual != null) {
            alActualizarNave.accept(naveActual);
        }
    }
}
