package modelo.misiones;

import modelo.asistente.AsistenteDeComando;

public class Mision2 extends Mision {

    public Mision2() {
        super("M-02");
    }

    @Override
    protected boolean evaluar(AsistenteDeComando a) {
        registrarAccion("Resultado evaluado");
        return operacionCompletada();
    }

    @Override
    protected InformeMision cerrar(AsistenteDeComando a) {
        boolean exito = fueExitosa();

        String resultado = exito ? "Exito: Elemento obtenido" : "Fracaso: No se pudo recolectar";
        String observaciones = exito ? "Muestra o datos registrados correctamente." : "La operacion de recoleccion resulto inviable o fue interrumpida.";

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