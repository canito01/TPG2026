package pruebas;

/** Utilidad minima de pruebas (sin librerias externas): cuenta aciertos y fallos. */
public final class Verificador {

    private static int pasaron = 0;
    private static int fallaron = 0;

    private Verificador() { }

    public static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    public static void igual(Object esperado, Object real, String mensaje) {
        if (esperado == null ? real != null : !esperado.equals(real)) {
            throw new AssertionError(mensaje + " | esperado: " + esperado + " | real: " + real);
        }
    }

    public static void fallar(String mensaje) { throw new AssertionError(mensaje); }

    public static void ok(String nombre) {
        pasaron++;
        System.out.println("  [OK]    " + nombre);
    }

    public static void fallo(String nombre, Throwable t) {
        fallaron++;
        System.out.println("  [FALLA] " + nombre + " -> " + t);
    }

    public static int getPasaron() { return pasaron; }

    public static int getFallaron() { return fallaron; }
}
