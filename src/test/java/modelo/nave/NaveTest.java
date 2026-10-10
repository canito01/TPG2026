package modelo.nave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import modelo.motor.MotorWarp;
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
        Tripulante t = new Tripulante();
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
        assertThrows(UnsupportedOperationException.class, () -> n.getTripulacion().add(new Tripulante()));
        assertTrue(n.getTripulacion().isEmpty());
    }

    @Test
    void escenarioDCargaQueExcedeLaCapacidadConservaElEstadoDeLaNave() {
        Nave n = new NaveCarguero("CAR-1", new MotorWarp()); // combustible 100
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
}
