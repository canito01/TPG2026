package modelo.asistente;

import java.util.List;

import modelo.nave.Nave;
import modelo.nave.RecursoFueraDeRangoException;
import modelo.nave.Recursos;
import modelo.motor.MotorWarp;

/**
 * Implementacion concreta del asistente. Opera una sola nave.
 * Delega al subsistema que corresponda y captura las excepciones
 * de dominio para devolver un Resultado en vez de propagarlas.
 * Registra todo en la bitacora.
 */
public class AsistenteBase implements AsistenteDeComando {

    private final Nave nave;
    private final Bitacora bitacora;

    public AsistenteBase(Nave nave) {
        if (nave == null) {
            throw new IllegalArgumentException("La nave no puede ser nula.");
        }
        this.nave = nave;
        this.bitacora = new Bitacora();
    }

    @Override
    public String getNaveId() {
        return nave.getId();
    }

    @Override
    public String consultarEstado() {
        return nave.getMotor().getEstado().nombre();
    }

    @Override
    public ResumenRecursos consultarRecursos() {
        Recursos r = nave.getRecursos();
        return new ResumenRecursos(
                r.getCombustible(),
                r.getEnergia(),
                r.getDesgaste(),
                r.requiereMantenimiento()
        );
    }

    @Override
    public Resultado consumir(int combustible, int energia, int desgaste) {
        validarNoNegativo("combustible", combustible);
        validarNoNegativo("energia", energia);
        validarNoNegativo("desgaste", desgaste);

        try {
            // primero consumimos combustible y energia (atomico en Recursos)
            nave.getRecursos().consumir(combustible, energia);
            // si paso, aplicamos el desgaste
            if (desgaste > 0) {
                try {
                    nave.getRecursos().desgastar(desgaste);
                } catch (RecursoFueraDeRangoException e) {
                    // el consumo ya se aplico pero el desgaste no entra
                    // Recursos no tiene rollback asi que queda registrado como rechazo
                    String msg = "Desgaste rechazado: " + e.getMessage();
                    bitacora.registrar(msg);
                    return new Resultado(false, msg);
                }
            }
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
        return ejecutarTransicion(() -> nave.getMotor().prepararSalto(),
                "Salto preparado", "prepararSalto");
    }

    @Override
    public Resultado saltar() {
        return ejecutarTransicion(() -> nave.getMotor().saltar(),
                "Salto ejecutado", "saltar");
    }

    @Override
    public Resultado enfriar() {
        return ejecutarTransicion(() -> nave.getMotor().enfriar(),
                "Enfriamiento iniciado", "enfriar");
    }

    @Override
    public Resultado terminarEnfriamiento() {
        return ejecutarTransicion(() -> nave.getMotor().terminarEnfriamiento(),
                "Enfriamiento terminado, motor disponible", "terminarEnfriamiento");
    }

    @Override
    public void registrarEvento(String descripcion) {
        bitacora.registrar(descripcion);
    }

    @Override
    public List<Evento> consultarBitacora() {
        return bitacora.getEventos();
    }

    // ---- metodos internos ----

    // intenta la transicion en el motor, si falla captura la excepcion y devuelve rechazo
    private Resultado ejecutarTransicion(Runnable accion, String mensajeExito,
                                         String nombreAccion) {
        try {
            accion.run();
            bitacora.registrar(mensajeExito);
            return new Resultado(true, mensajeExito);
        } catch (TransicionInvalidaException e) {
            String msg = "Transicion rechazada (" + nombreAccion + "): " + e.getMessage();
            bitacora.registrar(msg);
            return new Resultado(false, msg);
        }
    }

    private static void validarNoNegativo(String nombre, int valor) {
        if (valor < 0) {
            throw new IllegalArgumentException(
                    "La cantidad de " + nombre + " no puede ser negativa (recibido: " + valor + ")");
        }
    }
}
