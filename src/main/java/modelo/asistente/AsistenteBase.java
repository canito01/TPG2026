package modelo.asistente;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import modelo.motor.ResultadoDeTurno;
import modelo.nave.Nave;
import modelo.nave.RecursoFueraDeRangoException;
import modelo.nave.Recursos;
import modelo.tablero.Tablero;
import util.observer.Observador;

/**
 * Implementacion concreta del asistente. Opera una sola nave.
 * Delega al subsistema que corresponda y captura las excepciones
 * de dominio para devolver un Resultado en vez de propagarlas.
 * Registra todo en la bitacora.
 */
public class AsistenteBase implements AsistenteDeComando {

    private final Nave nave;
    private final Bitacora bitacora;
    private final NavegacionDeMision navegacion;
    private String estadoDeDemostracion = "Disponible";
    private boolean destruccionRegistrada;

    public AsistenteBase(Nave nave) {
        this(nave, null);
    }

    public static AsistenteBase conTablero(Nave nave, Tablero tablero) {
        return new AsistenteBase(nave, new NavegacionConTablero(nave, tablero));
    }

    /** Recibe el subsistema de navegación. null permite las demostraciones de la Entrega 1. */
    public AsistenteBase(Nave nave, NavegacionDeMision navegacion) {
        if (nave == null) {
            throw new IllegalArgumentException("La nave no puede ser nula.");
        }
        this.nave = nave;
        this.bitacora = new Bitacora();
        this.navegacion = navegacion;
    }

    @Override
    public String getNaveId() {
        return nave.getId();
    }

    @Override
    public String consultarEstado() {
        if (navegacion != null) {
            return navegacion.consultarEstado();
        }
        return estadoDeDemostracion;
    }

    @Override
    public Optional<PosicionNave> consultarPosicion() {
        if (navegacion == null) {
            return Optional.empty();
        }
        return Optional.of(navegacion.consultarPosicion());
    }

    @Override
    public Optional<PosicionNave> consultarBase() {
        return Optional.of(PosicionNave.desde(nave.getBaseDeOrigen()));
    }

    @Override
    public List<PosicionNave> calcularRuta(PosicionNave destino) {
        validarCasilla(destino);
        if (navegacion == null) {
            registrarEvento("Ruta rechazada: la navegación no está configurada.");
            return List.of();
        }
        try {
            List<PosicionNave> ruta = navegacion.calcularRuta(destino);
            registrarEvento(ruta.isEmpty() ? "No hay ruta hacia " + describir(destino)
                    : "Ruta hacia " + describir(destino) + ": " + (ruta.size() - 1) + " pasos.");
            return ruta;
        } catch (ExcepcionDominio e) {
            registrarEvento("Ruta rechazada: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public String interactuarConElementoActual() {
        if (navegacion == null || estaDestruida()) {
            return null;
        }
        String evento = navegacion.interactuarConElementoActual();
        if (evento != null && !evento.isBlank()) {
            registrarEvento(evento);
        }
        estaDestruida();
        return evento;
    }

    @Override
    public void avanzarTurnoTablero() {
        if (navegacion != null) {
            navegacion.avanzarTurnoTablero();
        }
    }

    @Override
    public boolean estaDestruida() {
        boolean destruida = nave.estaDestruida();
        if (destruida && !destruccionRegistrada) {
            destruccionRegistrada = true;
            registrarEvento("Nave " + nave.getId() + " destruida. Se perdió la tripulación.");
        }
        return destruida;
    }

    @Override
    public void agregarGemas(int cantidad) {
        nave.getRecursos().recolectarGemas(cantidad);
        registrarEvento("Recompensa: " + cantidad + " gemas recolectadas.");
    }

    @Override
    public void reabastecerYRepararTotal() {
        nave.getRecursos().recargaTotal();
        registrarEvento("Recompensa: recarga total y reparación de la nave.");
    }

    @Override
    public ResultadoEscaneo escanear(PosicionNave casilla) {
        if (navegacion == null) {
            return AsistenteDeComando.super.escanear(casilla);
        }
        validarCasilla(casilla);
        ResultadoEscaneo resultado;
        try {
            resultado = Objects.requireNonNull(navegacion.escanear(casilla));
        } catch (ExcepcionDominio | RecursoFueraDeRangoException e) {
            registrarEvento("Escaneo de " + describir(casilla) + " rechazado. " + e.getMessage());
            return ResultadoEscaneo.RECHAZADO;
        }
        registrarEvento("Escaneo de " + describir(casilla) + ": " + resultado + ".");
        return resultado;
    }

    @Override
    public ResultadoSalto saltar(PosicionNave siguiente) {
        if (navegacion == null) {
            return AsistenteDeComando.super.saltar(siguiente);
        }
        return ejecutarSalto("Salto normal", siguiente, () -> navegacion.saltar(siguiente));
    }

    @Override
    public ResultadoSalto saltarWarp(PosicionNave siguiente) {
        if (navegacion == null) {
            return AsistenteDeComando.super.saltarWarp(siguiente);
        }
        return ejecutarSalto("Salto Warp", siguiente, () -> navegacion.saltarWarp(siguiente));
    }

    @Override
    public ResumenRecursos consultarRecursos() {
        Recursos r = nave.getRecursos().copia();
        return new ResumenRecursos(
                r.getCombustible(),
                r.getEnergia(),
                r.getDesgaste(),
                r.requiereMantenimiento()
        );
    }

    @Override
    public void agregarObservadorRecursos(Observador observador) {
        nave.getRecursos().agregarObservador(observador);
    }

    @Override
    public void quitarObservadorRecursos(Observador observador) {
        nave.getRecursos().quitarObservador(observador);
    }

    @Override
    public Resultado consumir(int combustible, int energia, int desgaste) {
        validarNoNegativo("combustible", combustible);
        validarNoNegativo("energia", energia);
        validarNoNegativo("desgaste", desgaste);

        try {
            nave.getRecursos().consumir(combustible, energia, desgaste);
            String msg = "Consumo aplicado: combustible=" + combustible
                    + ", energia=" + energia + ", desgaste=" + desgaste;
            bitacora.registrar(msg);
            return new Resultado(true, msg);

        } catch (RecursoFueraDeRangoException e) {
            String msg = "Consumo rechazado: " + e.getMessage();
            bitacora.registrar(msg);
            return new Resultado(false, msg);
        }
    }

    @Override
    public Resultado cargarCombustible(int cantidad) {
        validarNoNegativo("cantidad de combustible", cantidad);
        try {
            nave.getRecursos().cargarCombustible(cantidad);
            String msg = "Combustible cargado: +" + cantidad;
            bitacora.registrar(msg);
            return new Resultado(true, msg);
        } catch (RecursoFueraDeRangoException | IllegalArgumentException e) {
            String msg = "Carga de combustible rechazada: " + e.getMessage();
            bitacora.registrar(msg);
            return new Resultado(false, msg);
        }
    }

    @Override
    public Resultado cargarEnergia(int cantidad) {
        validarNoNegativo("cantidad de energia", cantidad);
        try {
            nave.getRecursos().cargarEnergia(cantidad);
            String msg = "Energia cargada: +" + cantidad;
            bitacora.registrar(msg);
            return new Resultado(true, msg);
        } catch (RecursoFueraDeRangoException | IllegalArgumentException e) {
            String msg = "Carga de energia rechazada: " + e.getMessage();
            bitacora.registrar(msg);
            return new Resultado(false, msg);
        }
    }

    @Override
    public Resultado realizarMantenimiento() {
        nave.getRecursos().realizarMantenimiento();
        String msg = "Mantenimiento realizado: desgaste a 0";
        bitacora.registrar(msg);
        return new Resultado(true, msg);
    }

    @Override
    public Resultado prepararSalto() {
        return turnoDeDemostracion("Disponible", true);
    }

    @Override
    public Resultado saltar() {
        return turnoDeDemostracion(null, false);
    }

    @Override
    public Resultado enfriar() {
        return turnoDeDemostracion("En warp", true);
    }

    @Override
    public Resultado terminarEnfriamiento() {
        return turnoDeDemostracion("Enfriamiento", false);
    }

    @Override
    public void registrarEvento(String descripcion) {
        bitacora.registrar(descripcion);
    }

    @Override
    public List<Evento> consultarBitacora() {
        return bitacora.getEventos();
    }

    @Override
    public void agregarObservadorBitacora(Observador observador) {
        bitacora.agregarObservador(observador);
    }

    @Override
    public void quitarObservadorBitacora(Observador observador) {
        bitacora.quitarObservador(observador);
    }

    // ---- metodos internos ----

    private ResultadoSalto ejecutarSalto(String operacion, PosicionNave siguiente,
                                         Supplier<ResultadoSalto> accion) {
        validarCasilla(siguiente);
        ResultadoSalto resultado;
        try {
            resultado = Objects.requireNonNull(accion.get());
        } catch (ExcepcionDominio | RecursoFueraDeRangoException e) {
            String motivo = e.getMessage();
            if (motivo == null || motivo.isBlank()) {
                motivo = "El subsistema rechazó el salto.";
            }
            resultado = new ResultadoSalto(ResultadoSalto.Estado.RECHAZADO, motivo);
        }
        registrarEvento(operacion + " hacia " + describir(siguiente)
                + " [" + resultado.estado() + "]. " + resultado.mensaje());
        return resultado;
    }

    private static void validarCasilla(PosicionNave casilla) {
        if (casilla == null) {
            throw new IllegalArgumentException("La casilla es obligatoria.");
        }
    }

    private static String describir(PosicionNave casilla) {
        return "(" + casilla.fila() + ", " + casilla.columna() + ")";
    }

    private Resultado turnoDeDemostracion(String estadoRequerido, boolean warp) {
        if (navegacion != null) {
            String mensaje = "Salto rechazado: indicá la siguiente casilla de la ruta.";
            registrarEvento(mensaje);
            return new Resultado(false, mensaje);
        }
        if (estadoRequerido != null && !estadoRequerido.equals(estadoDeDemostracion)) {
            String mensaje = "Transición rechazada desde " + estadoDeDemostracion + ".";
            registrarEvento(mensaje);
            return new Resultado(false, mensaje);
        }
        ResultadoDeTurno turno = warp ? nave.getMotor().saltarWarp() : nave.getMotor().saltar();
        estadoDeDemostracion = turno.getEstadoActual();
        ResultadoSalto resultado = NavegacionConTablero.interpretar(turno);
        registrarEvento(resultado.mensaje());
        return new Resultado(resultado.estado() != ResultadoSalto.Estado.RECHAZADO, resultado.mensaje());
    }

    private static void validarNoNegativo(String nombre, int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de " + nombre + " no puede ser negativa (recibido: " + valor + ")");
        }
    }
}
