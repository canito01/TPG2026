package modelo.asistente;

import java.util.List;

import modelo.elementos.Asteroide;
import modelo.fabrica.NaveFactory;
import modelo.motor.ParametrosDelMotor;
import modelo.nave.Nave;
import modelo.nave.TipoNave;
import modelo.navegacion.Coordenada;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.TableroStub;
import modelo.tripulacion.Cargo;
import modelo.tripulacion.Origen;
import modelo.tripulacion.Tripulante;

/** Comprueba el contrato del asistente con una navegación de prueba. */
public final class VerificacionNavegacionAsistente {
    public static void main(String[] args) {
        verificarDelegacionYBitacora();
        verificarAsistenteSinNavegacion();
        verificarNaveYMotorConTablero();
        System.out.println("Navegación del asistente: delegación, resultados y bitácora verificados.");
    }

    private static void verificarDelegacionYBitacora() {
        NavegacionDePrueba navegacion = new NavegacionDePrueba();
        AsistenteDeComando asistente = new AsistenteBase(crearNave(), navegacion);
        PosicionNave siguiente = new PosicionNave(2, 3);
        comprobar(asistente.consultarPosicion().orElseThrow().equals(new PosicionNave(2, 2)),
                "La consulta devuelve la posición de la navegación.");
        comprobar(asistente.consultarEstado().equals("Motor de prueba"),
                "La consulta de estado usa el subsistema conectado.");
        comprobar(asistente.escanear(siguiente) == ResultadoEscaneo.LIBRE, "Escaneo de casilla libre.");
        comprobar(asistente.saltar(siguiente).estado() == ResultadoSalto.Estado.AVANZO,
                "Salto normal delegado.");
        comprobar(asistente.consultarPosicion().orElseThrow().equals(siguiente),
                "El asistente muestra la posición que cambió el subsistema.");

        PosicionNave obstaculo = new PosicionNave(2, 4);
        comprobar(asistente.escanear(obstaculo) == ResultadoEscaneo.OBSTACULO, "Escaneo de obstáculo.");
        for (int turno = 0; turno < 2; turno++) {
            comprobar(asistente.saltarWarp(obstaculo).estado() == ResultadoSalto.Estado.PREPARANDO,
                    "El turno de preparación se distingue de un salto ejecutado.");
            comprobar(asistente.consultarPosicion().orElseThrow().equals(siguiente),
                    "La preparación mantiene la posición.");
        }
        comprobar(asistente.saltarWarp(obstaculo).estado() == ResultadoSalto.Estado.AVANZO
                        && asistente.consultarPosicion().orElseThrow().equals(new PosicionNave(2, 5)),
                "El asistente informa el avance Warp que efectuó el subsistema.");
        comprobar(asistente.saltarWarp(new PosicionNave(2, 6)).estado() == ResultadoSalto.Estado.ENFRIANDO,
                "El enfriamiento consume un turno sin reportar avance.");

        PosicionNave fueraDelTablero = new PosicionNave(-1, 5);
        comprobar(asistente.escanear(fueraDelTablero) == ResultadoEscaneo.RECHAZADO,
                "Una excepción de dominio al escanear se convierte en rechazo.");
        ResultadoSalto saltoRechazado = asistente.saltar(fueraDelTablero);
        comprobar(saltoRechazado.estado() == ResultadoSalto.Estado.RECHAZADO
                        && saltoRechazado.mensaje().equals("Casilla fuera del tablero."),
                "El salto rechazado conserva el motivo.");

        List<String> avisos = asistente.consultarBitacora().stream().map(Evento::getDescripcion).toList();
        comprobar(avisos.size() == 9
                        && avisos.get(2).contains("OBSTACULO")
                        && avisos.get(3).contains("[PREPARANDO]")
                        && avisos.get(5).contains("[AVANZO]")
                        && avisos.get(6).contains("[ENFRIANDO]")
                        && avisos.get(7).contains("rechazado")
                        && avisos.get(8).contains("[RECHAZADO]"),
                "Cada acción de navegación deja un aviso con su resultado.");
    }

    private static void verificarAsistenteSinNavegacion() {
        AsistenteDeComando asistente = new AsistenteBase(crearNave());
        PosicionNave siguiente = new PosicionNave(2, 3);
        comprobar(asistente.consultarPosicion().isEmpty(), "La Entrega 1 carece de posición de tablero.");
        comprobar(asistente.escanear(siguiente) == ResultadoEscaneo.RECHAZADO
                        && asistente.saltar(siguiente).estado() == ResultadoSalto.Estado.RECHAZADO
                        && asistente.saltarWarp(siguiente).estado() == ResultadoSalto.Estado.RECHAZADO,
                "Las operaciones nuevas explican cuando falta configurar la navegación.");
        comprobar(asistente.consultarBitacora().size() == 3, "Los rechazos quedan registrados.");
        comprobar(asistente.prepararSalto().esExito() && asistente.saltar().esExito(),
                "El salto sin casilla conserva el comportamiento de la Entrega 1.");
    }

    private static Nave crearNave() {
        return new NaveFactory().crear(TipoNave.EXPLORADORA, "EXP-NAV");
    }

    private static void verificarNaveYMotorConTablero() {
        Nave nave = crearNave();
        nave.setBaseDeOrigen(new Coordenada(1, 2));
        Tripulante capitan = new Tripulante("Kirk", Cargo.CAPITAN, Origen.TERRICOLA, 5);
        nave.agregarTripulante(capitan);
        TableroStub tablero = new TableroStub(0L);
        AsistenteDeComando asistente = AsistenteBase.conTablero(nave, tablero);
        PosicionNave obstaculo = new PosicionNave(2, 2);
        PosicionNave despuesDelWarp = new PosicionNave(2, 3);
        PosicionNave destino = new PosicionNave(2, 4);
        tablero.colocar(ElementoDePrueba.representar(new Asteroide()), new Coordenada(2, 2));
        tablero.colocar(ElementoDePrueba.representar(new Asteroide(100)), new Coordenada(4, 2));
        comprobar(asistente.calcularRuta(destino)
                .equals(List.of(new PosicionNave(2, 1), obstaculo, despuesDelWarp, destino)),
                "El radar usa la posición real y convierte fila y columna al tablero.");
        comprobar(asistente.escanear(obstaculo) == ResultadoEscaneo.OBSTACULO,
                "El radar detecta un elemento real en la ruta.");
        ResumenRecursos antes = asistente.consultarRecursos();
        ResultadoSalto rechazado = asistente.saltar(destino);
        ResumenRecursos despuesDelRechazo = asistente.consultarRecursos();
        comprobar(rechazado.estado() == ResultadoSalto.Estado.RECHAZADO
                        && antes.getCombustible() == despuesDelRechazo.getCombustible()
                        && antes.getEnergia() == despuesDelRechazo.getEnergia()
                        && antes.getDesgaste() == despuesDelRechazo.getDesgaste(),
                "Una casilla que no sigue en la ruta no consume recursos.");
        comprobar(asistente.saltarWarp(obstaculo).estado() == ResultadoSalto.Estado.PREPARANDO
                        && asistente.saltarWarp(obstaculo).estado() == ResultadoSalto.Estado.PREPARANDO,
                "El motor cumple los dos turnos de preparación.");
        comprobar(asistente.saltarWarp(obstaculo).estado() == ResultadoSalto.Estado.AVANZO
                        && asistente.consultarPosicion().orElseThrow().equals(despuesDelWarp)
                        && tablero.getPosicionNave().equals(nave.getPosicion())
                        && nave.getRecursos().getEnergia() == antes.getEnergia() - ParametrosDelMotor.ENERGIA_WARP,
                "El Warp mueve la nave real y consume su energía.");
        comprobar(asistente.saltar(destino).estado() == ResultadoSalto.Estado.ENFRIANDO
                        && nave.getRecursos().getDesgaste() == ParametrosDelMotor.DESGASTE_ENFRIAMIENTO,
                "El enfriamiento aplica el desgaste sobre los recursos reales.");
        comprobar(asistente.saltar(destino).estado() == ResultadoSalto.Estado.AVANZO
                        && nave.getRecursos().getCombustible()
                        == antes.getCombustible() - ParametrosDelMotor.COMBUSTIBLE_SALTO_SIMPLE,
                "El salto normal descuenta combustible de la nave real.");
        comprobar(asistente.interactuarConElementoActual().contains("Asteroide")
                        && asistente.estaDestruida() && !capitan.estaVivo()
                        && asistente.consultarBitacora().stream()
                                .anyMatch(evento -> evento.getDescripcion().contains("Se perdió la tripulación")),
                "El encuentro se delega y el asistente registra la destrucción.");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static final class NavegacionDePrueba implements NavegacionDeMision {
        private PosicionNave posicion = new PosicionNave(2, 2);
        private int turnosWarp;

        @Override
        public PosicionNave consultarPosicion() {
            return posicion;
        }

        @Override
        public String consultarEstado() {
            return "Motor de prueba";
        }

        @Override
        public ResultadoEscaneo escanear(PosicionNave casilla) {
            comprobarCasilla(casilla);
            if (casilla.equals(new PosicionNave(2, 4))) {
                return ResultadoEscaneo.OBSTACULO;
            }
            return ResultadoEscaneo.LIBRE;
        }

        @Override
        public ResultadoSalto saltar(PosicionNave siguiente) {
            comprobarCasilla(siguiente);
            posicion = siguiente;
            return new ResultadoSalto(ResultadoSalto.Estado.AVANZO, "Avanzó una casilla.");
        }

        @Override
        public ResultadoSalto saltarWarp(PosicionNave siguiente) {
            turnosWarp++;
            if (turnosWarp <= 2) {
                return new ResultadoSalto(ResultadoSalto.Estado.PREPARANDO, "Preparación en curso.");
            }
            if (turnosWarp == 3) {
                posicion = new PosicionNave(siguiente.fila(), siguiente.columna() + 1);
                return new ResultadoSalto(ResultadoSalto.Estado.AVANZO, "Avanzó dos casillas.");
            }
            return new ResultadoSalto(ResultadoSalto.Estado.ENFRIANDO, "Enfriamiento.");
        }

        private static void comprobarCasilla(PosicionNave casilla) {
            if (casilla.fila() < 0 || casilla.columna() < 0) {
                throw new ExcepcionDominio("Casilla fuera del tablero.");
            }
        }
    }
}
