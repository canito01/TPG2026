package util.observer;

/**
 * Recibe avisos del modelo, sin depender de una pantalla concreta.
 * La firma coincide con el Observer de la version 2 del modulo de la catedra:
 * https://github.com/MicaelaRasso/TrabajoIntegrador-DestinoYTablero
 * Son tipos de paquetes distintos; el tablero se conectara al integrar el modulo.
 */
public interface Observador {

    /**
     * Recibe un mensaje despues del cambio que lo origino.
     * Se ejecuta en el hilo que notifica; una vista Swing debe actualizarse
     * mediante SwingUtilities.invokeLater. El observador no debe lanzar excepciones.
     */
    void actualizar(String mensaje);
}
