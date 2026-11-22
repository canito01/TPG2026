package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.motor.MotorWarp;
import modelo.motor.ParametrosDelMotor;
import modelo.motor.ResultadoDeTurno;
import modelo.navegacion.Coordenada;
import modelo.navegacion.RadarCortoAlcance;
import modelo.navegacion.RadarLargoAlcance;
import modelo.navegacion.Ruta;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.Tablero;

/**
 * Viaje de DEMOSTRACION: junta motor, radares y tablero con el mismo bucle por
 * turnos que describe el Enunciado (turno de la nave: escanear y saltar o
 * saltarWarp; turno del tablero: mueve los elementos). NO es la mision de
 * Tomas: no cobra el despegue, no interactua con los elementos ni arma informes.
 * Sirve para ver el conjunto andando y para probar la integracion.
 *
 * Se ejecuta en su propio hilo (Runnable) con ritmo configurable: pausa 0 en
 * las pruebas, algunos cientos de milisegundos para ver el avance en la vista.
 */
public class SimulacionDeViaje implements Runnable {

    private static final int MAX_TURNOS = 500;

    private final Tablero tablero;
    private final NaveSimulada nave;
    private final Coordenada destino;
    private final long pausaMs;
    private final MotorWarp motor;
    private final RadarLargoAlcance radarLargo;
    private final RadarCortoAlcance radarCorto;
    private final List<String> registro = Collections.synchronizedList(new ArrayList<String>());
    private volatile boolean llego;
    private volatile int turnos;
    private volatile int saltosWarp;

    /** Pre: todo no nulo, destino valido y pausaMs >= 0. */
    public SimulacionDeViaje(Tablero tablero, NaveSimulada nave, Coordenada destino, long pausaMs) {
        if (tablero == null || nave == null || destino == null || pausaMs < 0) {
            throw new IllegalArgumentException("Parametros invalidos para el viaje");
        }
        this.tablero = tablero;
        this.nave = nave;
        this.destino = destino;
        this.pausaMs = pausaMs;
        this.motor = new MotorWarp(nave);
        this.radarLargo = new RadarLargoAlcance(tablero);
        this.radarCorto = new RadarCortoAlcance(tablero);
    }

    @Override
    public void run() {
        try {
            // Preparar: ruta con el radar de largo alcance
            Ruta ruta = radarLargo.calcularRuta(nave.getPosicion(), destino);
            nave.setRuta(ruta);
            tablero.marcarDestino(destino);
            registro.add("Ruta calculada: " + ruta.pasos() + " casillas de " + ruta.getOrigen() + " a " + ruta.getDestino());

            // Ejecutar: bucle por turnos
            while (!nave.haLlegado() && turnos < MAX_TURNOS) {
                turnos++;
                ElementoDePrueba delante = radarCorto.escanearSiguiente(ruta, nave.getPosicion());
                ResultadoDeTurno r = (delante == null) ? motor.saltar() : motor.saltarWarp();
                if (r.getCasillasAvanzadas() == ParametrosDelMotor.CASILLAS_WARP) {
                    saltosWarp++;
                }
                registro.add("T" + turnos + " " + (delante == null ? "despejado" : "obstaculo " + delante.getNombre())
                        + " -> " + (delante == null ? "saltar" : "saltarWarp") + " " + r + " | pos=" + nave.getPosicion());
                tablero.moverElementos();   // turno del tablero
                if (pausaMs > 0) {
                    Thread.sleep(pausaMs);
                }
            }
            llego = nave.haLlegado();
            registro.add(llego ? "Llego a " + destino + " en " + turnos + " turnos"
                    : "No llego: se corto a los " + turnos + " turnos");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            registro.add("Viaje interrumpido");
        }
    }

    public boolean haLlegado() { return llego; }

    public int getTurnos() { return turnos; }

    public int getSaltosWarp() { return saltosWarp; }

    /** Copia del registro hasta el momento. */
    public List<String> getRegistro() {
        synchronized (registro) {
            return new ArrayList<String>(registro);
        }
    }
}
