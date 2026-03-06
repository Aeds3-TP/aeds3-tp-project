package service;

import dao.CategoriaDAO;
import model.Categoria;
import spark.Request;
import spark.Response;


public class CategoriaService extends BaseService<Categoria> {

    public CategoriaService() {
        super(new CategoriaDAO()); // Passa o DAO instanciado pro BaseService
    }

    @Override
    protected Class<Categoria> getModelClass() {
        return Categoria.class; // Ensina o Gson do BaseService qual classe usar
    }


    @Override
    protected void onBeforeInsert(Categoria obj) throws Exception {
        // 1. Validação estrutural
        obj.validar(); 
        
        // 2. Regra de negócio: Não deixar duplicar nome
        CategoriaDAO daoCat = (CategoriaDAO) this.dao;
        if (daoCat.getByNome(obj.getNome()) != null) {
            throw new Exception("Já existe uma categoria cadastrada com esse nome.");
        }
    }

    @Override
    protected void onBeforeUpdate(Categoria obj) throws Exception {
        obj.validar();
    }
    
    public Object getPorNome(Request req, Response res) {
        try {
            // Pega o nome que veio na URL
            String nomeBusca = req.params(":nome");
            
            // Faz o cast para acessar os poderes específicos do CategoriaDAO
            CategoriaDAO daoCat = (CategoriaDAO) this.dao;
            Categoria cat = daoCat.getByNome(nomeBusca);
            
            if (cat != null) {
                res.status(200);
                return gson.toJson(cat);
            } else {
                res.status(404);
                return "{\"erro\": \"Categoria não encontrada.\"}";
            }
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}