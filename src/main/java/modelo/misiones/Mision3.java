package modelo.misiones;

import modelo.asistente.AsistenteDeComando;
import destinoytablero.Posicion;

/**
 * M-03 - Retorno a punto seguro: volver a la base de origen, recargar al 100% y reparar.
 *
 * Sin estado propio: el destino lo da la nave (su base de origen).
 */
public class Mision3 extends Mision {

    public Mision3() {
        super("M-03");
    }

    @Override
    protected Posicion determinarDestino(AsistenteDeComando a) {
        Posicion base = a.getPosicionBase();
        if (base == null) {
            throw new IllegalStateException("La nave no tiene una base de origen definida");
        }
        return base;
    }

    @Override
    protected int energiaDespegue() {
        return 0;
    }

    /** @post combustible y energia al maximo y desgaste == 0 */
    @Override
    protected void aplicarRecompensa(AsistenteDeComando a) {
        a.reabastecerYRepararTotal();
    }

    @Override
    protected String describirRecompensa() {
        return "recarga total de combustible y energia, desgaste en 0";
    }

    @Override
    protected String objetivo() {
        return "Regresar a la base de origen para reabastecerse y reparar la nave";
    }

    @Override
    protected String descripcionResultado(boolean exito) {
        return exito ? "Exito: retorno completado" : "Fracaso: la nave no pudo retornar de forma segura";
    }
}