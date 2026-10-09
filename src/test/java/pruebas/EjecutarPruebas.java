package pruebas;

/** Punto de entrada de las pruebas. Ejecutar con aserciones activadas (-ea). */
public class EjecutarPruebas {

    public static void main(String[] args) {
        PruebasMotorWarp.correrTodas();
        PruebasCentroDeControl.correrTodas();
        app.PruebasEscenarios.correrTodas();
        System.out.println();
        System.out.println("Resultado: " + Verificador.getPasaron() + " pasaron, "
                + Verificador.getFallaron() + " fallaron");
        if (Verificador.getFallaron() > 0) {
            System.exit(1);
        }
    }
}
