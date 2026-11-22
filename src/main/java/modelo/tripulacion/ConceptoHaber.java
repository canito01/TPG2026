package modelo.tripulacion;

import java.util.List;

public interface ConceptoHaber{
    
    double calcularTotal();//sueldo total en PG
    
    String obtenerDetalle();//devuelve un texto explicando de donde sale el sueldo 

    List<ItemHaber> obtenerDesglose();//devuelve el desglose estructurado (concepto y aporte) para la vista
}
