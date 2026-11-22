package modelo.nave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RecursosTest {

    /** Verifica que el estado sea exactamente el esperado (combustible, energía, desgaste). */
    private static void assertEstado(Recursos r, int combustible, int energia, int desgaste) {
        assertEquals(combustible, r.getCombustible(), "combustible");
        assertEquals(energia, r.getEnergia(), "energía");
        assertEquals(desgaste, r.getDesgaste(), "desgaste");
    }

    // ---------------------------------------------------------------- constructor

    @Test
    void constructorConValoresValidosConservaLosValores() {
        Recursos r = new Recursos(60, 80, 0);
        assertEstado(r, 60, 80, 0);
        assertEquals(0, r.getGemas());
    }

    @Test
    void constructorAceptaLosLimitesExactos() {
        assertEstado(new Recursos(0, 0, 0), 0, 0, 0);
        assertEstado(new Recursos(100, 100, 100), 100, 100, 100);
    }

    @Test
    void constructorRechazaValoresFueraDeRango() {
        assertThrows(RecursoFueraDeRangoException.class, () -> new Recursos(-1, 50, 0));
        assertThrows(RecursoFueraDeRangoException.class, () -> new Recursos(101, 50, 0));
        assertThrows(RecursoFueraDeRangoException.class, () -> new Recursos(50, -1, 0));
        assertThrows(RecursoFueraDeRangoException.class, () -> new Recursos(50, 101, 0));
        assertThrows(RecursoFueraDeRangoException.class, () -> new Recursos(50, 50, -1));
        assertThrows(RecursoFueraDeRangoException.class, () -> new Recursos(50, 50, 101));
        assertThrows(IllegalArgumentException.class, () -> new Recursos(50, 50, 0, -1));
    }

    // ---------------------------------------------------------------- cargas estrictas (E1)

    @Test
    void cargarCombustibleSumaLaCantidad() {
        Recursos r = new Recursos(60, 80, 10);
        r.cargarCombustible(25);
        assertEstado(r, 85, 80, 10);
    }

    @Test
    void cargarCombustibleHastaLaCapacidadExactaEsValido() {
        Recursos r = new Recursos(60, 80, 0);
        r.cargarCombustible(40);
        assertEquals(100, r.getCombustible());
    }

    @Test
    void cargarCombustibleQueExcedeLaCapacidadSeRechazaYConservaElEstado() {
        Recursos r = new Recursos(60, 80, 15);
        assertThrows(RecursoFueraDeRangoException.class, () -> r.cargarCombustible(41));
        assertEstado(r, 60, 80, 15);
    }

    @Test
    void cargarEnergiaSumaLaCantidad() {
        Recursos r = new Recursos(60, 80, 10);
        r.cargarEnergia(20);
        assertEstado(r, 60, 100, 10);
    }

    @Test
    void cargarEnergiaQueExcedeLaCapacidadSeRechazaYConservaElEstado() {
        Recursos r = new Recursos(60, 80, 15);
        assertThrows(RecursoFueraDeRangoException.class, () -> r.cargarEnergia(21));
        assertEstado(r, 60, 80, 15);
    }

    @Test
    void cargarConCantidadNoPositivaSeRechazaYConservaElEstado() {
        Recursos r = new Recursos(60, 80, 15);
        assertThrows(IllegalArgumentException.class, () -> r.cargarCombustible(0));
        assertThrows(IllegalArgumentException.class, () -> r.cargarCombustible(-5));
        assertThrows(IllegalArgumentException.class, () -> r.cargarEnergia(0));
        assertThrows(IllegalArgumentException.class, () -> r.cargarEnergia(-5));
        assertEstado(r, 60, 80, 15);
    }

    @Test
    void cantidadEnormeNoDesbordaYSeRechaza() {
        Recursos r = new Recursos(60, 80, 0);
        assertThrows(RecursoFueraDeRangoException.class, () -> r.cargarCombustible(Integer.MAX_VALUE));
        assertEstado(r, 60, 80, 0);
    }

    // ---------------------------------------------------------------- consumir

    @Test
    void consumirDescuentaCombustibleYEnergia() {
        Recursos r = new Recursos(60, 80, 10);
        r.consumir(4, 5);
        assertEstado(r, 56, 75, 10);
    }

    @Test
    void consumirPermiteLlegarExactamenteACero() {
        Recursos r = new Recursos(4, 5, 0);
        r.consumir(4, 5);
        assertEstado(r, 0, 0, 0);
    }

    @Test
    void consumirEsAtomicoSiAlcanzaElCombustiblePeroNoLaEnergia() {
        Recursos r = new Recursos(50, 3, 0);
        assertThrows(RecursoFueraDeRangoException.class, () -> r.consumir(10, 5));
        assertEstado(r, 50, 3, 0);
    }

    @Test
    void consumirEsAtomicoSiAlcanzaLaEnergiaPeroNoElCombustible() {
        Recursos r = new Recursos(3, 50, 0);
        assertThrows(RecursoFueraDeRangoException.class, () -> r.consumir(5, 10));
        assertEstado(r, 3, 50, 0);
    }

    @Test
    void consumirConCantidadNegativaSeRechaza() {
        Recursos r = new Recursos(50, 50, 0);
        assertThrows(IllegalArgumentException.class, () -> r.consumir(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> r.consumir(0, -1));
        assertEstado(r, 50, 50, 0);
    }

    @Test
    void consumirCeroEsValidoYNoCambiaNada() {
        Recursos r = new Recursos(50, 50, 0);
        r.consumir(0, 0);
        assertEstado(r, 50, 50, 0);
    }

    // ---------------------------------------------------------------- desgaste y mantenimiento

    @Test
    void desgastarSumaDesgaste() {
        Recursos r = new Recursos(60, 80, 0);
        r.desgastar(4);
        assertEstado(r, 60, 80, 4);
    }

    @Test
    void desgastarQueExcedeElMaximoSeRechazaYConservaElEstado() {
        Recursos r = new Recursos(60, 80, 97);
        assertThrows(RecursoFueraDeRangoException.class, () -> r.desgastar(4));
        assertEstado(r, 60, 80, 97);
    }

    @Test
    void desgastarConCantidadNoPositivaSeRechaza() {
        Recursos r = new Recursos(60, 80, 10);
        assertThrows(IllegalArgumentException.class, () -> r.desgastar(0));
        assertThrows(IllegalArgumentException.class, () -> r.desgastar(-3));
        assertEstado(r, 60, 80, 10);
    }

    @Test
    void requiereMantenimientoDesdeDesgaste80Inclusive() {
        assertFalse(new Recursos(50, 50, 79).requiereMantenimiento());
        assertTrue(new Recursos(50, 50, 80).requiereMantenimiento());
        assertTrue(new Recursos(50, 50, 100).requiereMantenimiento());
        assertFalse(new Recursos(50, 50, 0).requiereMantenimiento());
    }

    @Test
    void desgastarPuedeHacerQueLaNaveRequieraMantenimiento() {
        Recursos r = new Recursos(60, 80, 76);
        assertFalse(r.requiereMantenimiento());
        r.desgastar(4);
        assertTrue(r.requiereMantenimiento());
    }

    @Test
    void realizarMantenimientoLlevaElDesgasteACeroYNoTocaLoDemas() {
        Recursos r = new Recursos(33, 44, 90);
        assertTrue(r.requiereMantenimiento());
        r.realizarMantenimiento();
        assertEstado(r, 33, 44, 0);
        assertFalse(r.requiereMantenimiento());
    }

    @Test
    void realizarMantenimientoConDesgasteCeroEsValido() {
        Recursos r = new Recursos(33, 44, 0);
        r.realizarMantenimiento();
        assertEstado(r, 33, 44, 0);
    }

    // ---------------------------------------------------------------- consultas "puede..."

    @Test
    void lasConsultasPuedeCoincidenConLasOperaciones() {
        Recursos r = new Recursos(60, 80, 97);
        assertTrue(r.puedeCargarCombustible(40));
        assertFalse(r.puedeCargarCombustible(41));
        assertFalse(r.puedeCargarCombustible(0));
        assertTrue(r.puedeCargarEnergia(20));
        assertFalse(r.puedeCargarEnergia(21));
        assertTrue(r.puedeConsumir(60, 80));
        assertFalse(r.puedeConsumir(61, 0));
        assertFalse(r.puedeConsumir(0, 81));
        assertFalse(r.puedeConsumir(-1, 0));
        assertTrue(r.puedeDesgastar(3));
        assertFalse(r.puedeDesgastar(4));
        assertFalse(r.puedeDesgastar(Integer.MAX_VALUE));
        assertEstado(r, 60, 80, 97);
    }

    // ---------------------------------------------------------------- copia

    @Test
    void copiaEsIndependienteDelOriginal() {
        Recursos original = new Recursos(60, 80, 10);
        original.recolectarGemas(30);
        Recursos copia = original.copia();
        original.consumir(10, 10);
        assertEstado(copia, 60, 80, 10);
        assertEquals(30, copia.getGemas());
        assertEstado(original, 50, 70, 10);
    }

    // ---------------------------------------------------------------- operaciones E2

    @Test
    void recolectarGemasAcumulaValoresPositivos() {
        Recursos r = new Recursos(60, 80, 0);
        r.recolectarGemas(25);
        r.recolectarGemas(40);
        assertEquals(65, r.getGemas());
        assertThrows(IllegalArgumentException.class, () -> r.recolectarGemas(0));
        assertThrows(IllegalArgumentException.class, () -> r.recolectarGemas(-5));
    }

    @Test
    void recargarHastaElTopeSaturaEn100SinLanzarExcepcion() {
        Recursos r = new Recursos(85, 90, 15);
        r.recargarHastaElTope(30, 25);
        assertEstado(r, 100, 100, 15);
        assertThrows(IllegalArgumentException.class, () -> r.recargarHastaElTope(-1, 5));
    }

    @Test
    void recargaTotalRestauraCombustibleYEnergiaAl100YDesgasteA0() {
        Recursos r = new Recursos(12, 34, 78);
        r.recargaTotal();
        assertEstado(r, 100, 100, 0);
    }

    @Test
    void entregarRecursosDescuentaDeFormaAtomica() {
        Recursos r = new Recursos(40, 50, 10);
        assertTrue(r.puedeEntregar(20, 30));
        r.entregarRecursos(20, 30);
        assertEstado(r, 20, 20, 10);
        assertFalse(r.puedeEntregar(25, 10));
        assertThrows(RecursoFueraDeRangoException.class, () -> r.entregarRecursos(25, 10));
        assertEstado(r, 20, 20, 10);
    }

    @Test
    void perderEnergiaHastaCeroSaturaEnCeroYMarcaDestruida() {
        Recursos r = new Recursos(60, 15, 0);
        assertFalse(r.estaDestruida());
        r.perderEnergiaHastaCero(20);
        assertEquals(0, r.getEnergia());
        assertTrue(r.estaDestruida());
    }

    @Test
    void estaDestruidaPorDesgaste100Energia0OCombustible0() {
        assertFalse(new Recursos(1, 1, 99).estaDestruida());
        assertTrue(new Recursos(1, 1, 100).estaDestruida());
        assertTrue(new Recursos(1, 0, 50).estaDestruida());
        assertTrue(new Recursos(0, 1, 50).estaDestruida());
    }
}
