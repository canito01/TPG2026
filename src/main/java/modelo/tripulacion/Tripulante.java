package modelo.tripulacion;

import java.util.List;

public class Tripulante {
    
    private String identidad;
    private Cargo cargo;
    private Origen origen;
    private int antiguedad;
    private int cantidadConsejos;
    private boolean vivo;

    public Tripulante(String identidad, Cargo cargo, Origen origen, int antiguedad){
        this(identidad, cargo, origen, antiguedad, 0);
    }

    public Tripulante(String identidad, Cargo cargo, Origen origen, int antiguedad, int cantidadConsejos){
        if (antiguedad < 0){ //validacion para antiguedad no negativa
            throw new IllegalArgumentException("La antiguedad no puede ser negativa."); //esto lo agarra el catch del que cree los tripulantes
        }
        if (cantidadConsejos < 0){
            throw new IllegalArgumentException("La cantidad de consejos no puede ser negativa.");
        }
        this.identidad = identidad;
        this.cargo = cargo;
        this.origen = origen;
        this.antiguedad = antiguedad;
        this.cantidadConsejos = cantidadConsejos;
        this.vivo = true;
    }

    public String getIdentidad(){
        return identidad;
    }

    public Cargo getCargo(){
        return cargo;
    }

    public Origen getOrigen(){
        return origen;
    }

    public int getAntiguedad(){
        return antiguedad;
    }

    public int getCantidadConsejos(){
        return cantidadConsejos;
    }

    public boolean estaVivo(){
        return vivo;
    }

    public void morir(){
        this.vivo = false;
    }

    /**
     * Construye la cadena Decorator completa de haberes para este tripulante.
     */
    public ConceptoHaber obtenerHaber(){
        ConceptoHaber haber = new SueldoBase(this);
        haber = new BonoAntiguedad(haber, this);
        if (cargo == Cargo.CONSEJERO && cantidadConsejos > 0){
            haber = new BonoConsejero(haber, this, cantidadConsejos);
        }
        haber = new SubsidioOrigen(haber, this);
        return haber;
    }

    public double calcularHaberTotal(){
        return obtenerHaber().calcularTotal();
    }

    public List<ItemHaber> obtenerDesgloseHaber(){
        return obtenerHaber().obtenerDesglose();
    }
}
