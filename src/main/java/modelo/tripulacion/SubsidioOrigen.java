package modelo.tripulacion;

public class SubsidioOrigen extends HaberDecorator{

    public SubsidioOrigen(ConceptoHaber envuelto, Tripulante tripulante){
        super(envuelto, tripulante);
    }

    private double getValorSubsidio(){
        switch (tripulante.getOrigen()){
            case TERRICOLA: return 20;
            case VULCANO: return 30;
            case MARCIANO: return 18;
            default: return 0;
        }
    }

    @Override
    public double calcularTotal(){
        return super.calcularTotal() + getValorSubsidio(); 
    }

    @Override
    public String obtenerDetalle(){
        return super.obtenerDetalle() + "\n+ Subsidio Origen (" + tripulante.getOrigen() + "): " + getValorSubsidio() + " PG";
    }
}
