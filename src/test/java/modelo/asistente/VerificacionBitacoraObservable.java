package modelo.asistente;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import modelo.fabrica.NaveFactory;
import modelo.nave.Nave;
import modelo.nave.TipoNave;
import util.observer.Observador;

/** Verifica Observer sin Swing ni bibliotecas externas. */
public class VerificacionBitacoraObservable {

    public static void main(String[] args) throws InterruptedException {
        verificarAvisosYConsultas();
        verificarRechazosSinAvisos();
        verificarCambiosDeSuscripcion();
        verificarSuscripcionDesdeAsistente();
        verificarAvisosDeRecursos();
        verificarRegistrosConcurrentes();
        System.out.println("Verificacion de bitacora observable: todos los casos pasaron.");
    }

    private static void verificarAvisosYConsultas() {
        Bitacora bitacora = new Bitacora();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 20, 10, 0);
        bitacora.registrar(new Evento(inicio, "Anterior a la suscripcion"));

        List<String> mensajes = new ArrayList<>();
        List<Integer> cantidadesAlNotificar = new ArrayList<>();
        Observador primero = mensaje -> {
            mensajes.add(mensaje);
            cantidadesAlNotificar.add(bitacora.getEventos().size());
        };
        List<String> segundaVista = new ArrayList<>();
        Observador segundo = segundaVista::add;
        bitacora.agregarObservador(primero);
        bitacora.agregarObservador(primero);
        bitacora.agregarObservador(segundo);
        comprobar(mensajes.isEmpty(), "La suscripcion no repite eventos anteriores");

        Evento salto = new Evento(inicio.plusMinutes(2), "Salto");
        Evento preparacion = new Evento(inicio.plusMinutes(1), "Preparacion");
        bitacora.registrar(salto);
        bitacora.registrar(preparacion);
        comprobar(mensajes.equals(List.of("Salto", "Preparacion")),
                "Un aviso por registro, incluso con suscripcion duplicada");
        comprobar(segundaVista.equals(mensajes), "Ambos observadores reciben los avisos");
        comprobar(cantidadesAlNotificar.equals(List.of(2, 3)),
                "Los eventos ya estan guardados al notificar");
        comprobar(bitacora.getEventos().get(1) == preparacion
                        && bitacora.getEventos().get(2) == salto,
                "La consulta sigue ordenada por fecha aunque los avisos sean de insercion");

        List<Evento> consulta = bitacora.getEventos();
        bitacora.quitarObservador(primero);
        bitacora.quitarObservador(primero);
        bitacora.registrar("Despues de quitar el primero");
        comprobar(mensajes.size() == 2 && segundaVista.size() == 3,
                "Desuscribir no afecta al otro observador");
        comprobar(consulta.size() == 3, "La consulta anterior conserva sus datos");
        try {
            consulta.clear();
            throw new AssertionError("La consulta permitio borrar eventos.");
        } catch (UnsupportedOperationException esperado) {
            comprobar(bitacora.getEventos().size() == 4, "La consulta sigue siendo de solo lectura");
        }
    }

    private static void verificarRechazosSinAvisos() {
        Bitacora bitacora = new Bitacora();
        List<String> mensajes = new ArrayList<>();
        bitacora.agregarObservador(mensajes::add);
        for (String descripcion : new String[] {null, "", "  "}) {
            try {
                bitacora.registrar(descripcion);
                throw new AssertionError("Se acepto una descripcion invalida.");
            } catch (IllegalArgumentException esperado) {
                comprobar(mensajes.isEmpty() && bitacora.getEventos().isEmpty(),
                        "Descripcion invalida sin registro ni aviso");
            }
        }
        try {
            bitacora.registrar((Evento) null);
            throw new AssertionError("Se acepto un evento nulo.");
        } catch (IllegalArgumentException esperado) {
            comprobar(mensajes.isEmpty(), "Evento nulo sin aviso");
        }
        try {
            bitacora.agregarObservador(null);
            throw new AssertionError("Se acepto un observador nulo.");
        } catch (IllegalArgumentException esperado) {
            bitacora.registrar("Valido");
            comprobar(mensajes.equals(List.of("Valido")), "El registro sigue funcionando tras rechazos");
        }
    }

    private static void verificarCambiosDeSuscripcion() {
        Bitacora bitacora = new Bitacora();
        List<String> mensajes = new ArrayList<>();
        Observador nuevo = mensaje -> mensajes.add("nuevo: " + mensaje);
        Observador temporal = new Observador() {
            @Override
            public void actualizar(String mensaje) {
                mensajes.add("temporal: " + mensaje);
                bitacora.quitarObservador(this);
                bitacora.agregarObservador(nuevo);
            }
        };
        bitacora.agregarObservador(temporal);
        bitacora.agregarObservador(mensaje -> mensajes.add("permanente: " + mensaje));
        bitacora.registrar("primero");
        bitacora.registrar("segundo");
        comprobar(mensajes.equals(List.of("temporal: primero", "permanente: primero",
                        "permanente: segundo", "nuevo: segundo")),
                "Cambiar suscripciones durante un aviso no altera sus destinatarios");
    }

    private static void verificarSuscripcionDesdeAsistente() {
        NaveFactory fabrica = new NaveFactory();
        AsistenteDeComando asistente = new AsistenteBase(
                fabrica.crear(TipoNave.EXPLORADORA, "OBS-1"));
        AsistenteDeComando otro = new AsistenteBase(
                fabrica.crear(TipoNave.EXPLORADORA, "OBS-2"));
        List<String> mensajes = new ArrayList<>();
        List<String> mensajesDeOtro = new ArrayList<>();
        List<Integer> energias = new ArrayList<>();
        Observador observador = mensaje -> {
            mensajes.add(mensaje);
            energias.add(asistente.consultarRecursos().getEnergia());
            List<Evento> eventos = asistente.consultarBitacora();
            comprobar(eventos.stream().anyMatch(evento -> evento.getDescripcion().equals(mensaje)),
                    "El aviso recibido por el asistente corresponde a un evento guardado");
        };

        asistente.registrarEvento("Antes de suscribir");
        asistente.agregarObservadorBitacora(observador);
        asistente.agregarObservadorBitacora(observador);
        otro.agregarObservadorBitacora(mensajesDeOtro::add);
        comprobar(mensajes.isEmpty(), "El asistente no repite avisos anteriores");

        Resultado carga = asistente.cargarEnergia(10);
        comprobar(carga.esExito() && mensajes.equals(List.of(carga.getMensaje()))
                        && energias.equals(List.of(90)),
                "La carga notifica una vez y permite consultar la energia actualizada");
        Resultado rechazo = asistente.cargarEnergia(100);
        comprobar(!rechazo.esExito() && mensajes.size() == 2
                        && mensajes.get(1).equals(rechazo.getMensaje()) && energias.get(1) == 90,
                "El rechazo se notifica y conserva los recursos");
        Resultado preparacion = asistente.prepararSalto();
        comprobar(preparacion.esExito() && mensajes.size() == 3
                        && mensajes.get(2).equals(preparacion.getMensaje())
                        && asistente.consultarEstado().equals("Preparando salto"),
                "Las operaciones del motor tambien notifican mediante el asistente");
        asistente.registrarEvento("Evento de mision");
        comprobar(mensajes.size() == 4 && mensajes.get(3).equals("Evento de mision"),
                "El registro explicito tambien llega al observador");
        otro.registrarEvento("Evento de otra nave");
        comprobar(mensajes.size() == 4 && mensajesDeOtro.equals(List.of("Evento de otra nave")),
                "Cada asistente notifica solo los eventos de su propia bitacora");

        try {
            asistente.agregarObservadorBitacora(null);
            throw new AssertionError("El asistente acepto un observador nulo.");
        } catch (IllegalArgumentException esperado) {
            comprobar(mensajes.size() == 4, "Una suscripcion invalida no genera avisos");
        }
        asistente.quitarObservadorBitacora(observador);
        asistente.quitarObservadorBitacora(observador);
        asistente.registrarEvento("Despues de desuscribir");
        comprobar(mensajes.size() == 4 && asistente.consultarBitacora().size() == 6,
                "Desuscribir detiene los avisos y permite seguir registrando");
    }

    private static void verificarAvisosDeRecursos() {
        Nave nave = new NaveFactory().crear(TipoNave.EXPLORADORA, "OBS-RECURSOS");
        AsistenteDeComando asistente = new AsistenteBase(nave);
        List<ResumenRecursos> avisos = new ArrayList<>();
        Observador observador = mensaje -> avisos.add(asistente.consultarRecursos());
        asistente.agregarObservadorRecursos(observador);

        comprobar(asistente.consumir(4, 5, 10).esExito() && avisos.size() == 1,
                "El consumo combinado envia un aviso");
        ResumenRecursos posterior = avisos.get(0);
        comprobar(posterior.getCombustible() == 56 && posterior.getEnergia() == 75
                        && posterior.getDesgaste() == 10,
                "El aviso permite consultar los tres cambios completos");

        comprobar(!asistente.consumir(1, 1, 91).esExito(), "El exceso de desgaste se rechaza");
        ResumenRecursos trasRechazo = asistente.consultarRecursos();
        comprobar(trasRechazo.getCombustible() == 56 && trasRechazo.getEnergia() == 75
                        && trasRechazo.getDesgaste() == 10 && avisos.size() == 1,
                "El rechazo conserva los tres valores y no notifica cambios");
        comprobar(asistente.consumir(0, 0, 0).esExito() && avisos.size() == 1,
                "Un consumo sin cambios no genera avisos de recursos");

        nave.getRecursos().cargarEnergia(3);
        comprobar(avisos.size() == 2 && avisos.get(1).getEnergia() == 78,
                "Un cambio directo al recurso tambien llega por la suscripcion");
        asistente.quitarObservadorRecursos(observador);
        asistente.realizarMantenimiento();
        comprobar(avisos.size() == 2 && asistente.consultarRecursos().getDesgaste() == 0,
                "Tras quitar el observador los recursos siguen actualizandose");
    }

    private static void verificarRegistrosConcurrentes() throws InterruptedException {
        Bitacora bitacora = new Bitacora();
        AtomicInteger avisos = new AtomicInteger();
        AtomicReference<Throwable> fallo = new AtomicReference<>();
        bitacora.agregarObservador(mensaje -> {
            bitacora.getEventos();
            avisos.incrementAndGet();
        });
        CountDownLatch inicio = new CountDownLatch(1);
        Thread[] hilos = new Thread[4];
        for (int i = 0; i < hilos.length; i++) {
            final int numero = i;
            hilos[i] = new Thread(() -> {
                try {
                    inicio.await();
                    for (int j = 0; j < 50; j++) {
                        bitacora.registrar("Hilo " + numero + " - evento " + j);
                    }
                } catch (Throwable error) {
                    fallo.compareAndSet(null, error);
                }
            });
            hilos[i].setDaemon(true);
            hilos[i].start();
        }
        inicio.countDown();
        for (Thread hilo : hilos) {
            hilo.join(5000);
            comprobar(!hilo.isAlive(), "Los registros terminan sin bloqueo");
        }
        if (fallo.get() != null) {
            throw new AssertionError("Fallo en registro concurrente", fallo.get());
        }
        List<Evento> eventos = bitacora.getEventos();
        comprobar(eventos.size() == 200 && avisos.get() == 200,
                "Cuatro hilos conservan los 200 eventos y sus 200 avisos");
        comprobar(eventos.stream().map(Evento::getDescripcion).distinct().count() == 200,
                "No se pierden ni duplican registros concurrentes");
        for (int i = 1; i < eventos.size(); i++) {
            comprobar(!eventos.get(i - 1).getFecha().isAfter(eventos.get(i).getFecha()),
                    "El orden temporal se conserva con varios hilos");
        }
    }

    private static void comprobar(boolean condicion, String caso) {
        if (!condicion) {
            throw new AssertionError(caso);
        }
    }
}
