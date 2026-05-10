package dao;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import indice.ArvoreBMais;
import indice.ParIntInt;
import model.Favorito;
import model.Produto;

public class ProdutoDAO extends FileDAO<Produto> {

    private ArvoreBMais<ParIntInt> arvoreCategoria;

    public ProdutoDAO() {
        super("produto.db", Produto.class);
        try {
            arvoreCategoria = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 5, "dados/indices/categoria_produto.btree");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar a Árvore B+ : " + e.getMessage());
        }
    }

    @Override
    public int insert(Produto p) throws Exception {
        int idGerado = super.insert(p); 
        arvoreCategoria.create(new ParIntInt(p.getIdCategoria(), idGerado));
        return idGerado;
    }

    @Override
    public boolean update(Produto p) throws Exception {
        Produto pAntigo = super.get(p.getId());
        
        if (pAntigo != null && pAntigo.getIdCategoria() != p.getIdCategoria()) {
            arvoreCategoria.delete(new ParIntInt(pAntigo.getIdCategoria(), p.getId()));
            arvoreCategoria.create(new ParIntInt(p.getIdCategoria(), p.getId()));
        }
        
        return super.update(p);
    }

    @Override
    public boolean delete(int idProduto) {
        Produto p = super.get(idProduto);
        
        ItemPedidoDAO itemDao = new ItemPedidoDAO();
        if (!itemDao.getItensByProduto(idProduto).isEmpty()) {
            System.err.println("ERRO DE INTEGRIDADE: Produto possui histórico de pedidos.");
            return false;
        }
        
        FavoritoDAO favoritoDao = new FavoritoDAO();
        List<Favorito> favoritos = favoritoDao.getFavoritosByProduto(idProduto);
        for (Favorito f : favoritos) favoritoDao.delete(f.getId());
        
        if (p != null) {
            try {
                arvoreCategoria.delete(new ParIntInt(p.getIdCategoria(), p.getId()));
            } catch (Exception e) { e.printStackTrace(); }
        }
        
        return super.delete(idProduto);
    }
    public List<Produto> getProdutosByCategoria(int idCategoria) {
        List<Produto> listaFiltrada = new ArrayList<>();
        try {
            ArrayList<ParIntInt> pares = arvoreCategoria.read(new ParIntInt(idCategoria, -1));
            
            for (ParIntInt par : pares) {
                Produto p = super.get(par.getNum2()); 
                if (p != null) {
                    listaFiltrada.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaFiltrada;
    }
    
    public List<Produto> getProdutosOrdenadosPorCategoriaGeral() {
        List<Produto> listaOrdenada = new ArrayList<>();
        try {
            ArrayList<ParIntInt> pares = arvoreCategoria.readAll();
            
            for (ParIntInt par : pares) {
                Produto p = super.get(par.getNum2()); 
                if (p != null) {
                    listaOrdenada.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaOrdenada;
    }

    public List<Produto> getProdutosAtePreco(float precoMaximo) {
        List<Produto> listaFiltrada = new ArrayList<>();
        List<Produto> todos = super.getAll(); 
        for (Produto p : todos) {
            if (p.getPreco() <= precoMaximo) listaFiltrada.add(p);
        }
        listaFiltrada.sort((p1, p2) -> Float.compare(p1.getPreco(), p2.getPreco()));
        return listaFiltrada;
    }
    
    public List<Produto> getProdutosOrdenadosPorPreco() {
        util.OrdenacaoExternaProduto ordenador = new util.OrdenacaoExternaProduto();
        ordenador.ordenarPorPreco(); 
        List<Produto> listaOrdenada = new ArrayList<>();
        try {
            File arquivoOrdenado = new File("dados/relatorios/produto_ordenado.db");
            if (arquivoOrdenado.exists()) {
                RandomAccessFile arqNovo = new RandomAccessFile(arquivoOrdenado, "r");
                if (arqNovo.length() > 0) {
                    arqNovo.seek(4);
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
        } catch (Exception e) { e.printStackTrace(); }
        return listaOrdenada;
    }
}
