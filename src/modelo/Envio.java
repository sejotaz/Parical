package modelo;

public abstract class Envio {
    private String codigo;
    private String cliente;
    private double peso;
    private double distancia;

    public Envio(String codigo, String cliente, double peso, double distancia) {
        this.codigo = codigo;
        this.cliente = cliente;
        this.peso = peso;
        this.distancia = distancia;
    }

    public String getCodigo()       { return codigo; }
    public String getCliente()      { return cliente; }
    public double getPeso()         { return peso; }
    public double getDistancia()    { return distancia; }

    public abstract double calcularTarifa();

    public abstract String getTipo();
}
