package service;

import dao.ItemPedidoDAO;
import dao.PedidoDAO;
import dao.ProdutoDAO;
import dao.UsuarioDAO;
import model.Favorito;
import model.ItemPedido;
import model.Pedido;
import model.Produto;
import model.Usuario;
import spark.Request;
import spark.Response;

import java.util.List;

public class PedidoService extends BaseService<Pedido> {

    private PedidoDAO pedidoDAO;
    private ProdutoDAO produtoDAO;

    public PedidoService() {
        // Instancia o ProdutoDAO e manda lá pro BaseService cuidar do CRUD básico
        super(new PedidoDAO());
        this.pedidoDAO = new PedidoDAO();
        this.produtoDAO = new ProdutoDAO();
    }

    @Override
    protected Class<Pedido> getModelClass() {
        return Pedido.class;
    }

    protected void onBeforeInsert(Pedido obj) throws Exception {
        obj.validar();
    }

    protected void onBeforeUpdate(Pedido obj) throws Exception {
        obj.validar();
    }

    // Criar pedido
    public Object criarPedido(Request req, Response res) {

        try {

            // 1. Descobrir usuário logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida. Faça login para comprar.\"}";
            }

            // 2. Buscar usuário
            UsuarioDAO userDAO = new UsuarioDAO();
            Usuario user = userDAO.getByLogin(donoLogin);

            if (user == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Converter JSON
            Pedido pedido = gson.fromJson(req.body(), getModelClass());

            if (pedido == null) {
                res.status(400);
                return "{\"erro\": \"JSON inválido.\"}";
            }

            // 4. Garantir que o pedido pertence ao usuário logado
            pedido.setIdUsuario(user.getId());

            // 5. Definir data
            pedido.setDataCompra(System.currentTimeMillis());

            // 6. Validar
            onBeforeInsert(pedido);

            // 7. Inserir
            dao.insert(pedido);

            res.status(201);
            return gson.toJson(pedido);

        } catch (Exception e) {

            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    // // Buscar pedido por id
    // public Pedido buscarPedido(int id) throws Exception {
    // return pedidoDAO.get(id);
    // }

    // // Atualizar pedido
    // public boolean atualizarPedido(Pedido pedido) throws Exception {

    // pedido.validar();
    // return pedidoDAO.update(pedido);
    // }

    // // Remover pedido
    // public boolean removerPedido(int id) throws Exception {
    // return pedidoDAO.delete(id);
    // }

    // Listar todos pedidos

    public Object getPedidoSeguro(Request req, Response res) {

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
            int idPedido = Integer.parseInt(req.params(":id"));

            // 4. Buscar pedido
            Pedido pedido = pedidoDAO.get(idPedido);

            if (pedido == null) {
                res.status(404);
                return "{\"erro\": \"Pedido não encontrado.\"}";
            }

            // 5. Verificar dono do pedido
            if (pedido.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado.\"}";
            }

            // 6. Retornar pedido
            res.status(200);
            return gson.toJson(pedido);

        } catch (Exception e) {

            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    public List<Pedido> listarPedidos() {
        return pedidoDAO.getAll();
    }

    // Listar pedidos de um usuário
    public List<Pedido> listarPedidosUsuario(int idUsuario) {
        return pedidoDAO.getPedidosByUsuario(idUsuario);
    }

    // Listar pedidos por status
    public List<Pedido> listarPedidosStatus(String status) {
        return pedidoDAO.getPedidosByStatus(status);
    }

    // Listar pedidos até uma data
    public List<Pedido> listarPedidosAteData(long data) {
        return pedidoDAO.getPedidosAteData(data);
    }

    // Listar pedidos até um valor
    public List<Pedido> listarPedidosAteValor(float valor) {
        return pedidoDAO.getPedidosAteValor(valor);
    }
}