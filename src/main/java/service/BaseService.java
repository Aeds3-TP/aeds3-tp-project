package service;

import dao.FileDAO;
import model.Registro;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;


// Classe base do service que faz a comunicação final do java com o frontend
public abstract class BaseService<T extends Registro> {
    
    protected FileDAO<T> dao;
    protected Gson gson = new Gson();

    public BaseService(FileDAO<T> dao) {
        this.dao = dao;
    }
    
    protected abstract Class<T> getModelClass();
    
    // Metodos que vão executar sempre antes de alterar ou atualizar algo, é onde faz as validacoes, verifica a criptografia etc, antes de de fato inserir e atualizar
    protected void onBeforeInsert(T obj) throws Exception {}
    protected void onBeforeUpdate(T obj) throws Exception {}

    // Inserir
    public Object insert(Request req, Response res) {
        try {
            T obj = gson.fromJson(req.body(), getModelClass());
            onBeforeInsert(obj);
            dao.insert(obj);
            res.status(201);
            return gson.toJson(obj);
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
    
    // Get por id
    public Object get(Request req, Response res) {
        try {
            int id = Integer.parseInt(req.params(":id"));
            T obj = dao.get(id);
            
            if (obj != null) {
                res.status(200);
                return gson.toJson(obj);
            } else {
                res.status(404);
                return "{\"erro\": \"Objeto não encontrado\"}";
            }
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    // Get all
    public Object getAll(Request req, Response res) {
        return gson.toJson(dao.getAll());
    }

    // update
    public Object update(Request req, Response res) {
        try {
            int id = Integer.parseInt(req.params(":id"));
            T obj = gson.fromJson(req.body(), getModelClass());
            
            if (obj.getId() != id) obj.setId(id); // Garante consistência do ID

            onBeforeUpdate(obj); // Chama regra de negócio

            if (dao.update(obj)) {
                res.status(200);
                return gson.toJson(obj);
            } else {
                res.status(404);
                return "{\"erro\": \"Objeto não encontrado\"}";
            }
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    // delete
    public Object delete(Request req, Response res) {
        try {
            int id = Integer.parseInt(req.params(":id"));
            if (dao.delete(id)) {
                res.status(200);
                return "{\"msg\": \"Deletado com sucesso\"}";
            } else {
                res.status(404);
                return "{\"erro\": \"Não encontrado\"}";
            }
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}