package modelo.asistente;

import java.util.List;
import java.util.Objects;

import modelo.motor.ResultadoDeTurno;
import modelo.motor.SistemasDeLaNave;
import modelo.nave.Nave;
import modelo.nave.Recursos;
import modelo.navegacion.Coordenada;
import modelo.navegacion.RadarCortoAlcance;
import modelo.navegacion.RadarLargoAlcance;
import modelo.navegacion.Ruta;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.Tablero;

/** Adapta los radares y el motor a la nave operada por el asistente. */
final class NavegacionConTablero implements NavegacionDeMision, SistemasDeLaNave {
    private final Nave nave;
    private final Tablero tablero;
    private final RadarCortoAlcance radarCorto;
    private final RadarLargoAlcance radarLargo;
    private String estado = "Disponible";

    NavegacionConTablero(Nave nave, Tablero tablero) {
        this.nave = Objects.requireNonNull(nave);
        this.tablero = Objects.requireNonNull(tablero);
        this.radarCorto = new RadarCortoAlcance(tablero);
        this.radarLargo = new RadarLargoAlcance(tablero);
        tablero.moverNave(nave.getPosicion());
        nave.conectarMotor(this);
    }

    @Override
    public PosicionNave consultarPosicion() {
        return PosicionNave.desde(nave.getPosicion());
    }

    @Override
    public String consultarEstado() {
        return estado;
    }

    @Override
    public List<PosicionNave> calcularRuta(PosicionNave destino) {
        Coordenada objetivo = destino.aCoordenada();
        Coordenada origen = nave.getPosicion();
        if (!tablero.esValida(origen) || !tablero.esValida(objetivo)) {
            throw new ExcepcionDominio("Origen o destino fuera del tablero.");
        }
        try {
            Ruta ruta = radarLargo.calcularRuta(origen, objetivo);
            tablero.marcarDestino(objetivo);
            return ruta.getCamino().stream().map(PosicionNave::desde).toList();
        } catch (IllegalStateException e) {
            throw new ExcepcionDominio(e.getMessage(), e);
        }
    }

    @Override
    public ResultadoEscaneo escanear(PosicionNave casilla) {
        Coordenada coordenada = casilla.aCoordenada();
        if (!tablero.esValida(coordenada)) {
            throw new ExcepcionDominio("Casilla fuera del tablero.");
        }
        return radarCorto.escanear(coordenada) == null ? ResultadoEscaneo.LIBRE : ResultadoEscaneo.OBSTACULO;
    }

    @Override
    public ResultadoSalto saltar(PosicionNave siguiente) {
        return realizarTurno(siguiente, false);
    }

    @Override
    public ResultadoSalto saltarWarp(PosicionNave siguiente) {
        return realizarTurno(siguiente, true);
    }

    private ResultadoSalto realizarTurno(PosicionNave siguiente, boolean warp) {
        if (nave.estaDestruida()) {
            throw new ExcepcionDominio("La nave está destruida.");
        }
        Coordenada actual = nave.getPosicion();
        Ruta ruta = radarLargo.getRutaActual();
        if (ruta == null || !ruta.contiene(actual) || actual.equals(ruta.getDestino())
                || !ruta.siguienteDe(actual).equals(siguiente.aCoordenada())) {
            throw new ExcepcionDominio("El salto requiere la siguiente casilla de la ruta.");
        }
        ResultadoDeTurno turno = warp ? nave.getMotor().saltarWarp() : nave.getMotor().saltar();
        estado = turno.getEstadoActual();
        return interpretar(turno);
    }

    static ResultadoSalto interpretar(ResultadoDeTurno turno) {
        ResultadoSalto.Estado resultado = ResultadoSalto.Estado.RECHAZADO;
        if (turno.avanzo()) {
            resultado = ResultadoSalto.Estado.AVANZO;
        } else if ("Enfriamiento".equals(turno.getEstadoAnterior())) {
            resultado = ResultadoSalto.Estado.ENFRIANDO;
        } else if ("Preparando salto".equals(turno.getEstadoAnterior())
                || "Preparando salto".equals(turno.getEstadoActual())) {
            resultado = ResultadoSalto.Estado.PREPARANDO;
        }
        return new ResultadoSalto(resultado, turno.getDescripcion());
    }

    @Override
    public String interactuarConElementoActual() {
        ElementoDePrueba elemento = tablero.getElementoEn(nave.getPosicion());
        return elemento == null ? null : elemento.interactuarCon(nave);
    }

    @Override
    public void avanzarTurnoTablero() {
        tablero.moverElementos();
    }

    @Override
    public int getCombustible() { return nave.getRecursos().getCombustible(); }

    @Override
    public int getEnergia() { return nave.getRecursos().getEnergia(); }

    @Override
    public void consumirCombustible(int cantidad) { nave.getRecursos().consumir(cantidad, 0); }

    @Override
    public void consumirEnergia(int cantidad) { nave.getRecursos().consumir(0, cantidad); }

    @Override
    public void agregarDesgaste(int cantidad) {
        Recursos recursos = nave.getRecursos();
        recursos.consumir(0, 0, Math.min(cantidad, Recursos.MAXIMO - recursos.getDesgaste()));
    }

    @Override
    public int avanzar(int casillas) {
        Ruta ruta = radarLargo.getRutaActual();
        Coordenada actual = nave.getPosicion();
        Coordenada nueva = ruta.avanzarDesde(actual, casillas);
        nave.setPosicion(nueva);
        tablero.moverNave(nueva);
        return ruta.indiceDe(nueva) - ruta.indiceDe(actual);
    }
}
