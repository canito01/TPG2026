package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.asistente.AsistenteDeComando;
import modelo.asistente.Bitacora;
import modelo.asistente.Evento;
import modelo.asistente.Resultado;
import modelo.asistente.ResumenRecursos;
import modelo.asistente.TransicionInvalidaException;
import modelo.motor.MotorWarp;
import modelo.nave.Recursos;
import util.observer.Observador;

/**
 * Asistente SOLO para la demostracion (no es el de Agustin). Muestra como el
 * asistente real usa el motor, tal como indica el UML:
 *  - opera una sola nave (aca, solo su motor);
 *  - si el motor acepta la accion, registra el cambio (estado antes -> despues);
 *  - si el motor lanza TransicionInvalidaException (ExcepcionDominio), la captura
 *    y la registra como rechazo.
 * No imprime: guarda su registro en una lista que los escenarios muestran.
 * Cuando exista el asistente real (AsistenteBase), los escenarios usan ese.
 */
class AsistenteDeDemostracion implements AsistenteDeComando {

    private final String naveId;
    private final MotorWarp motor = new MotorWarp();
    // En la demostracion el estado de recursos es fijo.
    private final Recursos recursos = new Recursos(0, 0, 0);
    private final List<String> registro = new ArrayList<String>();
    private final Bitacora bitacora = new Bitacora();
    private int rechazos = 0;

    AsistenteDeDemostracion(String naveId) {
        this.naveId = naveId;
    }

    @Override
    public String getNaveId() { return naveId; }

    String estadoDelMotor() { return motor.getEstado().nombre(); }

    boolean estaDisponible() { return motor.estaDisponible(); }

    int cantidadRechazos() { return rechazos; }

    List<String> getRegistro() { return Collections.unmodifiableList(registro); }

    @Override
    public String consultarEstado() {
        return estadoDelMotor();
    }

    @Override
    public ResumenRecursos consultarRecursos() {
        return new ResumenRecursos(recursos.getCombustible(), recursos.getEnergia(),
                recursos.getDesgaste(), recursos.requiereMantenimiento());
    }

    @Override
    public void agregarObservadorRecursos(Observador observador) {
        recursos.agregarObservador(observador);
    }

    @Override
    public void quitarObservadorRecursos(Observador observador) {
        recursos.quitarObservador(observador);
    }

    @Override
    public Resultado consumir(int combustible, int energia, int desgaste) {
        return new Resultado(true, "Consumo aplicado");
    }

    @Override
    public Resultado cargarCombustible(int cantidad) {
        return new Resultado(true, "Combustible cargado");
    }

    @Override
    public Resultado cargarEnergia(int cantidad) {
        return new Resultado(true, "Energia cargada");
    }

    @Override
    public Resultado realizarMantenimiento() {
        return new Resultado(true, "Mantenimiento realizado");
    }

    @Override
    public Resultado prepararSalto() {
        String antes = estadoDelMotor();
        try {
            motor.prepararSalto();
            cambio(antes, "prepararSalto");
            return new Resultado(true, "Salto preparado");
        } catch (TransicionInvalidaException e) {
            rechazo(e);
            return new Resultado(false, e.getMessage());
        }
    }

    @Override
    public Resultado saltar() {
        String antes = estadoDelMotor();
        try {
            motor.saltar();
            cambio(antes, "saltar");
            return new Resultado(true, "Salto ejecutado");
        } catch (TransicionInvalidaException e) {
            rechazo(e);
            return new Resultado(false, e.getMessage());
        }
    }

    @Override
    public Resultado enfriar() {
        String antes = estadoDelMotor();
        try {
            motor.enfriar();
            cambio(antes, "enfriar");
            return new Resultado(true, "Enfriamiento iniciado");
        } catch (TransicionInvalidaException e) {
            rechazo(e);
            return new Resultado(false, e.getMessage());
        }
    }

    @Override
    public Resultado terminarEnfriamiento() {
        String antes = estadoDelMotor();
        try {
            motor.terminarEnfriamiento();
            cambio(antes, "terminarEnfriamiento");
            return new Resultado(true, "Enfriamiento terminado");
        } catch (TransicionInvalidaException e) {
            rechazo(e);
            return new Resultado(false, e.getMessage());
        }
    }

    boolean terminarSalto() {
        String antes = estadoDelMotor();
        try {
            motor.terminarSalto();
            cambio(antes, "terminarSalto");
            return true;
        } catch (TransicionInvalidaException e) {
            rechazo(e);
            return false;
        }
    }

    @Override
    public void registrarEvento(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("El evento debe tener una descripcion.");
        }
        registro.add(descripcion);
        bitacora.registrar(descripcion);
    }

    @Override
    public List<Evento> consultarBitacora() {
        return bitacora.getEventos();
    }

    @Override
    public void agregarObservadorBitacora(Observador observador) {
        bitacora.agregarObservador(observador);
    }

    @Override
    public void quitarObservadorBitacora(Observador observador) {
        bitacora.quitarObservador(observador);
    }

    private boolean cambio(String antes, String accion) {
        registrarEvento("CAMBIO    " + antes + " -> " + estadoDelMotor() + " (" + accion + ")");
        return true;
    }

    private boolean rechazo(TransicionInvalidaException e) {
        rechazos++;
        registrarEvento("RECHAZADA " + e.getAccion() + " en '" + e.getEstado() + "'");
        return false;
    }
}
