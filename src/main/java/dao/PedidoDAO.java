package dao;

import java.util.ArrayList;
import java.util.List;

import model.ItemPedido;
import model.Pedido;

public class PedidoDAO extends FileDAO<Pedido> {

    public PedidoDAO() {
        super("Pedido.db", Pedido.class);
    }

    @Override
    public int insert(Pedido pedido) throws Exception { // Adicionado 'throws Exception' para sumir o erro
        // 1. O seu FileDAO já faz tudo: gera o ID, grava e retorna o ID!
        int idGerado = super.insert(pedido);

        // 2. Gravamos os itens vinculados a esse ID no arquivo itensPedido.db
        if (pedido.getItens() != null && !pedido.getItens().isEmpty()) {
            ItemPedidoDAO daoItens = new ItemPedidoDAO();

            for (model.ItemPedido item : pedido.getItens()) {
                item.setIdPedido(idGerado); // Vincula ao pedido pai
                daoItens.insert(item);      // Salva no arquivo itensPedido.db
            }
        }

        return idGerado;
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

    @Override
    public boolean delete(int idPedido) {
        ItemPedidoDAO itemDao = new ItemPedidoDAO();
        List<ItemPedido> itensDoPedido = itemDao.getItensByPedido(idPedido);

        for (ItemPedido item : itensDoPedido) {
            itemDao.delete(item.getId()); // Apaga os filhos
        }

        // Apaga o Pedido Pai
        return super.delete(idPedido);
    }
}
