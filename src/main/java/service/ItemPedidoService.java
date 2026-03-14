package service;

import dao.CategoriaDAO;
import dao.ItemPedidoDAO;
import model.ItemPedido;
import model.Pedido;
import model.Produto;
import model.Usuario;
import dao.ProdutoDAO;
import dao.UsuarioDAO;
import dao.PedidoDAO;
import spark.Request;
import spark.Response;

import java.util.List;

public class ItemPedidoService extends BaseService<ItemPedido> {

    private PedidoDAO pedidoDao;
    private ProdutoDAO produtoDao;

    private ItemPedidoDAO itemPedidoDAO;

    public ItemPedidoService() {
        // Instancia o ProdutoDAO e manda lá pro BaseService cuidar do CRUD básico
        super(new ItemPedidoDAO());
        this.pedidoDao = new PedidoDAO();
        this.produtoDao = new ProdutoDAO();
    }

    @Override
    protected Class<ItemPedido> getModelClass() {
        return ItemPedido.class; // Ensina o Gson a transformar o JSON da requisição na classe certa
    }

    protected void onBeforeInsert(ItemPedido obj) throws Exception {
        obj.validar();

        Produto p = produtoDao.get(obj.getIdProduto());

        obj.setPrecoCongelado(p.getPreco());

        if (p.getQuantidadeEstoque() < obj.getQuantidade()) {
            throw new Exception(
                    "Estoque insuficiente. Temos apenas " + p.getQuantidadeEstoque() + " unidades do " + p.getNome());
        }

        p.setQuantidadeEstoque(p.getQuantidadeEstoque() - obj.getQuantidade());
        produtoDao.update(p);
    }

    // Adicionar item ao pedido
    public Object adicionarItem(Request req, Response res) {

        try {

            // 1. Descobre quem está logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            // 2. Busca o usuário no banco
            UsuarioDAO userDAO = new UsuarioDAO();
            Usuario usuario = userDAO.getByLogin(donoLogin);

            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Converte JSON da requisição para objeto
            ItemPedido item = gson.fromJson(req.body(), getModelClass());

            if (item == null) {
                res.status(400);
                return "{\"erro\": \"JSON inválido.\"}";
            }

            // 4. Busca o pedido
            Pedido pedidoAlvo = pedidoDao.get(item.getIdPedido());

            if (pedidoAlvo == null) {
                res.status(404);
                return "{\"erro\": \"Pedido não encontrado.\"}";
            }

            // 5. Verifica se o pedido pertence ao usuário logado
            if (pedidoAlvo.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado: você não pode modificar o pedido de outro usuário.\"}";
            }

            // 6. Lógica antes da inserção
            onBeforeInsert(item);

            // 7. Insere no banco
            dao.insert(item);

            // 8. Retorno de sucesso
            res.status(201);
            return gson.toJson(item);

        } catch (Exception e) {

            e.printStackTrace();

            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    public Object getItensPedidoSeguro(Request req, Response res) {

        try {

            // 1. Descobrir usuário logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            // 2. Buscar usuário
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Pegar ID do pedido
            int idPedido = Integer.parseInt(req.params(":idPedido"));

            // 4. Buscar pedido
            Pedido pedido = pedidoDao.get(idPedido);

            if (pedido == null) {
                res.status(404);
                return "{\"erro\": \"Pedido não encontrado.\"}";
            }

            // 5. Verificar se o pedido pertence ao usuário
            if (pedido.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado.\"}";
            }

            // 6. Buscar itens do pedido
            List<ItemPedido> itens = itemPedidoDAO.getItensByPedido(idPedido);

            res.status(200);
            return gson.toJson(itens);

        } catch (Exception e) {

            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    // // Buscar item
    // public ItemPedido buscarItem(int id) {

    // return itemPedidoDAO.get(id);
    // }

    // // Atualizar item
    // public boolean atualizarItem(ItemPedido item) throws Exception {

    // item.validar();

    // return itemPedidoDAO.update(item);
    // }

    // // Remover item
    // public boolean removerItem(int id) {

    // return itemPedidoDAO.delete(id);
    // }

    // Listar todos os itens
    public List<ItemPedido> listarTodos() {

        return itemPedidoDAO.getAll();
    }

    // Itens de um pedido
    public List<ItemPedido> listarItensPedido(int idPedido) {

        return itemPedidoDAO.getItensByPedido(idPedido);
    }

    // Itens de um produto
    public List<ItemPedido> listarItensProduto(int idProduto) {

        return itemPedidoDAO.getItensByProduto(idProduto);
    }
}