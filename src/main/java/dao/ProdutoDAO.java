package dao;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import model.Favorito;
import model.Produto;

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
        
        listaFiltrada.sort((p1, p2) -> Float.compare(p1.getPreco(), p2.getPreco()));
        
        return listaFiltrada;
    }
    
    public List<Produto> getProdutosOrdenadosPorPreco() {
        
        // 1. Dispara a Ordenação Externa no disco
        util.OrdenacaoExternaProduto ordenador = new util.OrdenacaoExternaProduto();
        ordenador.ordenarPorPreco(); 
        
        // 2. Lê o arquivo NOVO (produto_ordenado.db) gerado
        List<Produto> listaOrdenada = new ArrayList<>();
        try {
        	File arquivoOrdenado = new File("dados/relatorios/produto_ordenado.db");
            if (arquivoOrdenado.exists()) {
                RandomAccessFile arqNovo = new RandomAccessFile(arquivoOrdenado, "r");
                if (arqNovo.length() > 0) {
                    arqNovo.seek(4); // Pula o cabeçalho
                    while (arqNovo.getFilePointer() < arqNovo.length()) {
                        byte lapide = arqNovo.readByte();
                        int tamanho = arqNovo.readInt();
                        
                        byte[] bytes = new byte[tamanho];
                        arqNovo.read(bytes);
                        Produto p = new Produto();
                        p.fromByteArray(bytes);
                        
                        listaOrdenada.add(p);
                    }
                }
                arqNovo.close();
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo ordenado: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaOrdenada;
    }
    
    @Override
    public boolean delete(int idProduto) {
        // Checa se o produto já foi vendido (RESTRICT)
        ItemPedidoDAO itemDao = new ItemPedidoDAO();
        if (!itemDao.getItensByProduto(idProduto).isEmpty()) {
            System.err.println("ERRO DE INTEGRIDADE: O Produto ID " + idProduto 
                + " não pode ser excluído pois faz parte do histórico de pedidos finalizados.");
            return false;
        }
        
        // Apaga o produto das listas de Favoritos (CASCADE)
        FavoritoDAO favoritoDao = new FavoritoDAO();
        List<Favorito> favoritos = favoritoDao.getFavoritosByProduto(idProduto);
        for (Favorito f : favoritos) {
            favoritoDao.delete(f.getId());
        }
        
        // Libera a exclusão física do produto
        return super.delete(idProduto);
    }
}
