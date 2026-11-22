package modelo.misiones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.asistente.AsistenteDeComando;
import modelo.asistente.PosicionNave;
import modelo.asistente.Resultado;
import modelo.asistente.ResultadoEscaneo;
import modelo.asistente.ResultadoSalto;
import modelo.asistente.ResumenRecursos;

/**
 * Clase base de las misiones. Implementa el patron Template Method: el metodo
 * fija el orden preparar -> ejecutar ->
 * evaluar -> cerrar, y las subclases solo completan los "hooks" que dependen del tipo
 * de mision (destino, costo energetico, verificacion en destino, recompensa, textos).
 *
 * @invariant nombre != null && !nombre.isBlank()
 * @invariant 0 <= turnos <= MAX_TURNOS
 * @invariant fallida => motivoFallo != null
 * @invariant (fase == EVALUADA || fase == CERRADA) <=> informe != null
 * @invariant recorrido != null && acciones != null
 *
 */
public abstract class Mision {


    public static final int MAX_TURNOS = 200;
    protected static final int DESGASTE_DESTRUCCION = 100;

    private enum Fase { SIN_INICIAR, PREPARADA, EJECUTADA, EVALUADA, CERRADA }

    private final String nombre;
    private Fase fase = Fase.SIN_INICIAR;
    private InformeMision informe;

    private final List<String> acciones = new ArrayList<>();
    private final List<PosicionNave> recorrido = new ArrayList<>();
    private List<PosicionNave> rutaPlanificada = Collections.emptyList();
    private PosicionNave destino;
    private boolean fallida;
    private String motivoFallo;
    private int turnos;
    private int combustibleInicial;
    private int energiaInicial;
    private int desgasteInicial;

    /**
     * @pre nombre != null && !nombre.isBlank()
     * @throws IllegalArgumentException si el nombre es nulo o vacio
     */
    protected Mision(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("La mision necesita un nombre");
        }
        this.nombre = nombre;
    }

    /**
     * Ejecuta el ciclo completo de la mision. Es final: el orden no se puede alterar.
     *
     * @pre a != null
     * @pre la mision no esta en curso (fase SIN_INICIAR o CERRADA)
     * @post retorna un informe != null y la mision queda CERRADA
     * @post si la mision fallo por una condicion del dominio (recursos, ruta, destruccion,
     *       verificacion en destino) NO se lanza excepcion: el informe indica el fracaso
     * @throws IllegalArgumentException si a es null
     * @throws IllegalStateException si la mision ya esta en curso o se viola el contrato interno
     */
    public final InformeMision ejecutarCiclo(AsistenteDeComando a) {
        if (a == null) {
            throw new IllegalArgumentException("El asistente de comando no puede ser null");
        }
        if (fase != Fase.SIN_INICIAR && fase != Fase.CERRADA) {
            throw new IllegalStateException("La mision " + nombre + " ya esta en curso");
        }
        reiniciar();
        try {
            preparar(a);
            if (!fallida) {
                ejecutar(a);
            }
            evaluar(a);
            cerrar(a);
            return informe;
        } finally {
            if (fase != Fase.CERRADA) {   // salio por excepcion: se permite reintentar
                fase = Fase.SIN_INICIAR;
                informe = null;
            }
        }
    }

    /**
     * Verifica recursos minimos y calcula la ruta con el radar de largo alcance.
     * La verificacion va ANTES del calculo de ruta para no gastar energia de radar
     * en una mision que no puede iniciarse.
     *
     * @pre fase == SIN_INICIAR
     * @post fase == PREPARADA
     * @post si no hay recursos minimos o no hay ruta: fallida == true y la nave no cambio
     * @post si no fallo: rutaPlanificada no vacia y destino != null
     */
    protected void preparar(AsistenteDeComando a) {
        exigirFase(Fase.SIN_INICIAR, "preparar");
        ResumenRecursos inicial = a.consultarRecursos();
        combustibleInicial = inicial.getCombustible();
        energiaInicial = inicial.getEnergia();
        desgasteInicial = inicial.getDesgaste();

        destino = determinarDestino(a);
        if (destino == null) {
            throw new IllegalStateException("determinarDestino() no puede retornar null");
        }

        String faltante = recursosMinimosFaltantes();
        if (faltante != null) {
            fallar("Consumo rechazado: recursos insuficientes para iniciar (" + faltante + ")");
        } else {
            List<PosicionNave> ruta = a.calcularRuta(destino);
            if (ruta == null || ruta.isEmpty()) {
                fallar("No existe una ruta valida hacia " + coord(destino));
            } else {
                rutaPlanificada = Collections.unmodifiableList(new ArrayList<>(ruta));
                registrarAccion("Mision " + nombre + " preparada: ruta de " + ruta.size()
                        + " pasos hacia " + coord(destino));
            }
        }
        fase = Fase.PREPARADA;
        verificarInvariantes();
    }

    /**
     * Consumo de despegue + bucle de turnos (nave y tablero) + verificacion en destino.
     *
     * @pre fase == PREPARADA && !fallida   (una mision no se ejecuta sin preparacion)
     * @post fase == EJECUTADA
     * @post turnos <= MAX_TURNOS
     * @post si la nave se destruyo, se agoto el limite de turnos o se interrumpio el hilo:
     *       fallida == true
     * @post si llego al destino: la nave esta en destino y se ejecuto verificarEnDestino()
     * @throws IllegalStateException si no se preparo la mision
     */
    protected void ejecutar(AsistenteDeComando a) {
        if (fase != Fase.PREPARADA || fallida) {
            throw new IllegalStateException("Una mision no puede ejecutarse sin preparacion previa.");
        }

        // Consumo de despegue (atomico: si falla, la nave no cambia)
        Resultado r = a.consumir(combustibleDespegue(), energiaDespegue(), desgasteDespegue());
        if (!r.fueExito()) {
            fallar("Recursos insuficientes para el despegue: " + r.getMensaje());
        } else {
            registrarAccion("Despegue: " + resumenDespegue());
            recorrido.add(posicionActual(a));

            while (puedeSeguirViajando(a)) {
                if (turnos >= MAX_TURNOS) {
                    fallar("Se supero el maximo de " + MAX_TURNOS + " turnos sin llegar al destino");
                    break;
                }
                turnos++;
                turnoDeLaNave(a);
                if (fallida || naveDestruida(a)) {
                    break;
                }
                a.avanzarTurnoTablero();
                if (!pausarTurno()) {
                    break;
                }
            }

            if (naveDestruida(a)) {
                fallar("La nave fue destruida en " + coord(posicionActual(a)) + "; la tripulacion se perdio");
            } else if (!fallida) {
                registrarAccion("Llegada al destino " + coord(destino) + " en " + turnos + " turnos");
                verificarEnDestino(a);
            }
        }
        fase = Fase.EJECUTADA;
        verificarInvariantes();
    }

    /** Un turno de la nave: inspeccionar posicion, escanear y moverse. */
    private void turnoDeLaNave(AsistenteDeComando a) {
        String evento = a.interactuarConElementoActual();
        if (evento != null && !evento.isBlank()) {
            registrarAccion("Turno " + turnos + ": " + evento);
        }
        if (naveDestruida(a)) {
            return;
        }

        PosicionNave antes = posicionActual(a);
        int indice = rutaPlanificada.indexOf(antes);
        if (indice < 0 || indice + 1 >= rutaPlanificada.size()) {
            fallar("La nave no tiene un paso siguiente en la ruta planificada.");
            return;
        }
        PosicionNave siguiente = rutaPlanificada.get(indice + 1);
        ResultadoEscaneo escaneo = a.escanear(siguiente);
        if (escaneo == ResultadoEscaneo.RECHAZADO) {
            fallar("No se pudo escanear la casilla " + coord(siguiente));
            return;
        }
        boolean obstaculoAdelante = escaneo == ResultadoEscaneo.OBSTACULO;
        ResultadoSalto salto = obstaculoAdelante ? a.saltarWarp(siguiente) : a.saltar(siguiente);
        if (salto.estado() == ResultadoSalto.Estado.RECHAZADO) {
            fallar(salto.mensaje());
            return;
        }
        PosicionNave despues = posicionActual(a);
        String modo = obstaculoAdelante ? "Warp (obstaculo detectado)" : "salto normal";
        if (despues.equals(antes)) {
            registrarAccion("Turno " + turnos + ": " + salto.mensaje());
        } else {
            recorrido.add(despues);
            registrarAccion("Turno " + turnos + ": " + modo + " de " + coord(antes) + " a " + coord(despues));
        }
    }

    private boolean puedeSeguirViajando(AsistenteDeComando a) {
        return !fallida && !naveDestruida(a) && !posicionActual(a).equals(destino);
    }

    private PosicionNave posicionActual(AsistenteDeComando a) {
        return a.consultarPosicion().orElseThrow(
                () -> new IllegalStateException("El asistente no tiene navegación configurada."));
    }

    /** Regla del enunciado: desgaste 100, energia 0 o combustible 0 destruyen la nave. */
    private boolean naveDestruida(AsistenteDeComando a) {
        ResumenRecursos recursos = a.consultarRecursos();
        return a.estaDestruida()
                || recursos.getDesgaste() >= DESGASTE_DESTRUCCION
                || recursos.getEnergia() <= 0
                || recursos.getCombustible() <= 0;
    }

    /** @return false si el hilo fue interrumpido (la mision se marca como fallida). */
    private boolean pausarTurno() {
        try {
            Thread.sleep(pausaPorTurnoMs());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fallar("La mision fue interrumpida durante la travesia");
            return false;
        }
    }

    /**
     * Confecciona el informe final (exito o fracaso + balance de recursos).
     * Todavia NO aplica recompensas: eso ocurre en cerrar().
     *
     * @pre fase == EJECUTADA, o fase == PREPARADA con la mision ya fallida
     * @post informe != null && fase == EVALUADA
     */
    protected void evaluar(AsistenteDeComando a) {
        boolean ok = (fase == Fase.EJECUTADA) || (fase == Fase.PREPARADA && fallida);
        if (!ok) {
            throw new IllegalStateException("No se puede evaluar una mision en fase " + fase);
        }
        boolean exito = !fallida;
        registrarAccion("Mision " + nombre + " evaluada: " + (exito ? "EXITOSA" : "FALLIDA"));

        String observaciones = exito
                ? "Mision completada en " + turnos + " turnos."
                : "Motivo del fracaso: " + motivoFallo;

        informe = new InformeMision(nombre, objetivo(), exito, descripcionResultado(exito),
                acciones, recorrido, balanceDeRecursos(a, exito),
                String.valueOf(a.consultarEstado()), observaciones);
        fase = Fase.EVALUADA;
        verificarInvariantes();
    }

    /**
     * Impacta de forma definitiva en la nave lo ganado o entregado (solo si hubo exito).
     *
     * @pre fase == EVALUADA && informe != null  (no se cierra sin resultado e informe)
     * @post fase == CERRADA
     * @post si !fallida: se aplico la recompensa/entrega de la mision exactamente una vez
     * @post el informe contiene el estado final de la nave
     * @throws IllegalStateException si se intenta cerrar sin evaluar
     */
    protected void cerrar(AsistenteDeComando a) {
        if (fase != Fase.EVALUADA || informe == null) {
            throw new IllegalStateException("Una mision no puede cerrarse sin resultado e informe.");
        }
        if (!fallida) {
            aplicarRecompensa(a);
            registrarAccion("Cierre: " + describirRecompensa());
        } else {
            a.registrarEvento(nombre + " fallida. " + motivoFallo);
            registrarAccion("Cierre sin recompensas (mision fallida)");
        }
        registrarAccion("Mision " + nombre + " cerrada");
        informe.registrarEstadoFinal(String.valueOf(a.consultarEstado()));
        fase = Fase.CERRADA;
        verificarInvariantes();
    }


    /** @return coordenada objetivo. @post != null */
    protected abstract PosicionNave determinarDestino(AsistenteDeComando a);

    /** @return energia consumida al despegar (Anexo: M-01=5, M-02=10, M-03=0). @post >= 0 */
    protected abstract int energiaDespegue();

    /**
     * Verificacion al llegar al destino. Por defecto no exige nada.
     * Si la condicion no se cumple, la subclase debe llamar a {@link #fallar(String)}.
     * NO descuenta nada: el descuento efectivo se hace en cerrar().
     */
    protected void verificarEnDestino(AsistenteDeComando a) { }

    /**
     * Aplica la recompensa o entrega definitiva.
     * @pre solo se invoca si la mision no fallo
     */
    protected abstract void aplicarRecompensa(AsistenteDeComando a);

    /** @return texto de lo que se entrega/recibe al cerrar (para informe y bitacora). */
    protected abstract String describirRecompensa();

    /** @return objetivo evaluado, para el informe. */
    protected abstract String objetivo();

    /** @return texto del resultado final segun el exito. */
    protected abstract String descripcionResultado(boolean exito);

    /** @return costo base de combustible al despegar (igual para las tres misiones). */
    protected int combustibleDespegue() { return 4; }

    /** @return desgaste generado al despegar (igual para las tres misiones). */
    protected int desgasteDespegue() { return 4; }

    /** @return pausa entre turnos para poder ver el avance en el tablero. */
    protected int pausaPorTurnoMs() { return 300; }

    /**
     * Marca la mision como fallida. Se conserva el PRIMER motivo registrado.
     * @pre motivo != null && !motivo.isBlank()
     */
    protected final void fallar(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo del fallo no puede estar vacio");
        }
        if (!fallida) {
            fallida = true;
            motivoFallo = motivo;
            registrarAccion("FALLO: " + motivo);
        }
    }

    /** Agrega una accion a la lista (y al informe, si ya fue generado). */
    protected final void registrarAccion(String accion) {
        if (accion == null || accion.isBlank()) {
            throw new IllegalArgumentException("Accion vacia");
        }
        acciones.add(accion);
        if (informe != null) {
            informe.agregarAccion(accion);
        }
    }

    protected static String coord(PosicionNave p) {
        return p == null ? "(sin posicion)" : "(" + p.columna() + "," + p.fila() + ")";
    }

    /** @return copia de las acciones registradas hasta el momento. */
    protected List<String> getAcciones() {
        return new ArrayList<>(acciones);
    }

    protected final boolean haFallado() { return fallida; }

    public String getNombre() { return nombre; }

    /**
     * @return informe de la ultima ejecucion, o null si todavia no hay uno.
     * Usar el valor devuelto por ejecutarCiclo(); no consultar mientras la mision corre.
     */
    public InformeMision getInforme() { return informe; }

    public int getTurnos() { return turnos; }



    private void reiniciar() {
        acciones.clear();
        recorrido.clear();
        rutaPlanificada = Collections.emptyList();
        destino = null;
        fallida = false;
        motivoFallo = null;
        turnos = 0;
        informe = null;
        fase = Fase.SIN_INICIAR;
    }

    private void exigirFase(Fase esperada, String paso) {
        if (fase != esperada) {
            throw new IllegalStateException("No se puede " + paso + " en fase " + fase
                    + " (se esperaba " + esperada + ")");
        }
    }

    /** @return null si alcanzan los recursos; si no, descripcion de lo que falta. */
    private String recursosMinimosFaltantes() {
        // Tras el despegue ningun recurso puede quedar en 0 ni el desgaste en 100 (nave destruida).
        if (combustibleInicial <= combustibleDespegue()) {
            return "combustible " + combustibleInicial + " (se requieren mas de " + combustibleDespegue() + ")";
        }
        if (energiaInicial <= energiaDespegue()) {
            return "energia " + energiaInicial + " (se requieren mas de " + energiaDespegue() + ")";
        }
        if (desgasteInicial + desgasteDespegue() >= DESGASTE_DESTRUCCION) {
            return "desgaste " + desgasteInicial + " (el despegue lo llevaria a " + DESGASTE_DESTRUCCION + ")";
        }
        return null;
    }

    private String resumenDespegue() {
        return "combustible -" + combustibleDespegue()
                + ", energia -" + energiaDespegue()
                + ", desgaste +" + desgasteDespegue();
    }

    private String balanceDeRecursos(AsistenteDeComando a, boolean exito) {
        ResumenRecursos recursos = a.consultarRecursos();
        StringBuilder sb = new StringBuilder();
        sb.append("Combustible: ").append(combustibleInicial).append(" -> ").append(recursos.getCombustible())
                .append(" (").append(variacion(recursos.getCombustible() - combustibleInicial)).append(")");
        sb.append(" | Energia: ").append(energiaInicial).append(" -> ").append(recursos.getEnergia())
                .append(" (").append(variacion(recursos.getEnergia() - energiaInicial)).append(")");
        sb.append(" | Desgaste: ").append(desgasteInicial).append(" -> ").append(recursos.getDesgaste())
                .append(" (").append(variacion(recursos.getDesgaste() - desgasteInicial)).append(")");
        if (exito) {
            sb.append(" | A aplicar en el cierre: ").append(describirRecompensa());
        }
        return sb.toString();
    }

    private static String variacion(int delta) {
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }

    /** Comprueba los invariantes de clase al terminar cada fase. */
    private void verificarInvariantes() {
        if (turnos < 0 || turnos > MAX_TURNOS) {
            throw new IllegalStateException("Invariante violado: turnos fuera de rango (" + turnos + ")");
        }
        if (fallida && motivoFallo == null) {
            throw new IllegalStateException("Invariante violado: mision fallida sin motivo");
        }
        boolean conInforme = (fase == Fase.EVALUADA || fase == Fase.CERRADA);
        if (conInforme != (informe != null)) {
            throw new IllegalStateException("Invariante violado: fase " + fase + " e informe inconsistentes");
        }
    }
}
