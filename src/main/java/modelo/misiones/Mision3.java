package modelo.misiones;

import modelo.asistente.AsistenteDeComando;

public class Mision3 extends Mision {

    public Mision3() {
        super("M-03");
    }

    @Override
    protected boolean evaluar(AsistenteDeComando a) {
        registrarAccion("Resultado evaluado");
        return operacionCompletada();
    }

    @Override
    protected InformeMision cerrar(AsistenteDeComando a) {
        boolean exito = fueExitosa();

        String resultado = exito ? "Exito: Retorno completado" : "Fracaso: La nave no pudo retornar de forma segura";
        String observaciones = exito ? "La nave alcanzo la zona designada y cerro su operacion correctamente." : "La nave no logro llegar a la zona de referencia dentro de los parametros seguros.";

        registrarAccion("Mision " + getNombre() + " cerrada");

        InformeMision informe = new InformeMision(getNombre(), resultado, resumenRecursosConsumidos(), String.valueOf(a.consultarEstado()), observaciones);

        for (String accion : getAcciones()) {
            informe.agregarAccion(accion);
        }
        return informe;
    }

    @Override
    protected int energiaAdicional() {
        return 0;
    }
}