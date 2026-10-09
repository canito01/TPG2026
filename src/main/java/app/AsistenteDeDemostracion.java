package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.asistente.AsistenteDeComando;
import modelo.asistente.TransicionInvalidaException;
import modelo.motor.MotorWarp;

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
    private final List<String> registro = new ArrayList<String>();
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

    boolean prepararSalto() {
        String antes = estadoDelMotor();
        try { motor.prepararSalto(); }
        catch (TransicionInvalidaException e) { return rechazo(e); }
        return cambio(antes, "prepararSalto");
    }

    boolean saltar() {
        String antes = estadoDelMotor();
        try { motor.saltar(); }
        catch (TransicionInvalidaException e) { return rechazo(e); }
        return cambio(antes, "saltar");
    }

    boolean enfriar() {
        String antes = estadoDelMotor();
        try { motor.enfriar(); }
        catch (TransicionInvalidaException e) { return rechazo(e); }
        return cambio(antes, "enfriar");
    }

    boolean terminarEnfriamiento() {
        String antes = estadoDelMotor();
        try { motor.terminarEnfriamiento(); }
        catch (TransicionInvalidaException e) { return rechazo(e); }
        return cambio(antes, "terminarEnfriamiento");
    }

    boolean terminarSalto() {
        String antes = estadoDelMotor();
        try { motor.terminarSalto(); }
        catch (TransicionInvalidaException e) { return rechazo(e); }
        return cambio(antes, "terminarSalto");
    }

    private boolean cambio(String antes, String accion) {
        registro.add("CAMBIO    " + antes + " -> " + estadoDelMotor() + " (" + accion + ")");
        return true;
    }

    private boolean rechazo(TransicionInvalidaException e) {
        rechazos++;
        registro.add("RECHAZADA " + e.getAccion() + " en '" + e.getEstado() + "'");
        return false;
    }
}
