package modelo.tripulacion;

import java.util.List;

public abstract class HaberDecorator implements ConceptoHaber{

    protected ConceptoHaber envuelto; //el concepto que estamos envolviendo
    protected Tripulante tripulante;  //tripulante para calcular los bonos

    public HaberDecorator(ConceptoHaber envuelto, Tripulante tripulante){
        this.envuelto = envuelto;
        this.tripulante = tripulante;
    } //las clases que extienden al decorator solo deben llamar a esta funcion con super(concepto,tripulant)

    @Override
    public double calcularTotal(){
        return envuelto.calcularTotal(); //le pasa la responsabilidad al de adentro hasta llegar al sueldo base
    }

    @Override
    public String obtenerDetalle(){
        return envuelto.obtenerDetalle(); //igual que con el total del sueldo
    }

    @Override
    public List<ItemHaber> obtenerDesglose(){
        return envuelto.obtenerDesglose();
    }
}
