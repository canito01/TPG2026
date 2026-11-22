package vista;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

import controlador.TableroController;
import modelo.navegacion.Coordenada;
import modelo.tablero.ElementoDePrueba;
import modelo.tablero.TableroStub;
import pruebas.Verificador;

/**
 * Pruebas de la vista del tablero y su controlador. Estan en el paquete vista
 * para poder mirar la copia que dibuja la vista. Funcionan sin pantalla
 * (modo headless): no se abre ninguna ventana, se pinta sobre una imagen.
 */
public class PruebasVista {

    public static void correrTodas() {
        System.out.println("Vista del tablero");
        try { seActualizaAlCambiarElModelo(); Verificador.ok("la vista se actualiza (por invokeLater) cuando cambia el tablero"); }
        catch (Throwable t) { Verificador.fallo("la vista se actualiza", t); }
        try { dibujaLaCopia(); Verificador.ok("dibuja elementos, nave y destino a partir de la copia"); }
        catch (Throwable t) { Verificador.fallo("dibuja la copia", t); }
        try { desconectarDetieneLasActualizaciones(); Verificador.ok("al cerrar, la vista deja de observar el tablero"); }
        catch (Throwable t) { Verificador.fallo("desconectar", t); }
        try { variosHilosNoCorrompenLaVista(); Verificador.ok("cambios desde otro hilo: la vista termina con el estado final y sin perder elementos"); }
        catch (Throwable t) { Verificador.fallo("varios hilos y vista", t); }
        try { controladorDelegaEnElModelo(); Verificador.ok("el controlador mueve el tablero y la vista se entera por Observer"); }
        catch (Throwable t) { Verificador.fallo("controlador", t); }
    }

    /** Espera a que el hilo de Swing procese todo lo que quedo en su cola. */
    private static void esperarAlHiloDeSwing() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() { }
        });
    }

    private static void seActualizaAlCambiarElModelo() throws Exception {
        TableroStub t = new TableroStub(1L);
        TableroPanel panel = new TableroPanel(t);
        Verificador.verificar(panel.getInstantanea().getEtiquetaEn(3, 2) == null, "empieza vacia");
        t.colocar(new ElementoDePrueba("Asteroide"), new Coordenada(3, 2));
        esperarAlHiloDeSwing();
        Verificador.igual("Asteroide", panel.getInstantanea().getEtiquetaEn(3, 2), "la vista ve el elemento nuevo");
    }

    private static void dibujaLaCopia() throws Exception {
        TableroStub t = new TableroStub(1L);
        t.colocar(new ElementoDePrueba("Asteroide"), new Coordenada(3, 2));
        t.colocar(new ElementoDePrueba("EstacionDeCarga"), new Coordenada(5, 5));
        t.moverNave(new Coordenada(8, 8));
        t.marcarDestino(new Coordenada(10, 10));
        TableroPanel panel = new TableroPanel(t);
        esperarAlHiloDeSwing();
        int lado = 20 * TableroPanel.CELDA;
        panel.setSize(lado, lado);
        BufferedImage img = new BufferedImage(lado, lado, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        panel.paint(g);
        g.dispose();
        int c = TableroPanel.CELDA;
        Verificador.igual(TableroPanel.ASTEROIDE.getRGB(), img.getRGB(3 * c + 2, 2 * c + 2), "color del asteroide");
        Verificador.igual(TableroPanel.ESTACION.getRGB(), img.getRGB(5 * c + 2, 5 * c + 2), "color de la estacion");
        Verificador.igual(TableroPanel.VACIA.getRGB(), img.getRGB(12 * c + 2, 12 * c + 2), "casilla vacia");
        Color enNave = new Color(img.getRGB(8 * c + c / 2, 8 * c + c / 2));
        Verificador.verificar(enNave.getBlue() > 150 && enNave.getRed() < 80, "la nave se dibuja en azul");
        Color enDestino = new Color(img.getRGB(10 * c + 2, 10 * c + 2));
        Verificador.verificar(enDestino.getGreen() > 100 && enDestino.getRed() < 60, "el destino se marca en verde");
    }

    private static void desconectarDetieneLasActualizaciones() throws Exception {
        TableroStub t = new TableroStub(1L);
        TableroController controlador = new TableroController(t);
        TableroPanel panel = controlador.getVista();
        controlador.cerrar();
        t.colocar(new ElementoDePrueba("Asteroide"), new Coordenada(1, 1));
        esperarAlHiloDeSwing();
        Verificador.verificar(panel.getInstantanea().getEtiquetaEn(1, 1) == null, "ya no se entera de los cambios");
    }

    private static void variosHilosNoCorrompenLaVista() throws Exception {
        final TableroStub t = TableroStub.conElementosDePrueba(4L, 30, 5, 5);
        TableroPanel panel = new TableroPanel(t);
        Thread mision = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 200; i++) {
                    t.moverElementos();
                    t.moverNave(new Coordenada(i % 20, 3));
                }
            }
        });
        mision.start();
        mision.join();
        esperarAlHiloDeSwing();
        Verificador.igual(40, panel.getInstantanea().cantidadDeElementos(), "elementos en la vista");
        Verificador.igual(t.copiaParaDibujar().comoTexto(), panel.getInstantanea().comoTexto(),
                "la vista muestra el estado final del tablero");
    }

    private static void controladorDelegaEnElModelo() throws Exception {
        TableroStub t = TableroStub.conElementosDePrueba(6L, 20, 2, 2);
        TableroController controlador = new TableroController(t);
        String antes = controlador.getVista().getInstantanea().comoTexto();
        for (int i = 0; i < 5; i++) {
            controlador.moverTableroUnaVez();
        }
        esperarAlHiloDeSwing();
        String despues = controlador.getVista().getInstantanea().comoTexto();
        Verificador.verificar(!antes.equals(despues), "los elementos se movieron");
        Verificador.igual(t.copiaParaDibujar().comoTexto(), despues, "la vista muestra lo que hay en el modelo");
        try { new TableroController(null); Verificador.fallar("tablero nulo"); }
        catch (IllegalArgumentException e) { /* esperado */ }
    }
}
