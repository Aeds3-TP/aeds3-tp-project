package service;

import dao.PedidoDAO;
import dao.UsuarioDAO;
import model.Pedido;
import model.Usuario;
import spark.Request;
import spark.Response;

import java.util.List;

public class PedidoService extends BaseService<Pedido> {

    public PedidoService() {
        // Apenas o super! O BaseService já cria a variável 'this.dao'
        super(new PedidoDAO());
    }

    @Override
    protected Class<Pedido> getModelClass() {
        return Pedido.class;
    }

    @Override
    protected void onBeforeInsert(Pedido obj) throws Exception {
        obj.validar();
    }

    @Override
    protected void onBeforeUpdate(Pedido obj) throws Exception {
        obj.validar();
    }

    
    @Override
    public Object insert(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida. Faça login para comprar.\"}";
            }

            UsuarioDAO userDAO = new UsuarioDAO();
            Usuario user = userDAO.getByLogin(donoLogin);
            if (user == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            Pedido pedido = gson.fromJson(req.body(), getModelClass());
            if (pedido == null) {
                res.status(400);
                return "{\"erro\": \"JSON inválido.\"}";
            }

            pedido.setIdUsuario(user.getId());
            pedido.setDataCompra(System.currentTimeMillis());

            onBeforeInsert(pedido);
            this.dao.insert(pedido); // Usa o 'dao' do BaseService

            res.status(201);
            return gson.toJson(pedido);

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
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
            Pedido pedido = this.dao.get(idPedido); // Usa o 'dao' do BaseService

            if (pedido == null) {
                res.status(404);
                return "{\"erro\": \"Pedido não encontrado.\"}";
            }

            // Regra de segurança
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
            res.status(500); return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object getAteData(Request req, Response res) {
        try {
            long data = Long.parseLong(req.params(":dataLimite"));
            PedidoDAO pedDao = (PedidoDAO) this.dao;
            res.status(200);
            return gson.toJson(pedDao.getPedidosAteData(data));
        } catch (Exception e) {
            res.status(500); return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object getAteValor(Request req, Response res) {
        try {
            float valor = Float.parseFloat(req.params(":valorMaximo"));
            PedidoDAO pedDao = (PedidoDAO) this.dao;
            res.status(200);
            return gson.toJson(pedDao.getPedidosAteValor(valor));
        } catch (Exception e) {
            res.status(500); return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    // MÉTODO NOVO: O que faltava para listar "Minhas Compras"
    public Object getMeusPedidos(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            UsuarioDAO userDAO = new UsuarioDAO();
            Usuario u = userDAO.getByLogin(donoLogin);

            // Faz o cast para acessar métodos específicos do PedidoDAO
            PedidoDAO pedDao = (PedidoDAO) this.dao;
            List<Pedido> meusPedidos = pedDao.getPedidosByUsuario(u.getId());

            res.status(200);
            return gson.toJson(meusPedidos);

        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}