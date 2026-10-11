package pruebas;

import modelo.asistente.TransicionInvalidaException;
import modelo.motor.EstadoMotor;
import modelo.motor.MotorWarp;

/** Pruebas del Motor Warp (State): transiciones validas, invalidas sin efecto y contrato. */
class PruebasMotorWarp {

    // Estados, como se llega a cada uno desde Disponible y que acciones acepta
    private static final String[] ESTADOS = {"Disponible", "Preparando salto", "En warp", "Enfriamiento"};
    private static final String[][] CAMINOS = {
        {},
        {"prepararSalto"},
        {"prepararSalto", "saltar"},
        {"prepararSalto", "saltar", "enfriar"}};
    private static final String[][] VALIDAS = {
        {"prepararSalto"},
        {"saltar"},
        {"enfriar", "terminarSalto"},
        {"terminarEnfriamiento"}};
    private static final String[] ACCIONES = {
        "prepararSalto", "saltar", "enfriar", "terminarEnfriamiento", "terminarSalto"};

    static void correrTodas() {
        System.out.println("MotorWarp");
        try { estadoInicialEsDisponible(); Verificador.ok("estado inicial es Disponible"); }
        catch (Throwable t) { Verificador.fallo("estado inicial es Disponible", t); }
        try { secuenciaValidaDeLaFicha(); Verificador.ok("secuencia valida: Disponible > Preparando salto > En warp > Enfriamiento > Disponible"); }
        catch (Throwable t) { Verificador.fallo("secuencia valida de la Ficha", t); }
        try { terminarSaltoVuelveADisponible(); Verificador.ok("terminarSalto: el salto termina en Disponible (R3)"); }
        catch (Throwable t) { Verificador.fallo("terminarSalto: el salto termina en Disponible (R3)", t); }
        try { transicionesInvalidasSinEfecto(); Verificador.ok("toda accion no permitida: excepcion, sin cambio de estado"); }
        catch (Throwable t) { Verificador.fallo("toda accion no permitida: excepcion, sin cambio de estado", t); }
        try { estaDisponibleSoloEnDisponible(); Verificador.ok("estaDisponible() es true solo en Disponible"); }
        catch (Throwable t) { Verificador.fallo("estaDisponible() es true solo en Disponible", t); }
        try { motoresIndependientes(); Verificador.ok("dos motores no se afectan entre si"); }
        catch (Throwable t) { Verificador.fallo("dos motores no se afectan entre si", t); }
    }

    private static void estadoInicialEsDisponible() {
        Verificador.igual("Disponible", new MotorWarp().getEstado().nombre(), "estado inicial");
    }

    private static void secuenciaValidaDeLaFicha() {
        MotorWarp m = new MotorWarp();
        m.prepararSalto();
        Verificador.igual("Preparando salto", m.getEstado().nombre(), "tras prepararSalto");
        m.saltar();
        Verificador.igual("En warp", m.getEstado().nombre(), "tras saltar");
        m.enfriar();
        Verificador.igual("Enfriamiento", m.getEstado().nombre(), "tras enfriar");
        m.terminarEnfriamiento();
        Verificador.igual("Disponible", m.getEstado().nombre(), "tras terminarEnfriamiento");
    }

    private static void terminarSaltoVuelveADisponible() {
        MotorWarp m = new MotorWarp();
        m.prepararSalto();
        m.saltar();
        m.terminarSalto();
        Verificador.igual("Disponible", m.getEstado().nombre(), "tras terminarSalto");
        Verificador.verificar(m.estaDisponible(), "la nave vuelve a estar disponible");
    }

    private static void transicionesInvalidasSinEfecto() {
        int combinacionesProbadas = 0;
        for (int i = 0; i < ESTADOS.length; i++) {
            for (int j = 0; j < ACCIONES.length; j++) {
                if (esValida(i, ACCIONES[j])) {
                    continue;
                }
                MotorWarp m = llegarA(i);
                EstadoMotor antes = m.getEstado();
                String contexto = ACCIONES[j] + " en " + ESTADOS[i];

                try {
                    ejecutar(m, ACCIONES[j]);
                    Verificador.fallar("deberia rechazar: " + contexto);
                } catch (TransicionInvalidaException ex) {
                    Verificador.igual(ESTADOS[i], ex.getEstado(), "estado en la excepcion: " + contexto);
                    Verificador.igual(ACCIONES[j], ex.getAccion(), "accion en la excepcion: " + contexto);
                }
                Verificador.verificar(m.getEstado() == antes, "el estado no debe cambiar (mismo objeto): " + contexto);
                Verificador.igual(ESTADOS[i], m.getEstado().nombre(), "el nombre no debe cambiar: " + contexto);
                combinacionesProbadas++;
            }
        }
        // 4 estados x 5 acciones = 20; validas: 1 + 1 + 2 + 1 = 5; invalidas: 15
        Verificador.igual(15, combinacionesProbadas, "combinaciones invalidas probadas");
    }

    private static void estaDisponibleSoloEnDisponible() {
        for (int i = 0; i < ESTADOS.length; i++) {
            MotorWarp m = llegarA(i);
            Verificador.igual(ESTADOS[i], m.getEstado().nombre(), "estado alcanzado");
            Verificador.igual(i == 0, m.estaDisponible(), "estaDisponible() en " + ESTADOS[i]);
        }
    }

    private static void motoresIndependientes() {
        MotorWarp a = new MotorWarp();
        MotorWarp b = new MotorWarp();
        a.prepararSalto();
        Verificador.igual("Preparando salto", a.getEstado().nombre(), "motor a");
        Verificador.igual("Disponible", b.getEstado().nombre(), "motor b no se toca");
    }

    private static MotorWarp llegarA(int estado) {
        MotorWarp m = new MotorWarp();
        for (int k = 0; k < CAMINOS[estado].length; k++) {
            ejecutar(m, CAMINOS[estado][k]);
        }
        return m;
    }

    private static boolean esValida(int estado, String accion) {
        for (int k = 0; k < VALIDAS[estado].length; k++) {
            if (VALIDAS[estado][k].equals(accion)) {
                return true;
            }
        }
        return false;
    }

    private static void ejecutar(MotorWarp m, String accion) {
        if (accion.equals("prepararSalto")) { m.prepararSalto(); }
        else if (accion.equals("saltar")) { m.saltar(); }
        else if (accion.equals("enfriar")) { m.enfriar(); }
        else if (accion.equals("terminarEnfriamiento")) { m.terminarEnfriamiento(); }
        else if (accion.equals("terminarSalto")) { m.terminarSalto(); }
        else { Verificador.fallar("accion desconocida en la prueba: " + accion); }
    }
}
