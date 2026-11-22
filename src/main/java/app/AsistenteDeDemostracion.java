package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.asistente.AsistenteDeComando;
import modelo.asistente.Bitacora;
import modelo.asistente.Evento;
import modelo.asistente.Resultado;
import modelo.asistente.ResumenRecursos;
import modelo.motor.MotorWarp;
import modelo.motor.ResultadoDeTurno;
import modelo.navegacion.Coordenada;
import modelo.navegacion.PlanificadorDeRuta;
import modelo.tablero.Tablero;
import modelo.tablero.TableroStub;
import util.observer.Observador;

/**
 * Asistente SOLO para la demostracion (no es el de Agustin). Muestra como el
 * asistente real usa el motor: pide el turno (saltar / saltarWarp) y registra
 * en su bitacora lo que el motor informa en el ResultadoDeTurno. Opera una sola
 * nave. No imprime: guarda su registro en una lista que los escenarios muestran.
 */
class AsistenteDeDemostracion implements AsistenteDeComando {

    private final NaveSimulada nave;
    private final MotorWarp motor;
    private final List<String> registro = new ArrayList<String>();
    private final Bitacora bitacora = new Bitacora();
    private String estadoActual = "Disponible";

    AsistenteDeDemostracion(NaveSimulada nave) {
        this.nave = nave;
        this.motor = new MotorWarp(nave);
    }

    /**
     * Asistente de una nave que parte de (0,0) con una ruta recta hasta (19,0) sobre un
     * tablero vacio. Sirve para los escenarios que solo miran el motor.
     */
    static AsistenteDeDemostracion enLinea(String naveId, int combustible, int energia) {
        Tablero tablero = new TableroStub(0L);
        NaveSimulada nave = new NaveSimulada(naveId, combustible, energia, new Coordenada(0, 0), tablero);
        nave.setRuta(new PlanificadorDeRuta(tablero).planificar(new Coordenada(0, 0), new Coordenada(19, 0)));
        return new AsistenteDeDemostracion(nave);
    }

    @Override
    public String getNaveId() { return nave.getId(); }

    NaveSimulada getNave() { return nave; }

    List<String> getRegistro() { return Collections.unmodifiableList(registro); }

    ResultadoDeTurno turnoSaltar() { return registrar(motor.saltar()); }

    ResultadoDeTurno saltarWarp() { return registrar(motor.saltarWarp()); }

    private ResultadoDeTurno registrar(ResultadoDeTurno r) {
        estadoActual = r.getEstadoActual();
        String linea = r + " | pos=" + nave.getPosicion() + " comb=" + nave.getCombustible()
                + " ener=" + nave.getEnergia() + " desg=" + nave.getDesgaste();
        registro.add(linea);
        bitacora.registrar(linea);
        return r;
    }

    @Override
    public String consultarEstado() {
        return estadoActual;
    }

    @Override
    public ResumenRecursos consultarRecursos() {
        return new ResumenRecursos(nave.getCombustible(), nave.getEnergia(),
                nave.getDesgaste(), nave.getDesgaste() >= 80);
    }

    @Override
    public void agregarObservadorRecursos(Observador observador) { }

    @Override
    public void quitarObservadorRecursos(Observador observador) { }

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
        ResultadoDeTurno r = saltarWarp();
        return new Resultado(true, r.getDescripcion());
    }

    @Override
    public Resultado saltar() {
        ResultadoDeTurno r = turnoSaltar();
        return new Resultado(true, r.getDescripcion());
    }

    @Override
    public Resultado enfriar() {
        ResultadoDeTurno r = saltarWarp();
        return new Resultado(true, r.getDescripcion());
    }

    @Override
    public Resultado terminarEnfriamiento() {
        ResultadoDeTurno r = saltarWarp();
        return new Resultado(true, r.getDescripcion());
    }

    @Override
    public void registrarEvento(String descripcion) {
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
}
