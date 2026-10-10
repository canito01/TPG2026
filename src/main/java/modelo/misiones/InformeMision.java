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
    public String getnombre() {
        return this.nombre;
    }
    public String getresultado() {
        return this.resultado;
    }
    public ArrayList<String> getacciones() {
        return this.acciones;
    }
    public String getrecursosconsumidos() {
        return this.recursosconsu;
    }

    public String getestadonave() {
        return this.estadonave;
    }

    public String getobservaciones() {
        return this.observaciones;
    }
    protected void agregaaccione(String accion){
        acciones.add(accion);
    }

}

