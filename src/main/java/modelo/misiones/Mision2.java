package modelo.misiones;

public class Mision2 extends Mision {

    public Mision2() {
        super("M");
    }

    @Override
    protected boolean evaluar(AsistenteDeComando a) {
        String estado = a.consultarEstado();
        return estado.equals("Elemento obtenido");
    }

    @Override
    protected InformeMision cerrar(AsistenteDeComando a) {
        String estadonave = a.consultarEstado();
        String resultado;
        String observaciones;
        String recursosconsu = a.consultarRecursos().toString();

        boolean fueExito = this.evaluar(a);

        if (fueExito == true) {
            resultado = "Éxito: Elemento obtenido";
        } else {
            resultado = "Fracaso: No se pudo recolectar";
        }

        if (fueExito == true) {
            observaciones = "Muestra o datos guardados correctamente en la bodega de carga.";
        } else {
            observaciones = "La operación de recolección resultó inviable o fue interrumpida.";
        }

        InformeMision informeGenerado = new InformeMision(
                this.getNombre(),
                resultado,
                recursosconsu,
                estadonave,
                observaciones
        );

        return informeGenerado;
    }

    @Override
    protected int energiaAdicional() {
        return 5;
    }
}