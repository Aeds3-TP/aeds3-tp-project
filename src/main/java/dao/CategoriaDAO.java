package dao;

import model.Categoria;
import model.Produto;

import java.util.List;

public class CategoriaDAO extends FileDAO<Categoria> {

    public CategoriaDAO() {
        super("categoria.db", Categoria.class);
    }

    // Método extra caso você queira impedir categorias com o mesmo nome depois
    public Categoria getByNome(String nome) {
        List<Categoria> todas = super.getAll();
        for (Categoria c : todas) {
            if (c.getNome().equalsIgnoreCase(nome)) {
                return c;
            }
        }
        return null;
    }
    
    @Override
    public boolean delete(int idCategoria) {
        ProdutoDAO produtoDao = new ProdutoDAO();
        List<Produto> produtosVinculados = produtoDao.getProdutosByCategoria(idCategoria);
        
        if (!produtosVinculados.isEmpty()) {
            System.err.println("ERRO DE INTEGRIDADE: Não é possível deletar a Categoria ID " 
                + idCategoria + " pois existem " + produtosVinculados.size() + " produtos vinculados a ela.");
            return false; // Bloqueia a exclusão
        }
        
        // Se a lista estiver vazia, libera a exclusão
        return super.delete(idCategoria);
    }
}
