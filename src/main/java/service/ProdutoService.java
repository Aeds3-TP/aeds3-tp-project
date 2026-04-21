package service;

import dao.CategoriaDAO;
import dao.ProdutoDAO;
import model.Produto;
import spark.Request;
import spark.Response;

public class ProdutoService extends BaseService<Produto> {
    
    // O Service do Produto precisa consultar o DAO da Categoria para validar a chave estrangeira (FK)
    private CategoriaDAO categoriaDao;

    public ProdutoService() {
        // Instancia o ProdutoDAO e manda lá pro BaseService cuidar do CRUD básico
        super(new ProdutoDAO()); 
        this.categoriaDao = new CategoriaDAO(); 
    }

    @Override
    protected Class<Produto> getModelClass() {
        return Produto.class; // Ensina o Gson a transformar o JSON da requisição na classe certa
    }
    
    @Override
    protected void onBeforeInsert(Produto p) throws Exception {
        // 1. Validação Interna (O próprio objeto verifica se está válido)
        p.validar(); 
        
        // 2. Validação Externa (O Service vai no banco garantir que a categoria existe)
        if (categoriaDao.get(p.getIdCategoria()) == null) {
            throw new Exception("Categoria inexistente! Impossível cadastrar produto.");
        }
    }

    @Override
    protected void onBeforeUpdate(Produto p) throws Exception {
        // No update, aplicamos as mesmas regras de segurança do insert
        p.validar();
        if (categoriaDao.get(p.getIdCategoria()) == null) {
            throw new Exception("Categoria inexistente! Impossível atualizar para esta categoria.");
        }
    }

    // Rota para a Árvore B+ (Busca por intervalo de preço)
    public Object listarAtePreco(Request req, Response res) {
        try {
            float precoMaximo = Float.parseFloat(req.params(":preco"));
            if (precoMaximo < 0) {
                res.status(400); // 400 Bad Request
                return "{\"erro\": \"O preço máximo não pode ser negativo.\"}";
            }
            
            // Fazemos um 'cast' para acessar as funções específicas que só o ProdutoDAO tem
            ProdutoDAO prodDao = (ProdutoDAO) this.dao; 
            
            res.status(200);
            return gson.toJson(prodDao.getProdutosAtePreco(precoMaximo));
            
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
    
    public Object listarOrdenadosPorPreco(Request req, Response res) {
        try {
            ProdutoDAO prodDao = (ProdutoDAO) this.dao; 
            
            res.status(200);
            return gson.toJson(prodDao.getProdutosOrdenadosPorPreco());
            
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"Falha na ordenação externa: " + e.getMessage() + "\"}";
        }
    }

    // Rota para o Relacionamento 1:N
    public Object listarPorCategoria(Request req, Response res) {
        try {
            int idCategoria = Integer.parseInt(req.params(":idCategoria"));
            
            ProdutoDAO prodDao = (ProdutoDAO) this.dao;
            
            res.status(200);
            return gson.toJson(prodDao.getProdutosByCategoria(idCategoria));
            
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    // --- MÉTODO INTERNO DE NEGÓCIO ---
    // Este não recebe Request/Response porque não é uma rota. 
    // Ele será chamado internamente pelo 'ItemPedidoService' na hora que uma compra for finalizada.
    public boolean reduzirEstoque(int idProduto, int quantidadeComprada) throws Exception {
        Produto p = this.dao.get(idProduto);
        
        if (p != null && p.getQuantidadeEstoque() >= quantidadeComprada) {
            p.setQuantidadeEstoque(p.getQuantidadeEstoque() - quantidadeComprada);
            return this.dao.update(p);
        }
        return false;
    }
}