package modelo.elementos;

import javax.swing.SwingUtilities;

import controlador.CreacionController;
import modelo.fabrica.NaveFactory;
import modelo.nave.Nave;
import modelo.nave.RecursoFueraDeRangoException;
import modelo.nave.Recursos;
import modelo.nave.TipoNave;
import modelo.navegacion.Coordenada;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.Origen;
import modelo.tripulacion.Tripulante;
import vista.CreacionPanel;

/**
 * Verificación reproducible de la Parte 2 de Eneas:
 *  - V8: Las 9 combinaciones de encuentros entre naves con Double Dispatch y destrucción al perder.
 *  - V9: Impacto con Asteroide (energía hasta 0 deja la nave inoperativa) y Estación de Carga (monto fijo y porcentaje con tope 100).
 *  - Recursos ampliados: gemas, recarga hasta el tope, recarga total, entrega de recursos y condiciones de destrucción.
 *  - Sección de Creación (MVC): validaciones de formato en la Vista y reglas de dominio en el Controlador.
 */
public final class VerificacionElementosYCreacion {

    public static void main(String[] args) throws Exception {
        verificarRecursosAmpliados();
        verificarLas9CombinacionesDoubleDispatch();
        verificarAsteroideYDestruccionPorEnergiaCero();
        verificarEstacionDeCargaMontoFijoYPorcentaje();
        verificarCreacionMVC();
        System.out.println("Verificación de Eneas (Recursos E2, Double Dispatch, Elementos y Creación MVC): todos los casos pasaron.");
    }

    private static void verificarRecursosAmpliados() {
        NaveFactory fabrica = new NaveFactory();
        Nave exploradora = fabrica.crear(TipoNave.EXPLORADORA, "EXP-R");
        Recursos r = exploradora.getRecursos();

        // Gemas
        comprobar(r.getGemas() == 0, "Las gemas comienzan en 0");
        r.recolectarGemas(45);
        comprobar(r.getGemas() == 45, "Se acumulan las gemas recolectadas");

        // Recarga hasta el tope (sin fallar al exceder 100)
        r.recargarHastaElTope(80, 50);
        comprobar(r.getCombustible() == 100 && r.getEnergia() == 100, "Recargar hasta el tope satura en 100");

        // Cargar estricto de E1 sigue rechazando excesos
        try {
            r.cargarCombustible(1);
            throw new AssertionError("cargarCombustible estricto debió rechazar exceso sobre 100");
        } catch (RecursoFueraDeRangoException esperado) {
            comprobar(r.getCombustible() == 100, "Se conserva el estado ante rechazo estricto");
        }

        // Entrega de recursos (M-01)
        comprobar(r.puedeEntregar(30, 40), "Puede entregar 30 de combustible y 40 de energía");
        r.entregarRecursos(30, 40);
        comprobar(r.getCombustible() == 70 && r.getEnergia() == 60, "Entrega descuenta recursos");

        // Recarga total (M-03)
        r.desgastar(50);
        r.recargaTotal();
        comprobar(r.getCombustible() == 100 && r.getEnergia() == 100 && r.getDesgaste() == 0,
                "Recarga total deja combustible=100, energia=100, desgaste=0");

        // Posición y base de origen
        exploradora.setBaseDeOrigen(new Coordenada(3, 4));
        exploradora.setPosicion(new Coordenada(8, 9));
        comprobar(exploradora.getBaseDeOrigen().equals(new Coordenada(3, 4))
                && exploradora.getPosicion().equals(new Coordenada(8, 9)),
                "La nave conserva su base de origen y su posición actual");
    }

    private static void verificarLas9CombinacionesDoubleDispatch() {
        NaveFactory fabrica = new NaveFactory();
        TipoNave[] tipos = {TipoNave.EXPLORADORA, TipoNave.CARGUERO, TipoNave.COMBATE};
        ResultadoEncuentro[][] tablaEsperada = {
                // vs Exploradora,        vs Carguero,              vs Combate
                {ResultadoEncuentro.PACTO, ResultadoEncuentro.GANA,   ResultadoEncuentro.PIERDE}, // Exploradora
                {ResultadoEncuentro.PIERDE, ResultadoEncuentro.PACTO, ResultadoEncuentro.GANA},   // Carguero
                {ResultadoEncuentro.GANA,   ResultadoEncuentro.PIERDE, ResultadoEncuentro.PACTO}  // Combate
        };

        for (int i = 0; i < tipos.length; i++) {
            for (int j = 0; j < tipos.length; j++) {
                Nave propia = fabrica.crear(tipos[i], "PROPIA-" + i + "-" + j);
                Tripulante capitan = new Tripulante("Capitan", Cargo.CAPITAN, Origen.TERRICOLA, 5);
                Tripulante oficial = new Tripulante("Oficial", Cargo.TENIENTE, Origen.VULCANO, 2);
                propia.agregarTripulante(capitan);
                propia.agregarTripulante(oficial);

                NaveEnemiga enemiga = fabrica.crearNaveEnemiga(tipos[j], "ENEMIGA-" + i + "-" + j);
                String detalle = enemiga.interactuarCon(propia);
                ResultadoEncuentro obtenido = enemiga.getUltimoResultado();
                ResultadoEncuentro esperado = tablaEsperada[i][j];

                comprobar(obtenido == esperado,
                        "Encuentro " + tipos[i] + " vs " + tipos[j] + " esperado " + esperado + " pero fue " + obtenido);
                comprobar(detalle.contains(esperado.name()), "El mensaje del encuentro describe el resultado");

                if (esperado == ResultadoEncuentro.PIERDE) {
                    comprobar(propia.estaDestruida(), "Al perder, la nave propia queda destruida");
                    comprobar(!capitan.estaVivo() && !oficial.estaVivo(),
                            "Al perder, toda la tripulación de la nave propia muere");
                } else {
                    comprobar(!propia.estaDestruida(), "Si gana o pacta, la nave propia sigue operativa");
                    comprobar(capitan.estaVivo() && oficial.estaVivo(),
                            "Si gana o pacta, la tripulación sigue con vida");
                }

                if (esperado == ResultadoEncuentro.GANA) {
                    comprobar(enemiga.getNave().estaDestruida(), "Al ganar, la nave enemiga queda destruida");
                }
            }
        }
    }

    private static void verificarAsteroideYDestruccionPorEnergiaCero() {
        NaveFactory fabrica = new NaveFactory();
        Nave nave = fabrica.crear(TipoNave.CARGUERO, "CAR-AST"); // energía inicial 60
        Tripulante capitan = new Tripulante("Kirk", Cargo.CAPITAN, Origen.TERRICOLA, 10);
        nave.agregarTripulante(capitan);

        Asteroide asteroide = new Asteroide(25);
        asteroide.interactuarCon(nave);
        comprobar(nave.getRecursos().getEnergia() == 35 && !nave.estaDestruida() && capitan.estaVivo(),
                "Primer impacto descuenta 25 de energía y la nave sigue operativa");

        asteroide.interactuarCon(nave);
        comprobar(nave.getRecursos().getEnergia() == 10 && !nave.estaDestruida(),
                "Segundo impacto deja la energía en 10");

        asteroide.interactuarCon(nave);
        comprobar(nave.getRecursos().getEnergia() == 0, "Tercer impacto agota la energía en 0");
        comprobar(nave.estaDestruida() && !capitan.estaVivo(),
                "Con energía en 0 la nave queda destruida/inoperativa y su tripulación muere");
    }

    private static void verificarEstacionDeCargaMontoFijoYPorcentaje() {
        NaveFactory fabrica = new NaveFactory();
        Nave nave = fabrica.crear(TipoNave.EXPLORADORA, "EXP-EST"); // 60 combustible, 80 energía
        nave.getRecursos().consumir(20, 40); // queda en 40 combustible, 40 energía

        // Por porcentaje (+50% de 40/40 -> +20/+20 -> 60/60)
        EstacionDeCarga porPorcentaje = EstacionDeCarga.porPorcentaje(50);
        porPorcentaje.interactuarCon(nave);
        comprobar(nave.getRecursos().getCombustible() == 60 && nave.getRecursos().getEnergia() == 60,
                "Estación por porcentaje recarga 50% de los recursos actuales");

        // Por monto fijo (+50/+50 sobre 60/60 -> satura en 100/100)
        EstacionDeCarga porMonto = EstacionDeCarga.porMontoFijo(50, 50);
        porMonto.interactuarCon(nave);
        comprobar(nave.getRecursos().getCombustible() == 100 && nave.getRecursos().getEnergia() == 100,
                "Estación por monto fijo recarga respetando el tope máximo de 100");
    }

    private static void verificarCreacionMVC() throws Exception {
        CreacionPanel vista = new CreacionPanel();
        NaveFactory fabrica = new NaveFactory();
        CreacionController controlador = new CreacionController(vista, fabrica);

        // 1. Intentar agregar tripulante sin nave creada -> regla de negocio rechaza
        vista.completarFormularioTripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, "4", "3");
        vista.clickAgregarTripulante();
        esperarEDT();
        comprobar(controlador.getNaveActual() == null && vista.getMensajeActual().contains("primero debe instanciar"),
                "El controlador impide asignar tripulantes si no se creó la nave");

        // 2. Validación de formato en la vista: ID de nave vacío
        vista.setTextoIdNave("   ");
        vista.clickCrearNave();
        esperarEDT();
        comprobar(controlador.getNaveActual() == null && vista.getMensajeActual().contains("Error de formato"),
                "La vista valida formato de ID vacío antes de llamar al controlador");

        // 3. Crear nave Exploradora mediante la vista y el controlador
        vista.seleccionarTipoNave(TipoNave.EXPLORADORA);
        vista.setTextoIdNave("EXP-MVC");
        vista.clickCrearNave();
        esperarEDT();
        comprobar(controlador.getNaveActual() != null
                        && controlador.getNaveActual().getTipo() == TipoNave.EXPLORADORA
                        && !controlador.puedeIniciarMision(),
                "La nave se crea con la Factory y aún no puede iniciar misión sin tripulación mínima");

        // 4. Validación de formato en la vista: antigüedad no numérica
        vista.completarFormularioTripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, "abc", "3");
        vista.clickAgregarTripulante();
        esperarEDT();
        comprobar(vista.getCantidadTripulantesEnTabla() == 0 && vista.getMensajeActual().contains("Error de formato"),
                "La vista rechaza antigüedad no numérica");

        // 5. Asignar 1 capitán + 4 tripulantes adicionales -> habilita misión
        vista.completarFormularioTripulante("James Kirk", Cargo.CAPITAN, Origen.TERRICOLA, "10", "0");
        vista.clickAgregarTripulante();
        vista.completarFormularioTripulante("Spock", Cargo.CONSEJERO, Origen.VULCANO, "4", "3");
        vista.clickAgregarTripulante();
        vista.completarFormularioTripulante("Uhura", Cargo.TENIENTE, Origen.TERRICOLA, "3", "0");
        vista.clickAgregarTripulante();
        vista.completarFormularioTripulante("Sulu", Cargo.TENIENTE, Origen.TERRICOLA, "2", "0");
        vista.clickAgregarTripulante();
        esperarEDT();
        comprobar(!controlador.puedeIniciarMision() && vista.getCantidadTripulantesEnTabla() == 4,
                "Con 4 tripulantes todavía no cumple el mínimo de 5");

        vista.completarFormularioTripulante("Chekov", Cargo.ALFEREZ, Origen.MARCIANO, "1", "0");
        vista.clickAgregarTripulante();
        esperarEDT();
        comprobar(controlador.puedeIniciarMision() && vista.getCantidadTripulantesEnTabla() == 5,
                "Con 1 capitán y 4 tripulantes adicionales la nave queda habilitada para iniciar misión");
    }

    private static void esperarEDT() throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(() -> { });
        }
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError("Fallo de verificación: " + mensaje);
        }
    }
}
