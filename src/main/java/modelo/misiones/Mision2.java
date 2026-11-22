package modelo.misiones;

import java.util.concurrent.ThreadLocalRandom;
import modelo.asistente.AsistenteDeComando;
import destinoytablero.Posicion;

/**
 * M-02 - Recoleccion: llegar a un punto de interes y obtener gemas.
 *
 * @invariant puntoDeInteres != null
 * @invariant 20 <= gemas <= 100
 */
public class Mision2 extends Mision {

    public static final int GEMAS_MIN = 20;
    public static final int GEMAS_MAX = 100;
    private static final int ENERGIA_DESPEGUE = 10;

    private final Posicion puntoDeInteres;
    private final int gemas;

    /**
     * @pre puntoDeInteres != null && 20 <= gemas <= 100
     * @throws IllegalArgumentException si se viola alguna precondicion
     */
    public Mision2(Posicion puntoDeInteres, int gemas) {
        super("M-02");
        if (puntoDeInteres == null) {
            throw new IllegalArgumentException("Falta el punto de interes");
        }
        if (gemas < GEMAS_MIN || gemas > GEMAS_MAX) {
            throw new IllegalArgumentException("La recompensa debe estar entre "
                    + GEMAS_MIN + " y " + GEMAS_MAX + " gemas");
        }
        this.puntoDeInteres = puntoDeInteres;
        this.gemas = gemas;
    }

    /** Recompensa variable: sortea las gemas entre 20 y 100. */
    public Mision2(Posicion puntoDeInteres) {
        this(puntoDeInteres, ThreadLocalRandom.current().nextInt(GEMAS_MIN, GEMAS_MAX + 1));
    }

    @Override
    protected Posicion determinarDestino(AsistenteDeComando a) {
        return puntoDeInteres;
    }

    @Override
    protected int energiaDespegue() {
        return ENERGIA_DESPEGUE;
    }

    /** @post la nave tiene {@code gemas} gemas mas */
    @Override
    protected void aplicarRecompensa(AsistenteDeComando a) {
        a.agregarGemas(gemas);
    }

    @Override
    protected String describirRecompensa() {
        return gemas + " gemas obtenidas";
    }

    @Override
    protected String objetivo() {
        return "Recolectar gemas en el punto de interes " + coord(puntoDeInteres);
    }

    @Override
    protected String descripcionResultado(boolean exito) {
        return exito ? "Exito: " + gemas + " gemas recolectadas" : "Fracaso: no se pudo recolectar";
    }
}