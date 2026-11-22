package modelo.tripulacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BonoAntiguedad extends HaberDecorator{

    public BonoAntiguedad(ConceptoHaber envuelto, Tripulante tripulante){
        super(envuelto, tripulante);
    }

    private double getValorBono(){
        double base = 0;
        double porcentaje = 0;
        
        switch (tripulante.getCargo()){
            case CAPITAN: base = 1000; porcentaje = 0.20; break;
            case CONSEJERO: base = 600; porcentaje = 0.05; break;
            case TENIENTE: base = 400; porcentaje = 0.03; break;
            case ALFEREZ: base = 200; porcentaje = 0.005; break;
        }
        
        //calculado sobre la base del cargo por año
        return base * porcentaje * tripulante.getAntiguedad();
    }

    @Override
    public double calcularTotal(){
        return super.calcularTotal() + getValorBono(); 
    }

    @Override
    public String obtenerDetalle(){
        return super.obtenerDetalle() + "\n+ Bono Antigüedad (" + tripulante.getAntiguedad() + " años): " + getValorBono() + " PG";
    }

    @Override
    public List<ItemHaber> obtenerDesglose(){
        List<ItemHaber> items = new ArrayList<>(super.obtenerDesglose());
        items.add(new ItemHaber("Bono Antigüedad (" + tripulante.getAntiguedad() + " años)", getValorBono()));
        return Collections.unmodifiableList(items);
    }
}
