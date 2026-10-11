package modelo.fabrica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import modelo.nave.Nave;
import modelo.nave.NaveCarguero;
import modelo.nave.NaveCombate;
import modelo.nave.NaveExploradora;
import modelo.nave.TipoNave;

class NaveFactoryTest {

    private final NaveFactory fabrica = new NaveFactory();

    @Test
    void creaExploradoraConSuConfiguracionInicial() {
        Nave n = fabrica.crear(TipoNave.EXPLORADORA, "EXP-1");
        assertInstanceOf(NaveExploradora.class, n);
        assertEquals(TipoNave.EXPLORADORA, n.getTipo());
        assertEquals("EXP-1", n.getId());
        assertEquals(60, n.getRecursos().getCombustible());
        assertEquals(80, n.getRecursos().getEnergia());
        assertEquals(0, n.getRecursos().getDesgaste());
    }

    @Test
    void creaCargueroConSuConfiguracionInicial() {
        Nave n = fabrica.crear(TipoNave.CARGUERO, "CAR-1");
        assertInstanceOf(NaveCarguero.class, n);
        assertEquals(TipoNave.CARGUERO, n.getTipo());
        assertEquals(100, n.getRecursos().getCombustible());
        assertEquals(60, n.getRecursos().getEnergia());
        assertEquals(0, n.getRecursos().getDesgaste());
    }

    @Test
    void creaNaveDeCombateConSuConfiguracionInicial() {
        Nave n = fabrica.crear(TipoNave.COMBATE, "COM-1");
        assertInstanceOf(NaveCombate.class, n);
        assertEquals(TipoNave.COMBATE, n.getTipo());
        assertEquals(80, n.getRecursos().getCombustible());
        assertEquals(100, n.getRecursos().getEnergia());
        assertEquals(0, n.getRecursos().getDesgaste());
    }

    @Test
    void sePuedeCrearCadaTipoSoloConLaAbstraccionComun() {
        for (TipoNave tipo : TipoNave.values()) {
            Nave n = fabrica.crear(tipo, "N-" + tipo);
            assertEquals(tipo, n.getTipo());
            assertEquals(0, n.getRecursos().getDesgaste());
            assertEquals(0, n.getTripulacion().size());
        }
    }

    @Test
    void cadaNaveCreadaEsIndependiente() {
        Nave a = fabrica.crear(TipoNave.EXPLORADORA, "A");
        Nave b = fabrica.crear(TipoNave.EXPLORADORA, "B");
        assertNotSame(a, b);
        assertNotSame(a.getRecursos(), b.getRecursos());
        assertNotSame(a.getMotor(), b.getMotor());

        a.getRecursos().consumir(10, 10);
        assertEquals(60, b.getRecursos().getCombustible()); // b no se vio afectada
    }

    @Test
    void rechazaTipoNulo() {
        assertThrows(IllegalArgumentException.class, () -> fabrica.crear(null, "X"));
    }

    @Test
    void rechazaIdInvalido() {
        assertThrows(IllegalArgumentException.class, () -> fabrica.crear(TipoNave.COMBATE, null));
        assertThrows(IllegalArgumentException.class, () -> fabrica.crear(TipoNave.COMBATE, " "));
    }
}
