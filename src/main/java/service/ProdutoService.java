package service;

import dao.ProdutoDAO;
//import dao.CategoriaDAO;
import model.Produto;
import java.util.List;

public class ProdutoService {
    
    private ProdutoDAO produtoDao;
//    private CategoriaDAO categoriaDao;

    public ProdutoService() {
        this.produtoDao = new ProdutoDAO();
//        this.categoriaDao = new CategoriaDAO(); 
    }

    public int criarProduto(Produto p) throws Exception {
        // Validação Interna do Produto
        p.validar(); 
        
        // Validação Externa (Depende do BD)
//        if (categoriaDao.get(p.getIdCategoria()) == null) {
//            throw new Exception("Categoria inexistente! Impossível cadastrar produto.");
//        }

        // Salva no arquivo binário
        return produtoDao.insert(p);
    }
    
    // Regra de negócio para a busca por intervalo de preço
    public List<Produto> listarAtePreco(float precoMaximo) throws Exception {
        if (precoMaximo < 0) {
            throw new Exception("O preço máximo de busca não pode ser negativo.");
        }
        return produtoDao.getProdutosAtePreco(precoMaximo);
    }

    public boolean reduzirEstoque(int idProduto, int quantidadeComprada) throws Exception {
        Produto p = produtoDao.get(idProduto);
        
        if (p != null && p.getQuantidadeEstoque() >= quantidadeComprada) {
            p.setQuantidadeEstoque(p.getQuantidadeEstoque() - quantidadeComprada);
            return produtoDao.update(p);
        }
        return false;
    }

    public List<Produto> listarPorCategoria(int idCategoria) {
        return produtoDao.getProdutosByCategoria(idCategoria);
    }
}