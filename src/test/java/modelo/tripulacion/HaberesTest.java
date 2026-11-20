package modelo.tripulacion;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertThrows(IllegalArgumentException.class, () -> {
            new Tripulante("Consejero Roto", Cargo.CONSEJERO, Origen.VULCANO, 2, -1);
        }, "Debería rechazar consejos negativos");
    }

    @Test
    public void testEstadoVivoYMuerto() {
        Tripulante tripulante = new Tripulante("Uhura", Cargo.TENIENTE, Origen.TERRICOLA, 5);
        assertTrue(tripulante.estaVivo(), "Todo tripulante debe iniciar vivo");
        tripulante.morir();
        assertFalse(tripulante.estaVivo(), "Tras morir(), estaVivo() debe devolver false");
    }

    @Test
    public void testDesgloseHaberParaVista() {
        Tripulante consejero = new Tripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, 4, 3);

        List<ItemHaber> desglose = consejero.obtenerDesgloseHaber();
        assertEquals(4, desglose.size(), "Debe incluir base, antigüedad, consejos y subsidio");
        assertEquals(600.0, desglose.get(0).getAporte());
        assertEquals(120.0, desglose.get(1).getAporte());
        assertEquals(6.0, desglose.get(2).getAporte());
        assertEquals(30.0, desglose.get(3).getAporte());

        double sumaDesglose = desglose.stream().mapToDouble(ItemHaber::getAporte).sum();
        assertEquals(consejero.calcularHaberTotal(), sumaDesglose, "La suma del desglose debe coincidir con el total");
        assertEquals(756.0, consejero.calcularHaberTotal());

        assertThrows(UnsupportedOperationException.class, () -> {
            desglose.add(new ItemHaber("Extra", 10));
        }, "El desglose devuelto debe ser inmutable");
    }
}
