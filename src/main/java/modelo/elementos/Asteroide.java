package modelo.elementos;

import modelo.nave.Nave;

/**
 * Obstáculo espacial: campo de asteroides que impacta contra el casco y descuenta
 * un monto fijo de energía. Si como consecuencia del impacto la energía de la nave
 * llega a 0 (o menos), la nave queda destruida/inoperativa junto con su tripulación.
 */
public class Asteroide implements ElementoDelUniverso {

    public static final String NOMBRE = "Asteroide";
    public static final int DANO_ENERGIA_POR_DEFECTO = 20;

    private final int danoEnergia;

    /** Crea un asteroide con el daño fijo de energía por defecto (20 puntos). */
    public Asteroide() {
        this(DANO_ENERGIA_POR_DEFECTO);
    }

    /**
     * Crea un asteroide con un monto fijo de daño de energía.
     * Precondición: {@code danoEnergia > 0}.
     */
    public Asteroide(int danoEnergia) {
        if (danoEnergia <= 0) {
            throw new IllegalArgumentException("El daño de energía del asteroide debe ser positivo (recibido: " + danoEnergia + ")");
        }
        this.danoEnergia = danoEnergia;
    }

    public int getDanoEnergia() {
        return danoEnergia;
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String interactuarCon(Nave nave) {
        if (nave == null) {
            throw new IllegalArgumentException("La nave no puede ser nula");
        }
        int energiaAntes = nave.getRecursos().getEnergia();
        nave.getRecursos().perderEnergiaHastaCero(danoEnergia);
        int descontado = energiaAntes - nave.getRecursos().getEnergia();
        if (nave.estaDestruida()) {
            return "Impacto con Asteroide (-" + descontado + " energía): energía agotada (0), nave destruida.";
        }
        return "Impacto con Asteroide: -" + descontado + " de energía (energía restante: "
                + nave.getRecursos().getEnergia() + ").";
    }
}
