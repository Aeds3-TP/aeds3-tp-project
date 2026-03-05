package dao;

import model.Produto;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO extends FileDAO<Produto> {

    public ProdutoDAO() {
        super("produto.db", Produto.class);
    }

    // Busca específica para o Relacionamento 1:N
    public List<Produto> getProdutosByCategoria(int idCategoria) {
        List<Produto> listaFiltrada = new ArrayList<>();
        List<Produto> todos = super.getAll();
        
        for (Produto p : todos) {
            if (p.getIdCategoria() == idCategoria) {
                listaFiltrada.add(p);
            }
        }
        return listaFiltrada;
    }
    
    // Busca específica para a Árvore B+ (Busca por Intervalo)
    public List<Produto> getProdutosAtePreco(float precoMaximo) {
        List<Produto> listaFiltrada = new ArrayList<>();
        
        // Fase 1: Busca sequencial. 
        // Na Fase 2 (Árvore B+), este getAll() será substituído por arvoreBPlusPreco.read(precoMaximo)
        List<Produto> todos = super.getAll(); 
        
        for (Produto p : todos) {
            if (p.getPreco() <= precoMaximo) {
                listaFiltrada.add(p);
            }
        }
        return listaFiltrada;
    }
}
