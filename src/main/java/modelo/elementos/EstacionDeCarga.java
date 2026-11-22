package modelo.elementos;

import modelo.nave.Nave;
import modelo.nave.Recursos;

/**
 * Punto de asistencia: estación de carga que permite reabastecer a la nave,
 * recargando su combustible y energía ya sea en un monto fijo o en un porcentaje
 * sobre sus recursos actuales, siempre respetando el tope máximo de 100.
 */
public class EstacionDeCarga implements ElementoDelUniverso {

    public static final String NOMBRE = "EstacionDeCarga";
    public static final int RECARGA_FIJA_POR_DEFECTO = 25;

    private final boolean esPorPorcentaje;
    private final int combustibleFijo;
    private final int energiaFija;
    private final int porcentaje;

    /** Crea una estación de carga por defecto con monto fijo (+25 combustible, +25 energía). */
    public EstacionDeCarga() {
        this(false, RECARGA_FIJA_POR_DEFECTO, RECARGA_FIJA_POR_DEFECTO, 0);
    }

    /** Crea una estación de carga que repone montos fijos de combustible y energía. */
    public EstacionDeCarga(int combustibleFijo, int energiaFija) {
        this(false, combustibleFijo, energiaFija, 0);
    }

    private EstacionDeCarga(boolean esPorPorcentaje, int combustibleFijo, int energiaFija, int porcentaje) {
        if (!esPorPorcentaje) {
            if (combustibleFijo < 0 || energiaFija < 0 || (combustibleFijo == 0 && energiaFija == 0)) {
                throw new IllegalArgumentException("Los montos fijos deben ser no negativos y al menos uno mayor a 0");
            }
        } else {
            if (porcentaje <= 0 || porcentaje > 100) {
                throw new IllegalArgumentException("El porcentaje de recarga debe estar entre 1 y 100 (recibido: " + porcentaje + ")");
            }
        }
        this.esPorPorcentaje = esPorPorcentaje;
        this.combustibleFijo = combustibleFijo;
        this.energiaFija = energiaFija;
        this.porcentaje = porcentaje;
    }

    /** Fábrica estática para estación de carga por monto fijo. */
    public static EstacionDeCarga porMontoFijo(int combustible, int energia) {
        return new EstacionDeCarga(false, combustible, energia, 0);
    }

    /** Fábrica estática para estación de carga por porcentaje sobre los recursos actuales. */
    public static EstacionDeCarga porPorcentaje(int porcentaje) {
        return new EstacionDeCarga(true, 0, 0, porcentaje);
    }

    public boolean esPorPorcentaje() {
        return esPorPorcentaje;
    }

    public int getCombustibleFijo() {
        return combustibleFijo;
    }

    public int getEnergiaFija() {
        return energiaFija;
    }

    public int getPorcentaje() {
        return porcentaje;
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
        Recursos r = nave.getRecursos();
        int combAntes = r.getCombustible();
        int enerAntes = r.getEnergia();

        int combExtra;
        int enerExtra;
        if (esPorPorcentaje) {
            combExtra = (combAntes * porcentaje) / 100;
            enerExtra = (enerAntes * porcentaje) / 100;
        } else {
            combExtra = combustibleFijo;
            enerExtra = energiaFija;
        }

        r.recargarHastaElTope(combExtra, enerExtra);
        int combRecargado = r.getCombustible() - combAntes;
        int enerRecargada = r.getEnergia() - enerAntes;

        return "Asistencia en Estación de Carga: +" + combRecargado + " combustible, +"
                + enerRecargada + " energía (actual: " + r.getCombustible() + "/" + r.getEnergia() + ").";
    }
}
