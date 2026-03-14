package service;

import dao.FavoritoDAO;
import dao.ItemPedidoDAO;
import dao.PedidoDAO;
import dao.ProdutoDAO;
import dao.UsuarioDAO;
import model.Favorito;
import model.ItemPedido;
import model.Produto;
import model.Usuario;
import spark.Request;
import spark.Response;

import java.util.List;

public class FavoritoService extends BaseService<Favorito> {

    private FavoritoDAO favoritoDAO;
    private UsuarioDAO usuarioDAO;

    public FavoritoService() {
        // Instancia o ProdutoDAO e manda lá pro BaseService cuidar do CRUD básico
        super(new FavoritoDAO());

        this.favoritoDAO = new FavoritoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    @Override
    protected Class<Favorito> getModelClass() {
        return Favorito.class;
    }

    protected void onBeforeInsert(Favorito obj) throws Exception {
        obj.validar();
    }

    protected void onBeforeUpdate(Favorito obj) throws Exception {
        obj.validar();
    }

    public Object adicionarFavoritoSeguro(Request req, Response res) {

        try {

            // 1. Descobrir quem está logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            // 2. Buscar usuário no banco
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Converter JSON para objeto Favorito
            Favorito favorito = gson.fromJson(req.body(), getModelClass());

            if (favorito == null) {
                res.status(400);
                return "{\"erro\": \"JSON inválido.\"}";
            }

            // 4. Verificar se o usuário do JSON é o mesmo do token
            if (favorito.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado: você não pode favoritar produtos para outro usuário.\"}";
            }

            // 5. Verificar se o produto existe
            ProdutoDAO produtoDAO = new ProdutoDAO();
            Produto produto = produtoDAO.get(favorito.getIdProduto());

            if (produto == null) {
                res.status(404);
                return "{\"erro\": \"Produto não encontrado.\"}";
            }

            // 6. Verificar se já está nos favoritos
            if (favoritoDAO.isFavorito(usuario.getId(), favorito.getIdProduto())) {
                res.status(409);
                return "{\"erro\": \"Produto já está nos favoritos.\"}";
            }

            // 7. Validar objeto
            favorito.validar();

            // 8. Inserir favorito
            favoritoDAO.insert(favorito);

            res.status(201);
            return gson.toJson(favorito);

        } catch (Exception e) {

            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    public Object insertSeguro(Request req, Response res) {

        try {

            // 1. Descobrir quem está logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            // 2. Buscar usuário
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Converter JSON
            Favorito favorito = gson.fromJson(req.body(), getModelClass());

            if (favorito == null) {
                res.status(400);
                return "{\"erro\": \"JSON inválido.\"}";
            }

            // 4. Garantir que o favorito pertence ao usuário logado
            if (favorito.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado.\"}";
            }

            // 5. Verificar duplicação
            if (favoritoDAO.isFavorito(usuario.getId(), favorito.getIdProduto())) {
                res.status(409);
                return "{\"erro\": \"Produto já está nos favoritos.\"}";
            }

            // 6. Validar
            onBeforeInsert(favorito);

            // 7. Inserir
            favoritoDAO.insert(favorito);

            res.status(201);
            return gson.toJson(favorito);

        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    public Object updateSeguro(Request req, Response res) {

        try {

            // 1. Descobrir usuário logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            // 2. Buscar usuário
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Converter JSON
            Favorito favorito = gson.fromJson(req.body(), getModelClass());

            if (favorito == null) {
                res.status(400);
                return "{\"erro\": \"JSON inválido.\"}";
            }

            // 4. Buscar favorito existente
            Favorito favoritoExistente = favoritoDAO.get(favorito.getId());

            if (favoritoExistente == null) {
                res.status(404);
                return "{\"erro\": \"Favorito não encontrado.\"}";
            }

            // 5. Verificar se pertence ao usuário logado
            if (favoritoExistente.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado: você não pode alterar favoritos de outro usuário.\"}";
            }

            // 6. Validar
            onBeforeUpdate(favorito);

            // 7. Atualizar
            favoritoDAO.update(favorito);

            res.status(200);
            return gson.toJson(favorito);

        } catch (Exception e) {

            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    public Object deleteSeguro(Request req, Response res) {

        try {

            // 1. Descobrir quem está logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            // 2. Buscar usuário
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Pegar ID do favorito pela URL
            int idFavorito = Integer.parseInt(req.params(":id"));

            // 4. Buscar favorito
            Favorito favorito = favoritoDAO.get(idFavorito);

            if (favorito == null) {
                res.status(404);
                return "{\"erro\": \"Favorito não encontrado.\"}";
            }

            // 5. Verificar se pertence ao usuário logado
            if (favorito.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado: você não pode remover favoritos de outro usuário.\"}";
            }

            // 6. Deletar
            favoritoDAO.delete(idFavorito);

            res.status(204);
            return "";

        } catch (Exception e) {

            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    public Object getSeguro(Request req, Response res) {

        try {

            // 1. Descobrir quem está logado
            String donoLogin = AuthService.getLoginFromToken(req);

            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            // 2. Buscar usuário
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            if (usuario == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado.\"}";
            }

            // 3. Pegar ID do favorito
            int idFavorito = Integer.parseInt(req.params(":id"));

            // 4. Buscar favorito
            Favorito favorito = favoritoDAO.get(idFavorito);

            if (favorito == null) {
                res.status(404);
                return "{\"erro\": \"Favorito não encontrado.\"}";
            }

            // 5. Verificar se pertence ao usuário logado
            if (favorito.getIdUsuario() != usuario.getId()) {
                res.status(403);
                return "{\"erro\": \"Acesso negado.\"}";
            }

            // 6. Retornar favorito
            res.status(200);
            return gson.toJson(favorito);

        } catch (Exception e) {

            e.printStackTrace();
            res.status(500);
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }

    // Listar favoritos de um usuário
    public List<Favorito> listarFavoritosUsuario(int idUsuario) {
        return favoritoDAO.getFavoritosByUsuario(idUsuario);
    }

    // Listar usuários que favoritaram um produto
    public List<Favorito> listarFavoritosProduto(int idProduto) {
        return favoritoDAO.getFavoritosByProduto(idProduto);
    }

    // Verificar se é favorito
    public boolean isFavorito(int idUsuario, int idProduto) {
        return favoritoDAO.isFavorito(idUsuario, idProduto);
    }
}