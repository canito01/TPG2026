package modelo.elementos;

import modelo.nave.Nave;
import modelo.nave.TipoNave;

/**
 * Elemento del universo que representa otra nave (Exploradora, Carguera o de Combate)
 * con la que la nave principal puede coincidir en una coordenada.
 *
 * El encuentro se resuelve exclusivamente por el tipo de cada nave mediante
 * Double Dispatch (delegando en {@link Nave#enfrentarseCon(Nave)}).
 * Se construye a través de {@link modelo.fabrica.NaveFactory#crearNaveEnemiga(TipoNave, String)}.
 */
public class NaveEnemiga implements ElementoDelUniverso {

    public static final String NOMBRE = "NaveEnemiga";

    private final Nave nave;
    private ResultadoEncuentro ultimoResultado;

    /**
     * Precondición: {@code nave != null}.
     */
    public NaveEnemiga(Nave nave) {
        if (nave == null) {
            throw new IllegalArgumentException("La nave enemiga no puede ser nula");
        }
        this.nave = nave;
    }

    public Nave getNave() {
        return nave;
    }

    public TipoNave getTipo() {
        return nave.getTipo();
    }

    public ResultadoEncuentro getUltimoResultado() {
        return ultimoResultado;
    }

    /**
     * Resuelve el encuentro contra la nave propia mediante Double Dispatch.
     */
    public ResultadoEncuentro resolverEncuentro(Nave navePropia) {
        if (navePropia == null) {
            throw new IllegalArgumentException("La nave propia no puede ser nula");
        }
        this.ultimoResultado = navePropia.enfrentarseCon(this.nave);
        return this.ultimoResultado;
    }

    @Override
    public String getNombre() {
        return NOMBRE;
    }

    @Override
    public String interactuarCon(Nave navePropia) {
        ResultadoEncuentro resultado = resolverEncuentro(navePropia);
        return "Encuentro con nave enemiga (" + nave.getTipo() + " '" + nave.getId()
                + "') contra nave propia (" + navePropia.getTipo() + "): " + resultado;
    }
}
