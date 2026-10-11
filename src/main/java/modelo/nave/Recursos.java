package modelo.nave;

/**
 * Recursos de una nave: combustible, energía y desgaste (todos enteros).
 *
 * Invariantes de clase</
 *
 *   0 <= combustible <= MAXIMO
 *   0 <= energia <= MAXIMO
 *   0 <= desgaste <= MAXIMO
 *   #requiereMantenimiento() es verdadero si y solo si
 *    desgaste >= UMBRAL_MANTENIMIENTO
 *
 * Las invariantes se cumplen al terminar el constructor y antes y después de cada operación
 * pública. El estado es privado, por lo que solo estas operaciones pueden modificarlo.
 *
 * Garantía común de las operaciones que modifican
 * Si una operación no puede completarse sin violar un límite, lanza
 *  RecursoFueraDeRangoException y el objeto queda exactamente como estaba
 * (sin cambios parciales). Si el argumento no cumple la precondición (valor negativo o
 * nulo según el método), lanza  IllegalArgumentException} y tampoco modifica nada.
 *
 * Las consultas  puede permitir decidir de antemano, sin lanzar excepciones,
 * si una operación sería aceptada.
 *
 * Esta clase no depende de consola ni de Swing.
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
     * Precondición: cada valor está en  [MINIMO, MAXIMO]}.<br>
     * Postcondición: los recursos quedan con los valores indicados y se cumplen las
     * invariantes.
     *
     * @throws RecursoFueraDeRangoException si algún valor está fuera de  [0, 100]}
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

    /** @return  true si el desgaste es  UMBRAL_MANTENIMIENTO o superior */
    public boolean requiereMantenimiento() {
        return desgaste >= UMBRAL_MANTENIMIENTO;
    }

    /**
     * Copia independiente del estado actual. Útil para tomar una instantánea antes de una
     * operacin (por ejemplo, para calcular lo consumido por una misión).
     */
    public Recursos copia() {
        return new Recursos(combustible, energia, desgaste);
    }

    /** @return  verdadero si  #cargarCombustible(int) con esa cantidad sería aceptada */
    public boolean puedeCargarCombustible(int cantidad) {
        return cantidad > 0 && entraEnRango((long) combustible + cantidad);
    }

    /** @return  true si  #cargarEnergia(int) con esa cantidad sería aceptada */
    public boolean puedeCargarEnergia(int cantidad) {
        return cantidad > 0 && entraEnRango((long) energia + cantidad);
    }

    /** @return  true si  #consumir(int, int)} con esas cantidades sería aceptada */
    public boolean puedeConsumir(int combustibleAConsumir, int energiaAConsumir) {
        return combustibleAConsumir >= 0 && energiaAConsumir >= 0
                && entraEnRango((long) combustible - combustibleAConsumir)
                && entraEnRango((long) energia - energiaAConsumir);
    }

    /** @return  true si  #desgastar(int) con esa cantidad sería aceptada */
    public boolean puedeDesgastar(int cantidad) {
        return cantidad > 0 && entraEnRango((long) desgaste + cantidad);
    }

    // ------------------------------------------------------------------ operaciones

    /**
     * Suma combustible.
     *
     * Precondición:  cantidad > 0} y  combustible + cantidad <= MAXIMO}.<br>
     * Postcondición:  combustible = combustible_anterior + cantidad}; energía y
     * desgaste no cambian.
     *
     * @throws IllegalArgumentException     si  cantidad <= 0}
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
     * Precondición:  cantidad > 0} y  energia + cantidad <= MAXIMO}.<br>
     * Postcondición:  energia = energia_anterior + cantidad}; combustible y
     * desgaste no cambian.
     *
     * @throws IllegalArgumentException     si  cantidad <= 0}
     * @throws RecursoFueraDeRangoException si se superaría la capacidad máxima
     */
    public void cargarEnergia(int cantidad) {
        exigirPositiva("cantidad de energía", cantidad);
        int nuevo = resultado("energía", energia, cantidad);
        energia = nuevo;
        verificarInvariantes();
    }

    /**
     * Consume combustible y energía de forma atómica: o se descuentan ambos o no se
     * descuenta ninguno.
     *
     * Precondición: ambas cantidades  >= 0}, y  combustible >= combustibleAConsumir}
     * y  energia >= energiaAConsumir}.<br>
     * Postcondición: cada recurso disminuye exactamente en la cantidad indicada; el
     * desgaste no cambia.
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
     * Precondición:  cantidad > 0} y  desgaste + cantidad <= MAXIMO}.<br>
     * Postcondición:  desgaste = desgaste_anterior + cantidad}; combustible y
     * energía no cambian.
     *
     * @throws IllegalArgumentException     si  cantidad <= 0}
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
     * postcondición:  desgaste == 0} (y por lo tanto
     *  requiereMantenimiento() == false}); combustible y energía no cambian.
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
     * Calcula  actual + delta sin modificar nada; si el resultado no entra en
     *  [MINIMO, MAXIMO] lanza  RecursoFueraDeRangoException}. Usa  long
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
