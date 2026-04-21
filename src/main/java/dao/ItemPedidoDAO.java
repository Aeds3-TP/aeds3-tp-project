package dao;

import java.util.ArrayList;
import java.util.List;

import model.ItemPedido;

public class ItemPedidoDAO extends FileDAO<ItemPedido> {

    public ItemPedidoDAO() {
        super("itensPedido.db", ItemPedido.class);
    }

    // Buscar todos os itens de um pedido
    public List<ItemPedido> getItensByPedido(int idPedido) {

        List<ItemPedido> resultado = new ArrayList<>();

        for (ItemPedido item : getAll()) {
            if (item.getIdPedido() == idPedido) {
                resultado.add(item);
            }
        }

        return resultado;
    }

    // Buscar todos os itens de um produto
    public List<ItemPedido> getItensByProduto(int idProduto) {

        List<ItemPedido> resultado = new ArrayList<>();

        for (ItemPedido item : getAll()) {
            if (item.getIdProduto() == idProduto) {
                resultado.add(item);
            }
        }

        return resultado;
    }
}