package app;

import java.io.PrintStream;

/**
 * Un escenario reproducible de la Ficha de Inicio (A, B, C, D) o de la
 * Aclaracion (universo). Escribe en el PrintStream que recibe: asi el Main lo
 * manda a la consola y las pruebas lo capturan, y el modelo nunca imprime.
 *
 * Si una verificacion del escenario no se cumple, ejecutar() lanza
 * IllegalStateException: el escenario no puede "pasar" sin demostrar su resultado.
 */
interface Escenario {

    String getNombre();

    void ejecutar(PrintStream out);
}
