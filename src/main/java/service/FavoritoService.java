package service;

import java.util.List;

import dao.FavoritoDAO;
import dao.ProdutoDAO;
import dao.UsuarioDAO;
import model.Favorito;
import model.Produto;
import model.Usuario;
import spark.Request;
import spark.Response;

public class FavoritoService extends BaseService<Favorito> {


    public FavoritoService() {
        super(new FavoritoDAO());
    }

    @Override
    protected Class<Favorito> getModelClass() {
        return Favorito.class;
    }

    @Override
    protected void onBeforeInsert(Favorito obj) throws Exception {
        obj.validar();
        
        // Verifica se o produto que o cara quer favoritar realmente existe
        ProdutoDAO produtoDAO = new ProdutoDAO();
        Produto produto = produtoDAO.get(obj.getIdProduto());
        if (produto == null) {
            throw new Exception("Produto inexistente. Não é possível favoritar.");
        }
        
        // Verifica se a pessoa já favoritou esse produto antes
        FavoritoDAO favDao = (FavoritoDAO) this.dao;
        if (favDao.isFavorito(obj.getIdUsuario(), obj.getIdProduto())) {
            throw new Exception("Produto já está nos favoritos.");
        }
    }

    @Override
    protected void onBeforeUpdate(Favorito obj) throws Exception {
        throw new Exception("Ação não permitida. Para alterar, remova o favorito atual e crie um novo.");
    }

    @Override
    public Object insert(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401);
                return "{\"erro\": \"Sessão inválida.\"}";
            }

            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            Favorito favorito = gson.fromJson(req.body(), getModelClass());
            
            // FORÇA o idUsuario do favorito a ser o do cara que está logado (Segurança Máxima)
            favorito.setIdUsuario(usuario.getId());

            onBeforeInsert(favorito);
            this.dao.insert(favorito);

            res.status(201);
            return gson.toJson(favorito);

        } catch (Exception e) {
            res.status(400); // 400 = Bad Request (ex: já favoritou)
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

   @Override
    public Object delete(Request req, Response res) {
        try {
            // 1. Pega o login do usuário pelo token
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401); 
                return "{\"erro\": \"Sessão inválida.\"}";
            }
            
            // 2. Busca o objeto usuário completo para ter o ID
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.getByLogin(donoLogin);

            // 3. ATENÇÃO AQUI: O ":id" que vem da URL é o ID do PRODUTO
            int idProduto = Integer.parseInt(req.params(":id"));
            
            FavoritoDAO favDao = (FavoritoDAO) this.dao;

            // 4. Usa o novo método do DAO que criamos: delete(idUsuario, idProduto)
            // Isso remove a relação correta sem precisar do ID interno da tabela
            if (favDao.delete(usuario.getId(), idProduto)) {
                res.status(200);
                return "{\"msg\": \"Favorito removido com sucesso.\"}";
            } else {
                res.status(404); 
                return "{\"erro\": \"Este produto não estava nos seus favoritos.\"}";
            }

        } catch (Exception e) {
            e.printStackTrace(); // Log para você ver o erro no console do VS Code
            res.status(500); 
            return "{\"erro\": \"Erro interno do servidor.\"}";
        }
    }
    
    // --- RELATÓRIO PARA A GESTÃO ---
    public Object getFavoritosPorProduto(Request req, Response res) {
        try {
            int idProduto = Integer.parseInt(req.params(":idProduto"));
            FavoritoDAO favDao = (FavoritoDAO) this.dao;
            res.status(200);
            return gson.toJson(favDao.getFavoritosByProduto(idProduto));
        } catch (Exception e) {
            res.status(500); return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object getMeusFavoritos(Request req, Response res) {
        try {
            String donoLogin = AuthService.getLoginFromToken(req);
            if (donoLogin == null) {
                res.status(401); return "{\"erro\": \"Sessão inválida.\"}";
            }

            UsuarioDAO userDAO = new UsuarioDAO();
            Usuario u = userDAO.getByLogin(donoLogin);

            FavoritoDAO favDao = (FavoritoDAO) this.dao;
            List<Favorito> meusFavoritos = favDao.getFavoritosByUsuario(u.getId());

            res.status(200);
            return gson.toJson(meusFavoritos);

        } catch (Exception e) {
            res.status(500); return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}