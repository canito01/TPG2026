package modelo.asistente;

import java.time.LocalDateTime;
import java.util.List;

/** Verificación reproducible de la bitácora, ejecutable sin bibliotecas externas. */
public class VerificacionBitacora {
    public static void main(String[] args) {
        Bitacora bitacora = new Bitacora();
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 3, 10, 0);
        Evento preparacion = new Evento(inicio, "Preparación de M-01");
        Evento salto = new Evento(inicio.plusMinutes(1), "Salto iniciado");
        Evento consumo = new Evento(inicio.plusMinutes(1), "Consumo registrado");

        bitacora.registrar(salto);
        bitacora.registrar(preparacion);
        bitacora.registrar(consumo);
        List<Evento> consulta = bitacora.getEventos();
        comprobar(consulta.equals(List.of(preparacion, salto, consumo)),
                "Orden temporal y orden de registro para fechas iguales");

        try {
            consulta.clear();
            throw new AssertionError("La consulta permitió borrar registros.");
        } catch (UnsupportedOperationException esperado) {
            comprobar(bitacora.getEventos().size() == 3, "La lista interna sigue intacta");
        }

        bitacora.registrar(new Evento(inicio.plusMinutes(2), "Misión cerrada"));
        comprobar(consulta.size() == 3 && bitacora.getEventos().size() == 4,
                "Una consulta anterior conserva sus datos");

        String[] invalidas = {null, "", "   ", "\t\n"};
        for (String descripcion : invalidas) {
            try {
                bitacora.registrar(descripcion);
                throw new AssertionError("Se aceptó una descripción inválida.");
            } catch (IllegalArgumentException esperado) {
                comprobar(bitacora.getEventos().size() == 4,
                        "El rechazo conserva los registros anteriores");
            }
        }

        try {
            bitacora.registrar((Evento) null);
            throw new AssertionError("Se aceptó un evento nulo.");
        } catch (IllegalArgumentException esperado) {
            comprobar(bitacora.getEventos().size() == 4, "Evento nulo rechazado sin cambios");
        }

        try {
            new Evento(null, "Sin fecha");
            throw new AssertionError("Se aceptó una fecha nula.");
        } catch (IllegalArgumentException esperado) {
            // El evento inválido no llegó a construirse.
        }

        // LocalDateTime es inmutable: la operación devuelve una fecha nueva.
        LocalDateTime fechaOriginal = preparacion.getFecha();
        LocalDateTime otraFecha = fechaOriginal.plusDays(1);
        comprobar(!otraFecha.equals(fechaOriginal)
                        && preparacion.getFecha().equals(inicio),
                "La fecha del evento conserva su valor original");

        bitacora.registrar("Registro con fecha actual");
        comprobar(bitacora.getEventos().size() == 5, "Registro con fecha automática");
        System.out.println("Verificación de bitácora: todos los casos pasaron.");
    }

    private static void comprobar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError(caso);
        }
    }
}
