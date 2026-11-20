package modelo.tripulacion;

import java.util.Objects;

/**
 * Representa un renglon del desglose del haber de un tripulante (concepto y aporte en PG).
 * Inmutable y pensado para que la vista muestre el detalle sin parsear cadenas de texto.
 */
public final class ItemHaber {

    private final String concepto;
    private final double aporte;

    public ItemHaber(String concepto, double aporte) {
        if (concepto == null || concepto.isBlank()) {
            throw new IllegalArgumentException("El concepto del haber no puede ser vacio.");
        }
        this.concepto = concepto;
        this.aporte = aporte;
    }

    public String getConcepto() {
        return concepto;
    }

    public double getAporte() {
        return aporte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemHaber)) return false;
        ItemHaber that = (ItemHaber) o;
        return Double.compare(that.aporte, aporte) == 0 && concepto.equals(that.concepto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(concepto, aporte);
    }

    @Override
    public String toString() {
        return concepto + ": " + aporte + " PG";
    }
}
