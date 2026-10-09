package pruebas;

import java.util.List;

import modelo.asistente.AsistenteDeComando;
import modelo.universo.AsistenteDuplicadoException;
import modelo.universo.AsistenteInexistenteException;
import modelo.universo.CentroDeControl;

/** Pruebas del centro de control: registro por abstraccion, busqueda por id y una sola nave activa. */
class PruebasCentroDeControl {

    /** Asistente de prueba: otra variante de la abstraccion. */
    private static class AsistenteFalso implements AsistenteDeComando {
        private final String id;
        AsistenteFalso(String id) { this.id = id; }
        public String getNaveId() { return id; }
    }

    static void correrTodas() {
        System.out.println("CentroDeControl");
        try { registrarYObtenerPorId(); Verificador.ok("registrar y obtener por id devuelve el mismo asistente"); }
        catch (Throwable t) { Verificador.fallo("registrar y obtener por id devuelve el mismo asistente", t); }
        try { duplicadoRechazadoSinCambios(); Verificador.ok("id duplicado: se rechaza y el centro no cambia"); }
        catch (Throwable t) { Verificador.fallo("id duplicado: se rechaza y el centro no cambia", t); }
        try { obtenerInexistente(); Verificador.ok("obtener un id inexistente lanza excepcion"); }
        catch (Throwable t) { Verificador.fallo("obtener un id inexistente lanza excepcion", t); }
        try { unaSolaNaveActiva(); Verificador.ok("una sola nave activa a la vez"); }
        catch (Throwable t) { Verificador.fallo("una sola nave activa a la vez", t); }
        try { activarInexistenteConservaActivo(); Verificador.ok("activar un id inexistente conserva el activo anterior"); }
        catch (Throwable t) { Verificador.fallo("activar un id inexistente conserva el activo anterior", t); }
        try { varianteNuevaSeRegistraSinTocarElCentro(); Verificador.ok("una variante nueva de asistente se registra sin tocar el centro"); }
        catch (Throwable t) { Verificador.fallo("una variante nueva de asistente se registra sin tocar el centro", t); }
        try { registrarInvalidoLanzaIllegalArgument(); Verificador.ok("asistente nulo o id vacio: se rechaza y el centro no cambia"); }
        catch (Throwable t) { Verificador.fallo("asistente nulo o id vacio: se rechaza y el centro no cambia", t); }
        try { idsEnOrdenYSoloLectura(); Verificador.ok("ids() en orden de registro y de solo lectura"); }
        catch (Throwable t) { Verificador.fallo("ids() en orden de registro y de solo lectura", t); }
    }

    private static void registrarYObtenerPorId() throws Exception {
        CentroDeControl c = new CentroDeControl();
        AsistenteDeComando a = new AsistenteFalso("A1");
        c.registrar(a);
        Verificador.verificar(c.obtener("A1") == a, "debe devolver el mismo objeto");
        Verificador.verificar(c.existe("A1"), "existe(A1)");
        Verificador.igual(1, c.cantidad(), "cantidad");
    }

    private static void duplicadoRechazadoSinCambios() throws Exception {
        CentroDeControl c = new CentroDeControl();
        AsistenteDeComando original = new AsistenteFalso("A1");
        c.registrar(original);
        try {
            c.registrar(new AsistenteFalso("A1"));
            Verificador.fallar("deberia lanzar AsistenteDuplicadoException");
        } catch (AsistenteDuplicadoException e) {
            Verificador.igual("A1", e.getId(), "id en la excepcion");
        }
        Verificador.igual(1, c.cantidad(), "cantidad tras el rechazo");
        Verificador.verificar(c.obtener("A1") == original, "sigue el original");
    }

    private static void obtenerInexistente() {
        CentroDeControl c = new CentroDeControl();
        try {
            c.obtener("X");
            Verificador.fallar("deberia lanzar AsistenteInexistenteException");
        } catch (AsistenteInexistenteException e) {
            Verificador.igual("X", e.getId(), "id en la excepcion");
        }
    }

    private static void unaSolaNaveActiva() throws Exception {
        CentroDeControl c = new CentroDeControl();
        AsistenteDeComando a = new AsistenteFalso("A1");
        AsistenteDeComando b = new AsistenteFalso("A2");
        c.registrar(a);
        c.registrar(b);
        Verificador.verificar(!c.hayActivo(), "al inicio no hay activo");
        c.seleccionarActivo("A1");
        Verificador.verificar(c.getActivo() == a, "activo A1");
        c.seleccionarActivo("A2");
        Verificador.verificar(c.getActivo() == b, "activo A2");
        Verificador.igual(2, c.cantidad(), "ambas siguen registradas");
    }

    private static void activarInexistenteConservaActivo() throws Exception {
        CentroDeControl c = new CentroDeControl();
        AsistenteDeComando a = new AsistenteFalso("A1");
        c.registrar(a);
        c.seleccionarActivo("A1");
        try {
            c.seleccionarActivo("NO-EXISTE");
            Verificador.fallar("deberia lanzar AsistenteInexistenteException");
        } catch (AsistenteInexistenteException e) {
            // esperado
        }
        Verificador.verificar(c.getActivo() == a, "el activo anterior se conserva");
    }

    private static void varianteNuevaSeRegistraSinTocarElCentro() throws Exception {
        CentroDeControl c = new CentroDeControl();
        AsistenteDeComando variante = new AsistenteDeComando() {
            public String getNaveId() { return "VARIANTE"; }
        };
        c.registrar(variante);
        Verificador.verificar(c.obtener("VARIANTE") == variante, "la variante se obtiene por su abstraccion");
    }

    private static void registrarInvalidoLanzaIllegalArgument() throws Exception {
        CentroDeControl c = new CentroDeControl();
        try {
            c.registrar(null);
            Verificador.fallar("debia rechazar el asistente nulo");
        } catch (IllegalArgumentException e) {
            // esperado
        }
        try {
            c.registrar(new AsistenteFalso("  "));
            Verificador.fallar("debia rechazar el id vacio");
        } catch (IllegalArgumentException e) {
            // esperado
        }
        try {
            c.registrar(new AsistenteFalso(null));
            Verificador.fallar("debia rechazar el id nulo");
        } catch (IllegalArgumentException e) {
            // esperado
        }
        Verificador.igual(0, c.cantidad(), "el centro no cambia");
    }

    private static void idsEnOrdenYSoloLectura() throws Exception {
        CentroDeControl c = new CentroDeControl();
        c.registrar(new AsistenteFalso("B"));
        c.registrar(new AsistenteFalso("A"));
        List<String> ids = c.ids();
        Verificador.igual("B", ids.get(0), "primero registrado");
        Verificador.igual("A", ids.get(1), "segundo registrado");
        try {
            ids.add("X");
            Verificador.fallar("la lista debe ser de solo lectura");
        } catch (UnsupportedOperationException e) {
            // esperado
        }
    }
}
