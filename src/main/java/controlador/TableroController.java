package controlador;

import modelo.tablero.Tablero;
import vista.TableroPanel;

/**
 * Controlador minimo de la vista del tablero (MVC). La vista captura la accion
 * del usuario (un ActionListener) y la delega aca; el controlador llama al
 * modelo, y la vista se entera del cambio por Observer (no por el controlador).
 * Tambien arma la vista y la desconecta al cerrar.
 */
public class TableroController {

    private final Tablero modelo;
    private final TableroPanel vista;

    /** Pre: modelo != null. Post: crea la vista del tablero suscripta al modelo. */
    public TableroController(Tablero modelo) {
        if (modelo == null) {
            throw new IllegalArgumentException("El tablero no puede ser null");
        }
        this.modelo = modelo;
        this.vista = new TableroPanel(modelo);
    }

    public TableroPanel getVista() { return vista; }

    /** Accion del usuario "turno del tablero": el modelo mueve los elementos. */
    public void moverTableroUnaVez() {
        modelo.moverElementos();
    }

    /** Al cerrar la ventana: la vista deja de observar el tablero. */
    public void cerrar() {
        vista.desconectar();
    }
}
