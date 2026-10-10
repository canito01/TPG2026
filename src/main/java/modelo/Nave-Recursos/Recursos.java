package modelo.nave;

/**
 * Recursos de una nave: combustible, energía y desgaste (todos enteros).
 *
 * <h2>Invariantes de clase</h2>
 * <ul>
 *   <li>{@code 0 <= combustible <= MAXIMO}</li>
 *   <li>{@code 0 <= energia <= MAXIMO}</li>
 *   <li>{@code 0 <= desgaste <= MAXIMO}</li>
 *   <li>{@link #requiereMantenimiento()} es verdadero si y solo si
 *       {@code desgaste >= UMBRAL_MANTENIMIENTO}</li>
 * </ul>
 * Las invariantes se cumplen al terminar el constructor y antes y después de cada operación
 * pública. El estado es privado, por lo que solo estas operaciones pueden modificarlo.
 *
 * <h2>Garantía común de las operaciones que modifican</h2>
 * <p>Si una operación no puede completarse sin violar un límite, lanza
 * {@link RecursoFueraDeRangoException} y el objeto queda <b>exactamente como estaba</b>
 * (sin cambios parciales). Si el argumento no cumple la precondición (valor negativo o
 * nulo según el método), lanza {@link IllegalArgumentException} y tampoco modifica nada.</p>
 *
 * <p>Las consultas {@code puede...} permiten decidir de antemano, sin lanzar excepciones,
 * si una operación sería aceptada.</p>
 *
 * <p>Esta clase no depende de consola ni de Swing.</p>
 */
public class Recursos {

    /** Capacidad máxima de combustible y energía, y valor máximo de desgaste. */
    public static final int MAXIMO = 100;

    /** Valor mínimo de cualquier recurso. */
    public static final int MINIMO = 0;

    /** Desgaste a partir del cual la nave requiere mantenimiento. */
    public static final int UMBRAL_MANTENIMIENTO = 80;

    private int combustible;
    private int energia;
    private int desgaste;

    /**
     * Crea recursos con valores iniciales.
     *
     * <p><b>Precondición:</b> cada valor está en {@code [MINIMO, MAXIMO]}.<br>
     * <b>Postcondición:</b> los recursos quedan con los valores indicados y se cumplen las
     * invariantes.</p>
     *
     * @throws RecursoFueraDeRangoException si algún valor está fuera de {@code [0, 100]}
     */
    public Recursos(int combustible, int energia, int desgaste) {
        validarRango("combustible", combustible);
        validarRango("energía", energia);
        validarRango("desgaste", desgaste);
        this.combustible = combustible;
        this.energia = energia;
        this.desgaste = desgaste;
        verificarInvariantes();
    }

    // ------------------------------------------------------------------ consultas

    public int getCombustible() {
        return combustible;
    }

    public int getEnergia() {
        return energia;
    }

    public int getDesgaste() {
        return desgaste;
    }

    /** @return {@code true} si el desgaste es {@value #UMBRAL_MANTENIMIENTO} o superior */
    public boolean requiereMantenimiento() {
        return desgaste >= UMBRAL_MANTENIMIENTO;
    }

    /**
     * Copia independiente del estado actual. Útil para tomar una instantánea antes de una
     * operación (por ejemplo, para calcular lo consumido por una misión).
     */
    public Recursos copia() {
        return new Recursos(combustible, energia, desgaste);
    }

    /** @return {@code true} si {@link #cargarCombustible(int)} con esa cantidad sería aceptada */
    public boolean puedeCargarCombustible(int cantidad) {
        return cantidad > 0 && entraEnRango((long) combustible + cantidad);
    }

    /** @return {@code true} si {@link #cargarEnergia(int)} con esa cantidad sería aceptada */
    public boolean puedeCargarEnergia(int cantidad) {
        return cantidad > 0 && entraEnRango((long) energia + cantidad);
    }

    /** @return {@code true} si {@link #consumir(int, int)} con esas cantidades sería aceptada */
    public boolean puedeConsumir(int combustibleAConsumir, int energiaAConsumir) {
        return combustibleAConsumir >= 0 && energiaAConsumir >= 0
                && entraEnRango((long) combustible - combustibleAConsumir)
                && entraEnRango((long) energia - energiaAConsumir);
    }

    /** @return {@code true} si {@link #desgastar(int)} con esa cantidad sería aceptada */
    public boolean puedeDesgastar(int cantidad) {
        return cantidad > 0 && entraEnRango((long) desgaste + cantidad);
    }

    // ------------------------------------------------------------------ operaciones

    /**
     * Suma combustible.
     *
     * <p><b>Precondición:</b> {@code cantidad > 0} y {@code combustible + cantidad <= MAXIMO}.<br>
     * <b>Postcondición:</b> {@code combustible = combustible_anterior + cantidad}; energía y
     * desgaste no cambian.</p>
     *
     * @throws IllegalArgumentException     si {@code cantidad <= 0}
     * @throws RecursoFueraDeRangoException si se superaría la capacidad máxima
     */
    public void cargarCombustible(int cantidad) {
        exigirPositiva("cantidad de combustible", cantidad);
        int nuevo = resultado("combustible", combustible, cantidad);
        combustible = nuevo;
        verificarInvariantes();
    }

    /**
     * Suma energía.
     *
     * <p><b>Precondición:</b> {@code cantidad > 0} y {@code energia + cantidad <= MAXIMO}.<br>
     * <b>Postcondición:</b> {@code energia = energia_anterior + cantidad}; combustible y
     * desgaste no cambian.</p>
     *
     * @throws IllegalArgumentException     si {@code cantidad <= 0}
     * @throws RecursoFueraDeRangoException si se superaría la capacidad máxima
     */
    public void cargarEnergia(int cantidad) {
        exigirPositiva("cantidad de energía", cantidad);
        int nuevo = resultado("energía", energia, cantidad);
        energia = nuevo;
        verificarInvariantes();
    }

    /**
     * Consume combustible y energía de forma <b>atómica</b>: o se descuentan ambos o no se
     * descuenta ninguno.
     *
     * <p><b>Precondición:</b> ambas cantidades {@code >= 0}, y {@code combustible >= combustibleAConsumir}
     * y {@code energia >= energiaAConsumir}.<br>
     * <b>Postcondición:</b> cada recurso disminuye exactamente en la cantidad indicada; el
     * desgaste no cambia.</p>
     *
     * @throws IllegalArgumentException     si alguna cantidad es negativa
     * @throws RecursoFueraDeRangoException si alguno de los recursos no alcanza
     */
    public void consumir(int combustibleAConsumir, int energiaAConsumir) {
        if (combustibleAConsumir < 0 || energiaAConsumir < 0) {
            throw new IllegalArgumentException("Las cantidades a consumir no pueden ser negativas");
        }
        // Se calculan ambos resultados antes de asignar: si alguno falla, no se toca nada.
        int nuevoCombustible = resultado("combustible", combustible, -(long) combustibleAConsumir);
        int nuevaEnergia = resultado("energía", energia, -(long) energiaAConsumir);
        combustible = nuevoCombustible;
        energia = nuevaEnergia;
        verificarInvariantes();
    }

    /**
     * Suma desgaste.
     *
     * <p><b>Precondición:</b> {@code cantidad > 0} y {@code desgaste + cantidad <= MAXIMO}.<br>
     * <b>Postcondición:</b> {@code desgaste = desgaste_anterior + cantidad}; combustible y
     * energía no cambian.</p>
     *
     * @throws IllegalArgumentException     si {@code cantidad <= 0}
     * @throws RecursoFueraDeRangoException si el desgaste superaría el máximo
     */
    public void desgastar(int cantidad) {
        exigirPositiva("cantidad de desgaste", cantidad);
        int nuevo = resultado("desgaste", desgaste, cantidad);
        desgaste = nuevo;
        verificarInvariantes();
    }

    /**
     * Realiza el mantenimiento de la nave.
     *
     * <p><b>Postcondición:</b> {@code desgaste == 0} (y por lo tanto
     * {@code requiereMantenimiento() == false}); combustible y energía no cambian.</p>
     */
    public void realizarMantenimiento() {
        desgaste = MINIMO;
        verificarInvariantes();
    }

    @Override
    public String toString() {
        return "Recursos[combustible=" + combustible + ", energia=" + energia
                + ", desgaste=" + desgaste + "]";
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

    /**
     * Calcula {@code actual + delta} sin modificar nada; si el resultado no entra en
     * {@code [MINIMO, MAXIMO]} lanza {@link RecursoFueraDeRangoException}. Usa {@code long}
     * para que valores enormes no desborden.
     */
    private static int resultado(String nombre, int actual, long delta) {
        long nuevo = actual + delta;
        if (!entraEnRango(nuevo)) {
            throw new RecursoFueraDeRangoException(
                    "La operación dejaría el " + nombre + " fuera de rango [" + MINIMO + ", " + MAXIMO
                            + "] (actual: " + actual + ", cambio: " + delta + ")");
        }
        return (int) nuevo;
    }

    /** Chequeo defensivo: detecta un estado inválido si algún cambio futuro rompiera la clase. */
    private void verificarInvariantes() {
        if (!entraEnRango(combustible) || !entraEnRango(energia) || !entraEnRango(desgaste)) {
            throw new IllegalStateException("Invariante de Recursos violada: " + this);
        }
    }
}
