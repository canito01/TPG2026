package modelo.motor;

import modelo.asistente.TransicionInvalidaException;

/**
 * Motor Warp (E1-02, UML «Context» MotorWarp). Guarda el estado actual y
 * DELEGA en el cada accion: no hay if/switch sobre estados. La tabla de
 * transiciones y el contrato de cada accion estan en EstadoMotor.
 *
 * Accion no permitida en el estado actual: el motor queda igual y se lanza
 * TransicionInvalidaException (no comprobada, de dominio) para que el asistente
 * la capture y la registre en la bitacora.
 *
 * El motor es un subsistema de la Nave: no conoce al asistente ni a las
 * misiones. Las misiones preguntan "esta disponible?" a traves del asistente,
 * que usa estaDisponible(). El modelo no usa consola.
 *
 * Invariante de clase: el estado actual nunca es null.
 */
public class MotorWarp {

    private EstadoMotor estado;

    /** Post: el motor queda en Disponible. */
    public MotorWarp() {
        this.estado = new Disponible();
        assert invariante() : "Fallo invariante";
    }

    /** Consulta el estado actual; no modifica nada. */
    public EstadoMotor getEstado() { return estado; }

    /** Post: true solo si el estado actual es Disponible (consulta; no modifica nada). */
    public boolean estaDisponible() { return estado.esDisponible(); }

    /** Pre: Disponible. Post: Preparando salto. Si no: TransicionInvalidaException, sin cambios. */
    public void prepararSalto() { estado.prepararSalto(this); }

    /** Pre: Preparando salto. Post: En warp. Si no: TransicionInvalidaException, sin cambios. */
    public void saltar() { estado.saltar(this); }

    /** Pre: En warp. Post: Enfriamiento. Si no: TransicionInvalidaException, sin cambios. */
    public void enfriar() { estado.enfriar(this); }

    /** Pre: Enfriamiento. Post: Disponible. Si no: TransicionInvalidaException, sin cambios. */
    public void terminarEnfriamiento() { estado.terminarEnfriamiento(this); }

    /**
     * Fin del salto (Aclaracion R3: "por ahora una nave en Salto warp vuelve a
     * Disponible cuando termina el salto"). NO es una transicion nueva: como no
     * se modela el tiempo, el enfriamiento pasa sin demora y se usan las dos
     * transiciones oficiales (enfriar + terminarEnfriamiento).
     * Pre: En warp. Post: Disponible. Si no: TransicionInvalidaException con la
     * accion "terminarSalto" y sin cambios (la primera transicion ya la rechaza).
     */
    public void terminarSalto() {
        try {
            enfriar();
        } catch (TransicionInvalidaException e) {
            throw new TransicionInvalidaException(e.getEstado(), "terminarSalto");
        }
        terminarEnfriamiento();
    }

    /** Solo para los estados del paquete. Pre: nuevo != null. Post: el estado actual es 'nuevo'. */
    void setEstado(EstadoMotor nuevo) {
        assert nuevo != null : "Estado nuevo nulo";
        this.estado = nuevo;
        assert invariante() : "Fallo invariante";
    }

    private boolean invariante() { return estado != null; }
}
