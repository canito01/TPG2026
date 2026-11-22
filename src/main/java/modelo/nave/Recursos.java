package modelo.nave;

import util.observer.Sujeto;

/**
 * Recursos de una nave: combustible, energía, desgaste y gemas recolectadas (todos enteros).
 *
 * Invariantes de clase:
 *   0 <= combustible <= MAXIMO
 *   0 <= energia <= MAXIMO
 *   0 <= desgaste <= MAXIMO
 *   0 <= gemas
 *   #requiereMantenimiento() es verdadero si y solo si desgaste >= UMBRAL_MANTENIMIENTO
 *
 * Las invariantes se cumplen al terminar el constructor y antes y después de cada operación
 * pública. El estado es privado, por lo que solo estas operaciones pueden modificarlo.
 *
 * Esta clase no depende de consola ni de Swing.
 */
public class Recursos extends Sujeto {

    private static final String CAMBIO_RECURSOS = "Recursos actualizados";

    /** Capacidad máxima de combustible y energía, y valor máximo de desgaste. */
    public static final int MAXIMO = 100;

    /** Valor mínimo de cualquier recurso. */
    public static final int MINIMO = 0;

    /** Desgaste a partir del cual la nave requiere mantenimiento. */
    public static final int UMBRAL_MANTENIMIENTO = 80;

    private int combustible;
    private int energia;
    private int desgaste;
    private int gemas;

    /**
     * Crea recursos con valores iniciales y 0 gemas.
     *
     * Precondición: cada valor está en [MINIMO, MAXIMO].
     * Postcondición: los recursos quedan con los valores indicados y se cumplen las invariantes.
     *
     * @throws RecursoFueraDeRangoException si algún valor está fuera de [0, 100]
     */
    public Recursos(int combustible, int energia, int desgaste) {
        this(combustible, energia, desgaste, 0);
    }

    /**
     * Crea recursos con valores iniciales incluyendo gemas.
     *
     * @throws RecursoFueraDeRangoException si combustible, energía o desgaste están fuera de [0, 100]
     * @throws IllegalArgumentException     si gemas es negativo
     */
    public Recursos(int combustible, int energia, int desgaste, int gemas) {
        validarRango("combustible", combustible);
        validarRango("energía", energia);
        validarRango("desgaste", desgaste);
        if (gemas < 0) {
            throw new IllegalArgumentException("Las gemas no pueden ser negativas (recibido: " + gemas + ")");
        }
        this.combustible = combustible;
        this.energia = energia;
        this.desgaste = desgaste;
        this.gemas = gemas;
        verificarInvariantes();
    }

    // ------------------------------------------------------------------ consultas

    public synchronized int getCombustible() {
        return combustible;
    }

    public synchronized int getEnergia() {
        return energia;
    }

    public synchronized int getDesgaste() {
        return desgaste;
    }

    public synchronized int getGemas() {
        return gemas;
    }

    /** @return true si el desgaste es UMBRAL_MANTENIMIENTO o superior */
    public synchronized boolean requiereMantenimiento() {
        return desgaste >= UMBRAL_MANTENIMIENTO;
    }

    /**
     * Indica si los recursos llegaron a una condición de destrucción de la nave
     * según el enunciado de la Parte 2: desgaste en 100, energía en 0 o combustible en 0.
     */
    public synchronized boolean estaDestruida() {
        return desgaste >= MAXIMO || energia <= MINIMO || combustible <= MINIMO;
    }

    /**
     * Copia independiente del estado actual.
     */
    public synchronized Recursos copia() {
        return new Recursos(combustible, energia, desgaste, gemas);
    }

    /** @return verdadero si #cargarCombustible(int) con esa cantidad sería aceptada */
    public synchronized boolean puedeCargarCombustible(int cantidad) {
        return cantidad > 0 && entraEnRango((long) combustible + cantidad);
    }

    /** @return true si #cargarEnergia(int) con esa cantidad sería aceptada */
    public synchronized boolean puedeCargarEnergia(int cantidad) {
        return cantidad > 0 && entraEnRango((long) energia + cantidad);
    }

    /** @return true si #consumir(int, int) con esas cantidades sería aceptada */
    public synchronized boolean puedeConsumir(int combustibleAConsumir, int energiaAConsumir) {
        return combustibleAConsumir >= 0 && energiaAConsumir >= 0
                && entraEnRango((long) combustible - combustibleAConsumir)
                && entraEnRango((long) energia - energiaAConsumir);
    }

    /** @return true si #desgastar(int) con esa cantidad sería aceptada */
    public synchronized boolean puedeDesgastar(int cantidad) {
        return cantidad > 0 && entraEnRango((long) desgaste + cantidad);
    }

    /** @return true si la nave dispone de los recursos solicitados para transferir en M-01 */
    public synchronized boolean puedeEntregar(int combustibleAEntregar, int energiaAEntregar) {
        return puedeConsumir(combustibleAEntregar, energiaAEntregar);
    }

    // ------------------------------------------------------------------ operaciones E1 (estrictas)

    /**
     * Suma combustible de forma estricta (rechaza si supera MAXIMO).
     *
     * @throws IllegalArgumentException     si cantidad <= 0
     * @throws RecursoFueraDeRangoException si se superaría la capacidad máxima
     */
    public void cargarCombustible(int cantidad) {
        exigirPositiva("cantidad de combustible", cantidad);
        synchronized (this) {
            combustible = resultado("combustible", combustible, cantidad);
            verificarInvariantes();
        }
        notificarObservadores(CAMBIO_RECURSOS);
    }

    /**
     * Suma energía de forma estricta (rechaza si supera MAXIMO).
     *
     * @throws IllegalArgumentException     si cantidad <= 0
     * @throws RecursoFueraDeRangoException si se superaría la capacidad máxima
     */
    public void cargarEnergia(int cantidad) {
        exigirPositiva("cantidad de energía", cantidad);
        synchronized (this) {
            energia = resultado("energía", energia, cantidad);
            verificarInvariantes();
        }
        notificarObservadores(CAMBIO_RECURSOS);
    }

    /**
     * Consume combustible y energía de forma atómica: o se descuentan ambos o no se
     * descuenta ninguno.
     *
     * @throws IllegalArgumentException     si alguna cantidad es negativa
     * @throws RecursoFueraDeRangoException si alguno de los recursos no alcanza
     */
    public void consumir(int combustibleAConsumir, int energiaAConsumir) {
        consumir(combustibleAConsumir, energiaAConsumir, 0);
    }

    /**
     * Descuenta combustible y energía y suma desgaste en una sola operación atómica.
     */
    public void consumir(int combustibleAConsumir, int energiaAConsumir, int desgasteAAgregar) {
        if (combustibleAConsumir < 0 || energiaAConsumir < 0) {
            throw new IllegalArgumentException("Las cantidades a consumir no pueden ser negativas");
        }
        if (desgasteAAgregar < 0) {
            throw new IllegalArgumentException("La cantidad de desgaste no puede ser negativa");
        }
        if (combustibleAConsumir == 0 && energiaAConsumir == 0 && desgasteAAgregar == 0) {
            return;
        }
        synchronized (this) {
            int nuevoCombustible = resultado("combustible", combustible, -(long) combustibleAConsumir);
            int nuevaEnergia = resultado("energía", energia, -(long) energiaAConsumir);
            int nuevoDesgaste = resultado("desgaste", desgaste, desgasteAAgregar);
            combustible = nuevoCombustible;
            energia = nuevaEnergia;
            desgaste = nuevoDesgaste;
            verificarInvariantes();
        }
        notificarObservadores(CAMBIO_RECURSOS);
    }

    /**
     * Suma desgaste de forma estricta.
     *
     * @throws IllegalArgumentException     si cantidad <= 0
     * @throws RecursoFueraDeRangoException si el desgaste superaría el máximo
     */
    public void desgastar(int cantidad) {
        exigirPositiva("cantidad de desgaste", cantidad);
        synchronized (this) {
            desgaste = resultado("desgaste", desgaste, cantidad);
            verificarInvariantes();
        }
        notificarObservadores(CAMBIO_RECURSOS);
    }

    /**
     * Realiza el mantenimiento de la nave llevando el desgaste a 0.
     */
    public void realizarMantenimiento() {
        synchronized (this) {
            if (desgaste == MINIMO) {
                return;
            }
            desgaste = MINIMO;
            verificarInvariantes();
        }
        notificarObservadores(CAMBIO_RECURSOS);
    }

    // ------------------------------------------------------------------ operaciones E2

    /**
     * Recarga combustible y energía saturando en el tope máximo (100), sin fallar
     * si el excedente supera la capacidad (usado por Estaciones de Carga).
     *
     * Precondición: {@code combustibleExtra >= 0} y {@code energiaExtra >= 0}.
     *
     * @throws IllegalArgumentException si alguna cantidad es negativa
     */
    public void recargarHastaElTope(int combustibleExtra, int energiaExtra) {
        if (combustibleExtra < 0 || energiaExtra < 0) {
            throw new IllegalArgumentException("Las cantidades a recargar no pueden ser negativas");
        }
        boolean cambio;
        synchronized (this) {
            int nuevoCombustible = (int) Math.min(MAXIMO, (long) combustible + combustibleExtra);
            int nuevaEnergia = (int) Math.min(MAXIMO, (long) energia + energiaExtra);
            cambio = (nuevoCombustible != combustible) || (nuevaEnergia != energia);
            combustible = nuevoCombustible;
            energia = nuevaEnergia;
            verificarInvariantes();
        }
        if (cambio) {
            notificarObservadores(CAMBIO_RECURSOS);
        }
    }

    /**
     * Recarga total de los tanques al 100% y mantenimiento general con desgaste a 0
     * (recompensa operativa al completar M-03).
     */
    public void recargaTotal() {
        boolean cambio;
        synchronized (this) {
            cambio = (combustible != MAXIMO) || (energia != MAXIMO) || (desgaste != MINIMO);
            combustible = MAXIMO;
            energia = MAXIMO;
            desgaste = MINIMO;
            verificarInvariantes();
        }
        if (cambio) {
            notificarObservadores(CAMBIO_RECURSOS);
        }
    }

    /**
     * Transfiere recursos de auxilio a una nave aliada (M-01: entre 0 y 50 puntos).
     * Es atómica: si no alcanzan los recursos, lanza {@link RecursoFueraDeRangoException}
     * sin modificar la nave.
     */
    public void entregarRecursos(int combustibleAEntregar, int energiaAEntregar) {
        if (combustibleAEntregar < 0 || energiaAEntregar < 0) {
            throw new IllegalArgumentException("Los recursos a entregar no pueden ser negativos");
        }
        consumir(combustibleAEntregar, energiaAEntregar, 0);
    }

    /**
     * Registra gemas recolectadas al finalizar M-02.
     *
     * Precondición: {@code cantidad > 0}.
     */
    public void recolectarGemas(int cantidad) {
        exigirPositiva("cantidad de gemas", cantidad);
        synchronized (this) {
            gemas += cantidad;
            verificarInvariantes();
        }
        notificarObservadores(CAMBIO_RECURSOS);
    }

    /**
     * Descuenta energía por colisión con un obstáculo (asteroide), saturando en 0
     * para que la nave quede con energía 0 (inoperativa/destruida) si el impacto
     * iguala o supera su reserva actual.
     *
     * Precondición: {@code cantidad > 0}.
     */
    public void perderEnergiaHastaCero(int cantidad) {
        exigirPositiva("cantidad de energía a descontar", cantidad);
        boolean cambio;
        synchronized (this) {
            int nuevaEnergia = Math.max(MINIMO, energia - cantidad);
            cambio = (nuevaEnergia != energia);
            energia = nuevaEnergia;
            verificarInvariantes();
        }
        if (cambio) {
            notificarObservadores(CAMBIO_RECURSOS);
        }
    }

    @Override
    public synchronized String toString() {
        return "Recursos[combustible=" + combustible + ", energia=" + energia
                + ", desgaste=" + desgaste + ", gemas=" + gemas + "]";
    }

    // ------------------------------------------------------------------ internos

    private static boolean entraEnRango(long valor) {
        return valor >= MINIMO && valor <= MAXIMO;
    }

    private static void validarRango(String nombre, int valor) {
        if (!entraEnRango(valor)) {
            throw new RecursoFueraDeRangoException(
                    "El " + nombre + " debe estar entre " + MINIMO + " y " + MAXIMO + " (recibido: " + valor + ")");
        }
    }

    private static void exigirPositiva(String nombre, int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("La " + nombre + " debe ser positiva (recibido: " + valor + ")");
        }
    }

    private static int resultado(String nombre, int actual, long delta) {
        long nuevo = actual + delta;
        if (!entraEnRango(nuevo)) {
            throw new RecursoFueraDeRangoException(
                    "La operación dejaría el " + nombre + " fuera de rango [" + MINIMO + ", " + MAXIMO
                            + "] (actual: " + actual + ", cambio: " + delta + ")");
        }
        return (int) nuevo;
    }

    private void verificarInvariantes() {
        if (!entraEnRango(combustible) || !entraEnRango(energia) || !entraEnRango(desgaste) || gemas < 0) {
            throw new IllegalStateException("Invariante de Recursos violada: " + this);
        }
    }
}
