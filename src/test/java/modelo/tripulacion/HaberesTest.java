package modelo.tripulacion;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HaberesTest {

    @Test
    public void testCapitanTerricola10Anios() {
        //capitan, 10 años, terrícola = 3020 PG
        Tripulante capitan = new Tripulante("James Kirk", Cargo.CAPITAN, Origen.TERRICOLA, 10);
        
        ConceptoHaber sueldo = new SueldoBase(capitan);
        sueldo = new BonoAntiguedad(sueldo, capitan);
        sueldo = new SubsidioOrigen(sueldo, capitan);
        
        //imprimimos el resultado en la consola para poder leerlo
        System.out.println("=== TEST CAPITAN ===");
        System.out.println(sueldo.obtenerDetalle());
        System.out.println("TOTAL FINAL: " + sueldo.calcularTotal() + " PG\n");
        
        //El Assert prueba que nuestro cálculo matemático dé exactamente 3020
        assertEquals(3020.0, sueldo.calcularTotal(), "El sueldo del Capitán Terrícola falló");
    }

    @Test
    public void testConsejeroVulcano4Anios3Consejos() {
        //consejero, 4 años, 3 consejos, vulcano = 756 PG
        Tripulante consejero = new Tripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, 4);
        
        ConceptoHaber sueldo = new SueldoBase(consejero);
        sueldo = new BonoAntiguedad(sueldo, consejero);
        sueldo = new BonoConsejero(sueldo, consejero, 3);
        sueldo = new SubsidioOrigen(sueldo, consejero);
        
        //imprimimos el resultado en la consola para poder leerlo
        System.out.println("=== TEST CONSEJERO ===");
        System.out.println(sueldo.obtenerDetalle());
        System.out.println("TOTAL FINAL: " + sueldo.calcularTotal() + " PG\n");
        
        //el Assert prueba que nuestro calculo de exactamente 756
        assertEquals(756.0, sueldo.calcularTotal(), "El sueldo del Consejero Vulcano falló");
    }

    @Test
    public void testTripulanteInvalidoLanzaExcepcion() {
        //Tambien probamos que si alguien manda años negativos, lance la excepcion
        assertThrows(IllegalArgumentException.class, () -> {
            new Tripulante("Tripulante Roto", Cargo.ALFEREZ, Origen.MARCIANO, -5);
        }, "Debería rechazar antigüedades negativas");
    }
}
