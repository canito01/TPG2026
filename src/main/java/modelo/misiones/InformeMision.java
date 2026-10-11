package modelo.misiones;

import java.util.ArrayList;

public class InformeMision {
    private String nombre;
    private String resultado;
    private ArrayList<String> acciones = new ArrayList<>();
    private String recursosconsu;
    private String estadonave;
    private String observaciones;

    protected InformeMision(String nombre,String resultado,String recursosconsu,String estadonave,String observaciones){
        this.nombre = nombre;
        this.resultado = resultado;
        this.recursosconsu=recursosconsu;
        this.estadonave=estadonave;
        this.observaciones=observaciones;
    }
    public String getNombre() {
        return this.nombre;
    }
    public String getResultado() {
        return this.resultado;
    }
    public ArrayList<String> getAcciones() {
        return new ArrayList<>(acciones);
    }
    public String getRecursosConsumidos() {
        return this.recursosconsu;
    }

    public String getEstadoNave() {
        return this.estadonave;
    }

    public String getObservaciones() {
        return this.observaciones;
    }
    protected void agregarAccion(String accion){
        acciones.add(accion);
    }

}

