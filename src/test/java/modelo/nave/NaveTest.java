package modelo.nave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import modelo.elementos.ResultadoEncuentro;
import modelo.motor.MotorWarp;
import modelo.navegacion.Coordenada;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.Origen;
import modelo.tripulacion.Tripulante;

class NaveTest {

    @Test
    void exploradoraComienzaConLaConfiguracionDeLaFicha() {
        Nave n = new NaveExploradora("EXP-1", new MotorWarp());
        assertEquals(TipoNave.EXPLORADORA, n.getTipo());
        assertEquals(60, n.getRecursos().getCombustible());
        assertEquals(80, n.getRecursos().getEnergia());
        assertEquals(0, n.getRecursos().getDesgaste());
    }

    @Test
    void cargueroComienzaConLaConfiguracionDeLaFicha() {
        Nave n = new NaveCarguero("CAR-1", new MotorWarp());
        assertEquals(TipoNave.CARGUERO, n.getTipo());
        assertEquals(100, n.getRecursos().getCombustible());
        assertEquals(60, n.getRecursos().getEnergia());
        assertEquals(0, n.getRecursos().getDesgaste());
    }

    @Test
    void combateComienzaConLaConfiguracionDeLaFicha() {
        Nave n = new NaveCombate("COM-1", new MotorWarp());
        assertEquals(TipoNave.COMBATE, n.getTipo());
        assertEquals(80, n.getRecursos().getCombustible());
        assertEquals(100, n.getRecursos().getEnergia());
        assertEquals(0, n.getRecursos().getDesgaste());
    }

    @Test
    void laNaveConservaIdYMotorYComienzaSinTripulantes() {
        MotorWarp motor = new MotorWarp();
        Nave n = new NaveCombate("COM-7", motor);
        assertEquals("COM-7", n.getId());
        assertSame(motor, n.getMotor());
        assertTrue(n.getTripulacion().isEmpty());
        assertEquals(new Coordenada(0, 0), n.getPosicion());
        assertEquals(new Coordenada(0, 0), n.getBaseDeOrigen());
    }

    @Test
    void noSeCreaConIdNuloOVacio() {
        assertThrows(IllegalArgumentException.class, () -> new NaveExploradora(null, new MotorWarp()));
        assertThrows(IllegalArgumentException.class, () -> new NaveExploradora("", new MotorWarp()));
        assertThrows(IllegalArgumentException.class, () -> new NaveExploradora("   ", new MotorWarp()));
    }

    @Test
    void noSeCreaSinMotor() {
        assertThrows(IllegalArgumentException.class, () -> new NaveCarguero("CAR-1", null));
    }

    @Test
    void agregarTripulanteLoIncorporaALaTripulacion() {
        Nave n = new NaveExploradora("EXP-1", new MotorWarp());
        Tripulante t = new Tripulante("T-1", Cargo.CAPITAN, Origen.TERRICOLA, 5);
        n.agregarTripulante(t);
        assertEquals(1, n.getTripulacion().size());
        assertSame(t, n.getTripulacion().get(0));
    }

    @Test
    void noSeAceptaUnTripulanteNulo() {
        Nave n = new NaveExploradora("EXP-1", new MotorWarp());
        assertThrows(IllegalArgumentException.class, () -> n.agregarTripulante(null));
        assertTrue(n.getTripulacion().isEmpty());
    }

    @Test
    void laTripulacionExpuestaEsDeSoloLectura() {
        Nave n = new NaveExploradora("EXP-1", new MotorWarp());
        Tripulante t = new Tripulante("T-2", Cargo.TENIENTE, Origen.MARCIANO, 1);
        assertThrows(UnsupportedOperationException.class, () -> n.getTripulacion().add(t));
        assertTrue(n.getTripulacion().isEmpty());
    }

    @Test
    void escenarioDCargaQueExcedeLaCapacidadConservaElEstadoDeLaNave() {
        Nave n = new NaveCarguero("CAR-1", new MotorWarp());
        assertThrows(RecursoFueraDeRangoException.class, () -> n.getRecursos().cargarCombustible(1));
        assertEquals(100, n.getRecursos().getCombustible());
        assertEquals(60, n.getRecursos().getEnergia());
        assertEquals(0, n.getRecursos().getDesgaste());
    }

    @Test
    void mantenimientoDeUnaNaveDesgastada() {
        Nave n = new NaveExploradora("EXP-1", new MotorWarp());
        n.getRecursos().desgastar(85);
        assertTrue(n.getRecursos().requiereMantenimiento());
        n.getRecursos().realizarMantenimiento();
        assertEquals(0, n.getRecursos().getDesgaste());
        assertNotNull(n.getRecursos());
    }

    // ---------------------------------------------------------------- Parte 2: Posición, Tripulación mínima, Destrucción y Double Dispatch

    @Test
    void posicionYBaseDeOrigenSeActualizanYRechazanNulos() {
        Nave n = new NaveExploradora("EXP-1", new MotorWarp());
        n.setBaseDeOrigen(new Coordenada(2, 3));
        assertEquals(new Coordenada(2, 3), n.getBaseDeOrigen());
        assertEquals(new Coordenada(2, 3), n.getPosicion());
        n.setPosicion(new Coordenada(5, 7));
        assertEquals(new Coordenada(5, 7), n.getPosicion());
        assertEquals(new Coordenada(2, 3), n.getBaseDeOrigen());
        assertThrows(IllegalArgumentException.class, () -> n.setPosicion(null));
        assertThrows(IllegalArgumentException.class, () -> n.setBaseDeOrigen(null));
    }

    @Test
    void tripulacionValidaExigeCapitanYCuatroAdicionalesVivos() {
        Nave n = new NaveExploradora("EXP-1", new MotorWarp());
        for (int i = 1; i <= 5; i++) {
            n.agregarTripulante(new Tripulante("T-" + i, Cargo.ALFEREZ, Origen.TERRICOLA, 1));
        }
        assertFalse(n.tieneTripulacionValida(), "sin capitan no es valida");

        Nave conCapitan = new NaveExploradora("EXP-2", new MotorWarp());
        conCapitan.agregarTripulante(new Tripulante("Kirk", Cargo.CAPITAN, Origen.TERRICOLA, 10));
        conCapitan.agregarTripulante(new Tripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, 4));
        conCapitan.agregarTripulante(new Tripulante("Uhura", Cargo.TENIENTE, Origen.TERRICOLA, 3));
        conCapitan.agregarTripulante(new Tripulante("Sulu", Cargo.TENIENTE, Origen.TERRICOLA, 2));
        assertFalse(conCapitan.tieneTripulacionValida(), "con 4 tripulantes no alcanza");

        conCapitan.agregarTripulante(new Tripulante("Chekov", Cargo.ALFEREZ, Origen.MARCIANO, 1));
        assertTrue(conCapitan.tieneTripulacionValida(), "1 capitan + 4 adicionales es valida");

        conCapitan.destruir();
        assertTrue(conCapitan.estaDestruida());
        assertFalse(conCapitan.tieneTripulacionValida());
        for (Tripulante t : conCapitan.getTripulacion()) {
            assertFalse(t.estaVivo(), "al destruirse la nave muere toda la tripulacion");
        }
    }

    @Test
    void doubleDispatchResuelveLas9CombinacionesSinCondicionales() {
        // Exploradora propia
        assertEquals(ResultadoEncuentro.PACTO,
                new NaveExploradora("P", new MotorWarp()).enfrentarseCon(new NaveExploradora("E", new MotorWarp())));
        assertEquals(ResultadoEncuentro.GANA,
                new NaveExploradora("P", new MotorWarp()).enfrentarseCon(new NaveCarguero("E", new MotorWarp())));
        Nave expPierde = new NaveExploradora("P", new MotorWarp());
        Tripulante tExp = new Tripulante("Kirk", Cargo.CAPITAN, Origen.TERRICOLA, 5);
        expPierde.agregarTripulante(tExp);
        assertEquals(ResultadoEncuentro.PIERDE,
                expPierde.enfrentarseCon(new NaveCombate("E", new MotorWarp())));
        assertTrue(expPierde.estaDestruida());
        assertFalse(tExp.estaVivo());

        // Carguero propio
        Nave carPierde = new NaveCarguero("P", new MotorWarp());
        assertEquals(ResultadoEncuentro.PIERDE,
                carPierde.enfrentarseCon(new NaveExploradora("E", new MotorWarp())));
        assertTrue(carPierde.estaDestruida());
        assertEquals(ResultadoEncuentro.PACTO,
                new NaveCarguero("P", new MotorWarp()).enfrentarseCon(new NaveCarguero("E", new MotorWarp())));
        assertEquals(ResultadoEncuentro.GANA,
                new NaveCarguero("P", new MotorWarp()).enfrentarseCon(new NaveCombate("E", new MotorWarp())));

        // Combate propio
        assertEquals(ResultadoEncuentro.GANA,
                new NaveCombate("P", new MotorWarp()).enfrentarseCon(new NaveExploradora("E", new MotorWarp())));
        Nave comPierde = new NaveCombate("P", new MotorWarp());
        assertEquals(ResultadoEncuentro.PIERDE,
                comPierde.enfrentarseCon(new NaveCarguero("E", new MotorWarp())));
        assertTrue(comPierde.estaDestruida());
        assertEquals(ResultadoEncuentro.PACTO,
                new NaveCombate("P", new MotorWarp()).enfrentarseCon(new NaveCombate("E", new MotorWarp())));
    }
}
