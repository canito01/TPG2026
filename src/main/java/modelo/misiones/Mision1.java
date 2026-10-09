package modelo.misiones;

public class Mision1 extends Mision {

    public Mision1(){
        super("M1");
    }

    @Override
    protected boolean evaluar(AsistenteDeComando a) {
        String estado = a.consultarEstado();
        return estado.equals("Asistencia completada");
    }

    @Override
    protected InformeMision cerrar(AsistenteDeComando a) {

        String estadonave = a.consultarEstado();
        String resultado;
        String observaciones;
        String recursosconsu = a.consultarRecursos().toString();
        boolean fueExito = this.evaluar(a);


        if(fueExito == true) {
            resultado = "Éxito: Asistencia completada";
        } else {
            resultado = "Fracaso: No se pudo brindar asistencia";
        }

        if(fueExito == true) {
            observaciones = "Se interceptó el objetivo y se brindó asistencia sin inconvenientes.";
        } else {
            observaciones = "La misión se interrumpió o no se alcanzó el objetivo.";
        }


        InformeMision informeGenerado = new InformeMision(
                this.getNombre(),
                resultado,
                recursosconsu,
                estadonave,
                observaciones);

        return informeGenerado;
    }

    /**
     * @return La energía extra requerida por el objetivo de intercepción.
     */
    @Override
    protected int energiaAdicional() {
        return 5;
    }
}