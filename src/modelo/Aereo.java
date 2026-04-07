package modelo;

public class Aereo extends Envio {
    private static final double TARIFA_KM  = 5000;
    private static final double RECARGO_KG = 4000;

    public Aereo(String codigo, String cliente, double peso, double distancia) {
        super(codigo, cliente, peso, distancia);
    }

    @Override
    public double calcularTarifa() {
        return (getDistancia() * TARIFA_KM) + (getPeso() * RECARGO_KG);
    }

    @Override
    public String getTipo() { return "Aéreo"; }
}
