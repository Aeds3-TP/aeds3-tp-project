package service;

import java.util.List;

import dao.ItemPedidoDAO;
import dao.PedidoDAO;
import dao.ProdutoDAO;
import dao.UsuarioDAO;
import model.ItemPedido;
import model.Pedido;
import model.Produto;
import model.Usuario;
import spark.Request;
import spark.Response;

public class ItemPedidoService extends BaseService<ItemPedido> {

    private PedidoDAO pedidoDao;
    private ProdutoDAO produtoDao;
    // APAGADA a variável itemPedidoDAO duplicada (usaremos this.dao)

    public ItemPedidoService() {
        super(new ItemPedidoDAO());
        this.pedidoDao = new PedidoDAO();
        this.produtoDao = new ProdutoDAO();
    }

    @Override
    protected Class<ItemPedido> getModelClass() {
        return ItemPedido.class;
    }

    @Override
    protected void onBeforeInsert(ItemPedido obj) throws Exception {
        obj.validar();

        // CORREÇÃO: Verifica se o produto existe antes de tentar pegar o preço!
        Produto p = produtoDao.get(obj.getIdProduto());
        if (p == null) {
            throw new Exception("Produto inexistente. Não é possível adicionar ao pedido.");
        }

        obj.setPrecoCongelado(p.getPreco());

        if (p.getQuantidadeEstoque() < obj.getQuantidade()) {
            throw new Exception("Estoque insuficiente. Temos apenas " + p.getQuantidadeEstoque() + " unidades do " + p.getNome());
        }

        // Dá baixa no estoque
        p.setQuantidadeEstoque(p.getQuantidadeEstoque() - obj.getQuantidade());
        produtoDao.update(p);
    }
    
    // Bloqueia a alteração de itens de pedido (Segurança)
    @Override
    protected void onBeforeUpdate(ItemPedido obj) throws Exception {
        throw new Exception("Não é permitido alterar um item de pedido já finalizado.");
    }

    // CORREÇÃO: Renomeado para insert e com a tag @Override
    @Override
    public Object insert(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            UsuarioDAO userDAO = new UsuarioDAO();
            Usuario usuario = userDAO.getByLogin(donoLogin);
            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            ItemPedido item = gson.fromJson(req.body(), getModelClass());
            if (item == null) {
                res.status(400);
                return "{\"erro\": \"JSON inválido.\"}";
            }

            Pedido pedidoAlvo = pedidoDao.get(item.getIdPedido());
            if (pedidoAlvo == null) {
                res.status(404);
                return "{\"erro\": \"Pedido não encontrado.\"}";
            }

            if (pedidoAlvo.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado: você não pode modificar o pedido de outro usuário.\"}";
            }

            onBeforeInsert(item);
            this.dao.insert(item); // Usa o dao do BaseService

            res.status(201);
            return gson.toJson(item);

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}"; // Devolve a msg de erro real para o Front-end ler
        }
    }

    // Mantido o nome original dele, é um método customizado excelente
    public Object getItensPedidoSeguro(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);
            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            int idPedido = Integer.parseInt(req.params(":idPedido"));
            Pedido pedido = pedidoDao.get(idPedido);

            if (pedido == null) {
                res.status(404);
                return "{\"erro\": \"Pedido não encontrado.\"}";
            }

            if (pedido.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado.\"}";
            }

            // CORREÇÃO FATAL: Usando 'this.dao' convertido, evitando o NullPointerException!
            ItemPedidoDAO daoItens = (ItemPedidoDAO) this.dao;
            List<ItemPedido> itens = daoItens.getItensByPedido(idPedido);

            res.status(200);
            return gson.toJson(itens);

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }
    
    // --- RELATÓRIO PARA A GESTÃO ---
    public Object getItensPorProduto(Request req, Response res) {
        try {
            int idProduto = Integer.parseInt(req.params(":idProduto"));
            ItemPedidoDAO itemDao = (ItemPedidoDAO) this.dao;
            res.status(200);
            return gson.toJson(itemDao.getItensByProduto(idProduto));
        } catch (Exception e) {
            res.status(500); return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}