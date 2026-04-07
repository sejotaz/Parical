package modelo;

import java.util.ArrayList;
import java.util.List;

public class Logistica {
    private List<Envio> envios = new ArrayList<>();

    public void agregar(Envio envio) {
        envios.add(envio);
    }

    public boolean retirar(String codigo) {
        return envios.removeIf(e -> e.getCodigo().equalsIgnoreCase(codigo));
    }

    public List<Envio> listar() {
        return new ArrayList<>(envios);
    }

    public boolean existeCodigo(String codigo) {
        return envios.stream().anyMatch(e -> e.getCodigo().equalsIgnoreCase(codigo));
    }
}
