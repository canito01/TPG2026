package modelo.tripulacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BonoConsejero extends HaberDecorator{

    private int cantidadConsejos;

    //este bono a diferencia de los demas, se le pasa una variable extra, la cantidad de consejos
    public BonoConsejero(ConceptoHaber envuelto, Tripulante tripulante, int cantidadConsejos){
        super(envuelto, tripulante);
        if (cantidadConsejos < 0){
            throw new IllegalArgumentException("La cantidad de consejos no puede ser negativa");
        }
        this.cantidadConsejos = cantidadConsejos;
    }

    private double getValorBono(){//solo aplicamos el bono si el tripulante es CONSEJERO
        if (tripulante.getCargo() == Cargo.CONSEJERO){
            return cantidadConsejos * 2.0; //2 PG por consejo
        }
        return 0; 
    }

    @Override
    public double calcularTotal(){
        return super.calcularTotal() + getValorBono(); 
    }

    @Override
    public String obtenerDetalle(){
        if (getValorBono() > 0){
            return super.obtenerDetalle() + "\n+ Bono Consejero (" + cantidadConsejos + " consejos): " + getValorBono() + " PG";
        }
        return super.obtenerDetalle(); //si no es consejero o dio 0 consejos, no imprimimos nada
    }

    @Override
    public List<ItemHaber> obtenerDesglose(){
        List<ItemHaber> items = new ArrayList<>(super.obtenerDesglose());
        if (getValorBono() > 0){
            items.add(new ItemHaber("Bono Consejero (" + cantidadConsejos + " consejos)", getValorBono()));
        }
        return Collections.unmodifiableList(items);
    }
}
