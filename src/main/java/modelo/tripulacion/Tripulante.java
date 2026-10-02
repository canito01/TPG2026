package modelo.tripulacion;

public class Tripulante {
    
    private String identidad;
    private Cargo cargo;
    private Origen origen;
    private int antiguedad;

    public Tripulante(String identidad,Cargo cargo,Origen origen,int antiguedad){
    
        if (antiguedad < 0){ //validacion para antiguedad no negativa
            throw new IllegalArgumentException("La antiguedad no puede ser negativa."); //esto lo agarra el catch del que cree los tripulantes
        }
        this.identidad = identidad;
        this.cargo = cargo;
        this.origen = origen;
        this.antiguedad = antiguedad;
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
}
