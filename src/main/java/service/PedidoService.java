package service;

import java.util.List;

import dao.PedidoDAO;
import dao.ProdutoDAO; // Import necessário
import dao.UsuarioDAO;
import model.ItemPedido;
import model.Pedido; // Import necessário para percorrer os itens
import model.Produto;    // Import necessário
import model.Usuario;
import spark.Request;
import spark.Response;

public class PedidoService extends BaseService<Pedido> {

    public PedidoService() {
        super(new PedidoDAO());
    }

    @Override
    protected Class<Pedido> getModelClass() {
        return Pedido.class;
    }

    @Override
    protected void onBeforeInsert(Pedido obj) throws Exception {
        obj.validar(); // Isso vai travar se a lista de itens estiver vazia
        ProdutoDAO produtoDAO = new ProdutoDAO();

        // Verificação
        for (ItemPedido item : obj.getItens()) {
            Produto p = produtoDAO.get(item.getIdProduto());
            if (p == null) {
                throw new Exception("Produto inexistente.");
            }

            if (item.getQuantidade() > p.getQuantidadeEstoque()) {
                throw new Exception("Estoque insuficiente para " + p.getNome());
            }
        }

        // Baixa definitiva
        for (ItemPedido item : obj.getItens()) {
            Produto p = produtoDAO.get(item.getIdProduto());
            p.setQuantidadeEstoque(p.getQuantidadeEstoque() - item.getQuantidade());
            boolean atualizou = produtoDAO.update(p);
            if (!atualizou) {
                throw new Exception("Erro ao salvar estoque no arquivo.");
            }
        }
    }

    @Override
    protected void onBeforeUpdate(Pedido obj) throws Exception {
        obj.validar();
    }

    @Override
    public Object insert(Request req, Response res) {
        try {
            // Autenticação (você já tem)
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            Usuario u = new UsuarioDAO().getByLogin(donoLogin);
            Pedido pedido = gson.fromJson(req.body(), getModelClass());

            pedido.setIdUsuario(u.getId());
            pedido.setDataCompra(System.currentTimeMillis());

            // CHAMADA CRÍTICA: Se o estoque falhar, o código pula direto para o 'catch'
            onBeforeInsert(pedido);

            // SÓ GRAVA NO PEDIDO.DB SE O ONBEFOREINSERT PASSOU
            this.dao.insert(pedido);

            res.status(201);
            return gson.toJson(pedido);

        } catch (Exception e) {
            // Se cair aqui, o erro foi o estoque insuficiente ou produto inexistente
            System.err.println("ERRO NO PEDIDO: " + e.getMessage());
            res.status(400); // Bad Request
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    @Override
    public Object get(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            int idPedido = Integer.parseInt(req.params(":id"));
            Pedido pedido = this.dao.get(idPedido);

            if (pedido == null) {
                res.status(404);
                return "{\"erro\": \"Pedido não encontrado.\"}";
            }

            if (pedido.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado.\"}";
            }

            res.status(200);
            return gson.toJson(pedido);

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    // --- RELATÓRIOS PARA A GESTÃO ---
    public Object getPorStatus(Request req, Response res) {
        try {
            String status = req.params(":status");
            PedidoDAO pedDao = (PedidoDAO) this.dao;
            res.status(200);
            return gson.toJson(pedDao.getPedidosByStatus(status));
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object getAteData(Request req, Response res) {
        try {
            long data = Long.parseLong(req.params(":dataLimite"));
            PedidoDAO pedDao = (PedidoDAO) this.dao;
            res.status(200);
            return gson.toJson(pedDao.getPedidosAteData(data));
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object getAteValor(Request req, Response res) {
        try {
            float valor = Float.parseFloat(req.params(":valorMaximo"));
            PedidoDAO pedDao = (PedidoDAO) this.dao;
            res.status(200);
            return gson.toJson(pedDao.getPedidosAteValor(valor));
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object getMeusPedidos(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            UsuarioDAO userDAO = new UsuarioDAO();
            Usuario u = userDAO.getByLogin(donoLogin);

            PedidoDAO pedDao = (PedidoDAO) this.dao;
            List<Pedido> meusPedidos = pedDao.getPedidosByUsuario(u.getId());

            res.status(200);
            return gson.toJson(meusPedidos);

        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
    public Object atualizarStatus(Request req, Response res) {
    try {
        int id = Integer.parseInt(req.params(":id"));
        // O status virá no corpo da requisição (ex: {"status": "ENVIADO"})
        Pedido dadosNovos = gson.fromJson(req.body(), Pedido.class);
        
        PedidoDAO daoPedido = (PedidoDAO) this.dao;
        Pedido pedidoExistente = daoPedido.get(id);

        if (pedidoExistente != null) {
            pedidoExistente.setStatus(dadosNovos.getStatus());
            daoPedido.update(pedidoExistente); // O update do FileDAO grava no .db
            res.status(200);
            return "{\"mensagem\": \"Status atualizado com sucesso!\"}";
        } else {
            res.status(404);
            return "{\"erro\": \"Pedido não encontrado.\"}";
        }
    } catch (Exception e) {
        res.status(500);
        return "{\"erro\": \"" + e.getMessage() + "\"}";
    }
}
}
