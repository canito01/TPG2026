package modelo.concurrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/** Comprueba las esperas del monitor y el intercambio entre varios hilos. */
public final class VerificacionPlataformaDeCarga {
    public static void main(String[] args) throws Exception {
        verificarCapacidad();
        verificarEsperaSiEstaVacia();
        verificarEsperaSiEstaLlena();
        verificarInterrupcion();
        verificarVariosHilos();
        verificarGeneradoraYNave();
        System.out.println("Plataforma de carga: todos los casos pasaron.");
    }

    private static void verificarCapacidad() {
        try {
            new PlataformaDeCarga(0);
            throw new AssertionError("Se aceptó una plataforma sin capacidad.");
        } catch (IllegalArgumentException esperada) {
            // Una plataforma debe poder guardar al menos una batería.
        }
    }

    private static void verificarEsperaSiEstaVacia() throws Exception {
        PlataformaDeCarga plataforma = new PlataformaDeCarga(1);
        FutureTask<Void> retiro = new FutureTask<>(() -> {
            plataforma.retirarBateria();
            return null;
        });
        Thread nave = iniciar(retiro);
        try {
            esperarBloqueo(nave);
            comprobar(plataforma.getCantidadBaterias() == 0, "La nave espera sin retirar.");
            // Otro aviso puede despertar a la nave aunque siga sin haber baterías.
            synchronized (plataforma) {
                plataforma.notifyAll();
            }
            esperarBloqueo(nave);
            comprobar(!retiro.isDone(), "La nave vuelve a esperar si sigue vacía.");
            plataforma.depositarBateria();
            retiro.get(3, TimeUnit.SECONDS);
            comprobar(plataforma.getCantidadBaterias() == 0, "La nave retiró la batería depositada.");
        } finally {
            nave.interrupt();
        }
    }

    private static void verificarEsperaSiEstaLlena() throws Exception {
        PlataformaDeCarga plataforma = new PlataformaDeCarga(1);
        plataforma.depositarBateria();
        FutureTask<Void> deposito = new FutureTask<>(() -> {
            plataforma.depositarBateria();
            return null;
        });
        Thread generadora = iniciar(deposito);
        try {
            esperarBloqueo(generadora);
            comprobar(plataforma.getCantidadBaterias() == 1, "La generadora espera sin exceder la capacidad.");
            plataforma.retirarBateria();
            deposito.get(3, TimeUnit.SECONDS);
            comprobar(plataforma.getCantidadBaterias() == 1, "La generadora ocupó el lugar liberado.");
        } finally {
            generadora.interrupt();
        }
    }

    private static void verificarInterrupcion() throws Exception {
        PlataformaDeCarga plataforma = new PlataformaDeCarga(1);
        FutureTask<Void> retiro = new FutureTask<>(() -> {
            plataforma.retirarBateria();
            return null;
        });
        Thread nave = iniciar(retiro);
        try {
            esperarBloqueo(nave);
            nave.interrupt();
            try {
                retiro.get(3, TimeUnit.SECONDS);
                throw new AssertionError("El retiro debía terminar por interrupción.");
            } catch (ExecutionException e) {
                comprobar(e.getCause() instanceof InterruptedException, "Se propaga la interrupción.");
            }
            comprobar(plataforma.getCantidadBaterias() == 0, "La interrupción no retira ninguna batería.");
        } finally {
            nave.interrupt();
        }
    }

    private static void verificarVariosHilos() throws Exception {
        int capacidad = 3;
        PlataformaDeCarga plataforma = new PlataformaDeCarga(capacidad);
        ExecutorService hilos = Executors.newFixedThreadPool(6);
        CountDownLatch inicio = new CountDownLatch(1);
        List<Future<Integer>> generadoras = new ArrayList<>();
        List<Future<Integer>> naves = new ArrayList<>();
        try {
            for (int i = 0; i < 3; i++) {
                generadoras.add(hilos.submit(() -> {
                    inicio.await();
                    int depositadas = 0;
                    while (depositadas < 100) {
                        plataforma.depositarBateria();
                        depositadas++;
                        comprobarLimites(plataforma, capacidad);
                    }
                    return depositadas;
                }));
                naves.add(hilos.submit(() -> {
                    inicio.await();
                    int retiradas = 0;
                    while (retiradas < 100) {
                        plataforma.retirarBateria();
                        retiradas++;
                        comprobarLimites(plataforma, capacidad);
                    }
                    return retiradas;
                }));
            }
            inicio.countDown();
            int depositadas = 0;
            int retiradas = 0;
            for (int i = 0; i < 3; i++) {
                depositadas += generadoras.get(i).get(5, TimeUnit.SECONDS);
                retiradas += naves.get(i).get(5, TimeUnit.SECONDS);
            }
            comprobar(depositadas == 300 && retiradas == 300 && plataforma.getCantidadBaterias() == 0,
                    "Los seis hilos intercambiaron 300 baterías y la plataforma quedó vacía.");
        } finally {
            hilos.shutdownNow();
        }
    }

    private static void verificarGeneradoraYNave() throws InterruptedException {
        PlataformaDeCarga plataforma = new PlataformaDeCarga(1);
        Thread generadora = new Thread(new GeneradoraDeEnergia(plataforma, 1), "Generadora 1");
        Thread nave = new Thread(new NaveConsumidora(plataforma, 60_000), "Nave 1");
        generadora.setDaemon(true);
        nave.setDaemon(true);
        try {
            generadora.start();
            esperarBloqueo(generadora);
            comprobar(plataforma.getCantidadBaterias() == 1,
                    "La generadora deposita una batería y espera con la siguiente.");

            nave.start();
            esperarEstado(nave, Thread.State.TIMED_WAITING);
            esperarBloqueo(generadora);
            comprobar(plataforma.getCantidadBaterias() == 1,
                    "Mientras la nave consume, la generadora vuelve a llenar la plataforma.");
        } finally {
            generadora.interrupt();
            nave.interrupt();
            generadora.join(3_000);
            nave.join(3_000);
        }
        comprobar(!generadora.isAlive() && !nave.isAlive(),
                "La interrupción termina los hilos tanto en wait() como en sleep().");
    }

    private static Thread iniciar(FutureTask<Void> operacion) {
        Thread hilo = new Thread(operacion);
        hilo.setDaemon(true);
        hilo.start();
        return hilo;
    }

    private static void esperarBloqueo(Thread hilo) throws InterruptedException {
        esperarEstado(hilo, Thread.State.WAITING);
    }

    private static void esperarEstado(Thread hilo, Thread.State estado) throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (hilo.getState() != estado) {
            comprobar(hilo.isAlive() && System.nanoTime() < limite,
                    "El hilo " + hilo.getName() + " debía alcanzar el estado " + estado + ".");
            Thread.sleep(1);
        }
    }

    private static void comprobarLimites(PlataformaDeCarga plataforma, int capacidad) {
        int cantidad = plataforma.getCantidadBaterias();
        comprobar(cantidad >= 0 && cantidad <= capacidad, "Cantidad de baterías fuera de los límites.");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
