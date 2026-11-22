package modelo.motor;

/**
 * Motor Warp: contexto del patron State (Anexo). Hacia afuera expone SOLO dos
 * operaciones, saltar() y saltarWarp(): en cada turno la mision (por medio del
 * asistente) invoca una, y el estado actual decide que ocurre.
 *
 * El motor es un subsistema de la nave: opera sobre ella a traves de
 * SistemasDeLaNave (recursos y movimiento) y devuelve un ResultadoDeTurno con
 * lo ocurrido para que el asistente lo registre. No conoce la bitacora, el
 * tablero ni las misiones, y no usa consola ni Swing.
 *
 * Invariante de clase: la nave y el estado actual nunca son null.
 */
public class MotorWarp {

    private final SistemasDeLaNave nave;
    private EstadoMotor estado;

    /**
     * Constructor sin argumentos de compatibilidad con NaveFactory y pruebas de Nave (Entrega 1).
     * Post: el motor queda Disponible con sistemas internos por defecto.
     */
    public MotorWarp() {
        this(sistemasPorDefecto());
    }

    /**
     * Pre: nave != null (si no: IllegalArgumentException).
     * Post: el motor queda Disponible.
     */
    public MotorWarp(SistemasDeLaNave nave) {
        if (nave == null) {
            throw new IllegalArgumentException("El motor necesita la nave sobre la que opera");
        }
        this.nave = nave;
        this.estado = new Disponible();
        assert invariante() : "Fallo invariante";
    }

    /**
     * Un turno con salto simple. Pre: ninguna. Post: el estado actual resolvio el
     * turno (ver EstadoMotor); devuelve lo ocurrido.
     */
    public ResultadoDeTurno saltar() {
        return estado.saltar(this);
    }

    /**
     * Un turno con salto warp. Pre: ninguna. Post: el estado actual resolvio el
     * turno (ver EstadoMotor); devuelve lo ocurrido.
     */
    public ResultadoDeTurno saltarWarp() {
        return estado.saltarWarp(this);
    }

    // ---- Solo para los estados del paquete (no son parte de la API publica)

    SistemasDeLaNave getNave() { return nave; }

    void setEstado(EstadoMotor nuevo) {
        assert nuevo != null : "Estado nuevo nulo";
        this.estado = nuevo;
        assert invariante() : "Fallo invariante";
    }

    private boolean invariante() { return nave != null && estado != null; }

    private static SistemasDeLaNave sistemasPorDefecto() {
        return new SistemasDeLaNave() {
            private int combustible = 100;
            private int energia = 100;
            private int desgaste = 0;

            @Override
            public int getCombustible() { return combustible; }

            @Override
            public int getEnergia() { return energia; }

            @Override
            public void consumirCombustible(int cantidad) {
                combustible = Math.max(0, combustible - cantidad);
            }

            @Override
            public void consumirEnergia(int cantidad) {
                energia = Math.max(0, energia - cantidad);
            }

            @Override
            public void agregarDesgaste(int cantidad) {
                desgaste = Math.min(100, desgaste + cantidad);
            }

            @Override
            public int avanzar(int casillas) {
                return casillas;
            }
        };
    }
}
