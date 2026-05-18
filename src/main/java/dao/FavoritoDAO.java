package dao;

import java.util.ArrayList;
import java.util.List;

import indice.ArvoreBMais;
import indice.ParIntInt;
import model.Favorito;

public class FavoritoDAO extends FileDAO<Favorito> {

    private ArvoreBMais<ParIntInt> arvoreUsuarioProduto;
    private ArvoreBMais<ParIntInt> arvoreProdutoUsuario;

    public FavoritoDAO() {
        super("Favorito.db", Favorito.class);
        try {
            arvoreUsuarioProduto = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 5, "dados/indices/usuario_produto_n_n.btree");
            arvoreProdutoUsuario = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 5, "dados/indices/produto_usuario_n_n.btree");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int insert(Favorito f) throws Exception {
        int idGerado = super.insert(f);
        
        arvoreUsuarioProduto.create(new ParIntInt(f.getIdUsuario(), f.getIdProduto()));
        arvoreProdutoUsuario.create(new ParIntInt(f.getIdProduto(), f.getIdUsuario()));
        
        return idGerado;
    }

    @Override
    public boolean delete(int idRegistro) {
        Favorito f = super.get(idRegistro);
        if (f != null) {
            try {
                arvoreUsuarioProduto.delete(new ParIntInt(f.getIdUsuario(), f.getIdProduto()));
                arvoreProdutoUsuario.delete(new ParIntInt(f.getIdProduto(), f.getIdUsuario()));
            } catch (Exception e) { e.printStackTrace(); }
        }
        return super.delete(idRegistro);
    }

    public boolean delete(int idUsuario, int idProduto) {
        List<Favorito> todos = super.getAll();
        int idParaRemover = -1;

        for (Favorito f : todos) {
            if (f.getIdUsuario() == idUsuario && f.getIdProduto() == idProduto) {
                idParaRemover = f.getId();
                break;
            }
        }

        if (idParaRemover != -1) {
            try {
                arvoreUsuarioProduto.delete(new ParIntInt(idUsuario, idProduto));
                arvoreProdutoUsuario.delete(new ParIntInt(idProduto, idUsuario));
            } catch (Exception e) { e.printStackTrace(); }
            
            return super.delete(idParaRemover);
        }

        return false; 
    }
    
    public List<Favorito> getFavoritosByUsuario(int idUsuario) {
        List<Favorito> listaFiltrada = new ArrayList<>();
        try {
            ArrayList<ParIntInt> pares = arvoreUsuarioProduto.read(new ParIntInt(idUsuario, -1));
            
            for (ParIntInt par : pares) {
                listaFiltrada.add(new Favorito(par.getNum2(), par.getNum1())); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaFiltrada;
    }

    public List<Favorito> getFavoritosByProduto(int idProduto) {
        List<Favorito> listaFiltrada = new ArrayList<>();
        try {
            ArrayList<ParIntInt> pares = arvoreProdutoUsuario.read(new ParIntInt(idProduto, -1));
            
            for (ParIntInt par : pares) {
                listaFiltrada.add(new Favorito(par.getNum1(), par.getNum2())); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaFiltrada;
    }

    public boolean isFavorito(int idUsuario, int idProduto) {
        try {
            ArrayList<ParIntInt> pares = arvoreUsuarioProduto.read(new ParIntInt(idUsuario, idProduto));
            return !pares.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}