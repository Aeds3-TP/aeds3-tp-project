package dao;

import model.Pedido;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO extends FileDAO<Pedido> {

    public PedidoDAO() {
        super("Pedido.db", Pedido.class);
    }

    // Buscar pedidos de um usuário
    public List<Pedido> getPedidosByUsuario(int idUsuario) {

        List<Pedido> lista = new ArrayList<>();
        List<Pedido> todos = super.getAll();

        for (Pedido p : todos) {
            if (p.getIdUsuario() == idUsuario) {
                lista.add(p);
            }
        }

        return lista;
    }

    // Buscar pedidos por status
    public List<Pedido> getPedidosByStatus(String status) {

        List<Pedido> lista = new ArrayList<>();
        List<Pedido> todos = super.getAll();

        for (Pedido p : todos) {
            if (p.getStatus().equalsIgnoreCase(status)) {
                lista.add(p);
            }
        }

        return lista;
    }

    // Buscar pedidos feitos até uma data
    public List<Pedido> getPedidosAteData(long dataLimite) {

        List<Pedido> lista = new ArrayList<>();
        List<Pedido> todos = super.getAll();

        for (Pedido p : todos) {
            if (p.getDataCompra() <= dataLimite) {
                lista.add(p);
            }
        }

        return lista;
    }

    // Buscar pedidos por valor máximo
    public List<Pedido> getPedidosAteValor(float valorMaximo) {

        List<Pedido> lista = new ArrayList<>();
        List<Pedido> todos = super.getAll();

        for (Pedido p : todos) {
            if (p.getValorTotal() <= valorMaximo) {
                lista.add(p);
            }
        }

        return lista;
    }
}