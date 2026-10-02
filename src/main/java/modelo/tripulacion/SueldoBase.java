package modelo.tripulacion;

public class SueldoBase implements ConceptoHaber{

    private Tripulante tripulante;

    public SueldoBase(Tripulante tripulante){
        this.tripulante = tripulante;
    }

    private double getValorBase(){
        switch (tripulante.getCargo()){
            case CAPITAN: return 1000;
            case CONSEJERO: return 600;
            case TENIENTE: return 400;
            case ALFEREZ: return 200;
            default: return 0;
        }
    }

    @Override
    public double calcularTotal(){
        return getValorBase();
    }

    @Override
    public String obtenerDetalle(){
        return "Sueldo Base (" + tripulante.getCargo() + "): " + getValorBase() + " PG";
    }
}
