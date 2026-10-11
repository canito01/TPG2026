package modelo.misiones;

import modelo.asistente.AsistenteDeComando;

public class Mision1 extends Mision {

    public Mision1() {
        super("M-01");
    }

    @Override
    protected boolean evaluar(AsistenteDeComando a) {
        registrarAccion("Resultado evaluado");
        return operacionCompletada();
    }

    @Override
    protected InformeMision cerrar(AsistenteDeComando a) {
        boolean exito = fueExitosa();

        String resultado = exito ? "Exito: Asistencia completada" : "Fracaso: No se pudo brindar asistencia";
        String observaciones = exito ? "Se intercepto el objetivo y se brindo asistencia sin inconvenientes." : "La mision se interrumpio o no se alcanzo el objetivo.";

        registrarAccion("Mision " + getNombre() + " cerrada");

        InformeMision informe = new InformeMision(getNombre(), resultado, resumenRecursosConsumidos(), String.valueOf(a.consultarEstado()), observaciones);

        for (String accion : getAcciones()) {
            informe.agregarAccion(accion);
        }
        return informe;
    }

    @Override
    protected int energiaAdicional() {
        return 5;
    }
}