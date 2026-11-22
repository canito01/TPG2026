package modelo.misiones;

import modelo.asistente.AsistenteDeComando;
import modelo.asistente.Resultado;
import modelo.asistente.ResumenRecursos;
import modelo.asistente.PosicionNave;

/**
 * M-01 - Interceptacion y asistencia: llegar a la nave aliada y transferirle recursos.
 *
 * @invariant destino != null
 * @invariant 0 <= energiaAuxilio <= 50 && 0 <= combustibleAuxilio <= 50
 */
public class Mision1 extends Mision {

    public static final int AUXILIO_MAXIMO = 50;
    private static final int ENERGIA_DESPEGUE = 5;

    private final PosicionNave posicionAliada;
    private final int energiaAuxilio;
    private final int combustibleAuxilio;

    /**
     * @pre posicionAliada != null
     * @pre 0 <= energiaAuxilio <= 50 && 0 <= combustibleAuxilio <= 50
     * @throws IllegalArgumentException si se viola alguna precondicion
     */
    public Mision1(PosicionNave posicionAliada, int energiaAuxilio, int combustibleAuxilio) {
        super("M-01");
        if (posicionAliada == null) {
            throw new IllegalArgumentException("Falta la posicion de la nave aliada");
        }
        if (energiaAuxilio < 0 || energiaAuxilio > AUXILIO_MAXIMO
                || combustibleAuxilio < 0 || combustibleAuxilio > AUXILIO_MAXIMO) {
            throw new IllegalArgumentException("El auxilio debe estar entre 0 y " + AUXILIO_MAXIMO);
        }
        this.posicionAliada = posicionAliada;
        this.energiaAuxilio = energiaAuxilio;
        this.combustibleAuxilio = combustibleAuxilio;
    }

    @Override
    protected PosicionNave determinarDestino(AsistenteDeComando a) {
        return posicionAliada;
    }

    @Override
    protected int energiaDespegue() {
        return ENERGIA_DESPEGUE;
    }

    /**
     * Al llegar se comprueba que la nave AUN conserve el auxilio prometido.
     * Aca no se descuenta nada: la transferencia efectiva ocurre en cerrar().
     */
    @Override
    protected void verificarEnDestino(AsistenteDeComando a) {
        ResumenRecursos recursos = a.consultarRecursos();
        if (recursos.getEnergia() < energiaAuxilio || recursos.getCombustible() < combustibleAuxilio) {
            fallar("No alcanzan los recursos para el auxilio: requiere energia " + energiaAuxilio
                    + " y combustible " + combustibleAuxilio + ", disponibles energia "
                    + recursos.getEnergia() + " y combustible " + recursos.getCombustible());
        } else {
            registrarAccion("Contacto con la nave aliada en " + coord(posicionAliada)
                    + ": recursos de auxilio verificados");
        }
    }

    /**
     * @pre la verificacion en destino fue exitosa
     * @post la nave transfirio energiaAuxilio y combustibleAuxilio
     * @throws IllegalStateException si la transferencia es rechazada (violaria el contrato de verificarEnDestino)
     */
    @Override
    protected void aplicarRecompensa(AsistenteDeComando a) {
        Resultado r = a.consumir(combustibleAuxilio, energiaAuxilio, 0);
        if (!r.fueExito()) {
            throw new IllegalStateException("La transferencia de auxilio fue rechazada: " + r.getMensaje());
        }
        a.registrarEvento("Auxilio descontado: " + energiaAuxilio + " de energía y "
                + combustibleAuxilio + " de combustible.");
    }

    @Override
    protected String describirRecompensa() {
        return "transferencia de auxilio (energia " + energiaAuxilio + ", combustible " + combustibleAuxilio + ")";
    }

    @Override
    protected String objetivo() {
        return "Interceptar la nave aliada en " + coord(posicionAliada) + " y asistirla";
    }

    @Override
    protected String descripcionResultado(boolean exito) {
        return exito ? "Exito: asistencia completada" : "Fracaso: no se pudo brindar asistencia";
    }
}
