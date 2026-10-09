package modelo.misiones;

public class Mision3 extends Mision {

    public Mision3() {
        super("M3);
    }

    @Override
    protected boolean evaluar(AsistenteDeComando a) {
        String estado = a.consultarEstado();
        return estado.equals("Operativo");
    }

    @Override
    protected InformeMision cerrar(AsistenteDeComando a) {
        String estadonave = a.consultarEstado();
        String resultado;
        String observaciones;
        String recursosconsu = a.consultarRecursos().toString();

        boolean fueExito = this.evaluar(a);

        if (fueExito == true) {
            resultado = "Éxito: Retorno completado";
        } else {
            resultado = "Fracaso: La nave no pudo retornar de forma segura";
        }

        if (fueExito == true) {
            observaciones = "La nave alcanzó la zona designada y cerró su operación correctamente";
        } else {
            observaciones = "La nave no logró llegar a la zona de referencia dentro de los parámetros seguros";
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
        return 0;
    }
}