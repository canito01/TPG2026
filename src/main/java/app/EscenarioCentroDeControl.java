package app;

import java.io.PrintStream;

import modelo.universo.AsistenteDuplicadoException;
import modelo.universo.AsistenteInexistenteException;
import modelo.universo.CentroDeControl;

/**
 * Universo y centro de control (Aclaracion R1, R2, R6):
 *  - registrar naves listas para operar (por su asistente) y devolver cualquiera por id;
 *  - usar una unica nave a la vez aunque haya varias registradas;
 *  - rechazar lo invalido sin alterar el centro.
 * Los asistentes son de demostracion: en el programa real las naves salen de la
 * fabrica de Eneas y cada una tiene el asistente de Agustin.
 */
class EscenarioCentroDeControl extends EscenarioBase {

    @Override
    public String getNombre() { return "Universo - Centro de control (R1, R2)"; }

    @Override
    public void ejecutar(PrintStream out) {
        CentroDeControl centro = new CentroDeControl();

        out.println("U.1 Alta de naves (por su asistente):");
        centro.registrar(AsistenteDeDemostracion.enLinea("NAVE-1", 100, 100));
        centro.registrar(AsistenteDeDemostracion.enLinea("NAVE-2", 100, 100));
        centro.registrar(AsistenteDeDemostracion.enLinea("NAVE-3", 100, 100));
        out.println("  Registradas: " + centro.ids());
        exigir(centro.cantidad() == 3, "deben haber 3 registradas");
        exigir(!centro.hayActivo(), "al inicio no hay nave activa");

        out.println("U.2 Una sola nave activa a la vez:");
        centro.seleccionarActivo("NAVE-1");
        out.println("  Activa: " + centro.getActivo().getNaveId());
        centro.seleccionarActivo("NAVE-2");
        out.println("  Activa tras seleccionar NAVE-2: " + centro.getActivo().getNaveId());
        exigir(centro.getActivo().getNaveId().equals("NAVE-2"), "la activa debe ser NAVE-2");
        exigir(centro.cantidad() == 3, "siguen registradas las tres");

        out.println("U.3 Lista para los controladores y devolver cualquiera por id:");
        exigir(centro.getAsistentes().size() == 3, "la lista para los controladores tiene las 3 naves");
        exigir(centro.getAsistentes().get(0).getNaveId().equals("NAVE-1"), "la lista respeta el orden de registro");
        try {
            centro.getAsistentes().clear();
            exigir(false, "la lista debe ser de solo lectura");
        } catch (UnsupportedOperationException e) {
            out.println("  Lista de solo lectura: " + centro.getAsistentes().size() + " naves");
        }
        out.println("  Pedida NAVE-3: " + centro.obtener("NAVE-3").getNaveId());

        out.println("U.4 Rechazos (el centro no cambia):");
        try {
            centro.registrar(AsistenteDeDemostracion.enLinea("NAVE-1", 100, 100));
            exigir(false, "debia rechazar el id duplicado");
        } catch (AsistenteDuplicadoException e) {
            out.println("  " + e.getMessage());
        }
        try {
            centro.obtener("NAVE-9");
            exigir(false, "debia rechazar el id inexistente");
        } catch (AsistenteInexistenteException e) {
            out.println("  " + e.getMessage());
        }
        try {
            centro.seleccionarActivo("NAVE-9");
            exigir(false, "debia rechazar seleccionar un id inexistente");
        } catch (AsistenteInexistenteException e) {
            exigir(centro.getActivo().getNaveId().equals("NAVE-2"), "la activa anterior se conserva");
        }
        try {
            centro.registrar(null);
            exigir(false, "debia rechazar el asistente nulo");
        } catch (IllegalArgumentException e) {
            out.println("  " + e.getMessage());
        }
        exigir(centro.cantidad() == 3, "tras los rechazos siguen 3 registradas");
        out.println("  VERIFICADO: 3 registradas, activa NAVE-2, cuatro rechazos sin cambios");
    }
}
