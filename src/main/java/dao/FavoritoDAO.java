package dao;

import java.util.ArrayList;
import java.util.List;

import indice.ArvoreBMais;
import indice.ParIntInt;
import model.Favorito;

public class FavoritoDAO extends FileDAO<Favorito> {

    // REQUISITO (B) - Indexação do Relacionamento
    private ArvoreBMais<ParIntInt> arvoreUsuarioProduto;

    public FavoritoDAO() {
        super("Favorito.db", Favorito.class);
        try {
            // Cria o índice B+ apontando do Usuário para o Produto
            arvoreUsuarioProduto = new ArvoreBMais<>(ParIntInt.class.getConstructor(), 5, "dados/indices/favorito_n_n.btree");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int insert(Favorito f) throws Exception {
        int idGerado = super.insert(f);
        // Insere o relacionamento na Árvore B+
        arvoreUsuarioProduto.create(new ParIntInt(f.getIdUsuario(), f.getIdProduto()));
        return idGerado;
    }

    @Override
    public boolean delete(int idRegistro) {
        Favorito f = super.get(idRegistro);
        if (f != null) {
            try {
                // Remove o relacionamento da Árvore B+
                arvoreUsuarioProduto.delete(new ParIntInt(f.getIdUsuario(), f.getIdProduto()));
            } catch (Exception e) { e.printStackTrace(); }
        }
        return super.delete(idRegistro);
    }

    // Deleta pelo Par (Usado pelo Front-End)
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
                // Remove da árvore usando o par exato
                arvoreUsuarioProduto.delete(new ParIntInt(idUsuario, idProduto));
            } catch (Exception e) { e.printStackTrace(); }
            return super.delete(idParaRemover);
        }
        return false;
    }

    // =======================================================
    // ACESSO AO RELACIONAMENTO USANDO A ÁRVORE B+ (MUITO RÁPIDO)
    // =======================================================
    public List<Favorito> getFavoritosByUsuario(int idUsuario) {
        List<Favorito> listaFiltrada = new ArrayList<>();
        try {
            // Truque do Professor Kutova: num2 = -1 traz TODOS os produtos desse usuário!
            ArrayList<ParIntInt> pares = arvoreUsuarioProduto.read(new ParIntInt(idUsuario, -1));
            
            for (ParIntInt par : pares) {
                // Como não precisamos dos outros dados da tabela, reconstruímos o Favorito 
                // direto pela Árvore, poupando acesso ao disco físico!
                listaFiltrada.add(new Favorito(par.getNum2(), par.getNum1())); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listaFiltrada;
    }

    public boolean isFavorito(int idUsuario, int idProduto) {
        try {
            // Busca na Árvore B+ para ver se o par exato existe
            ArrayList<ParIntInt> pares = arvoreUsuarioProduto.read(new ParIntInt(idUsuario, idProduto));
            return !pares.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public List<Favorito> getFavoritosByProduto(int idProduto) {
        List<Favorito> listaFiltrada = new ArrayList<>();
        List<Favorito> todos = super.getAll();
        for (Favorito f : todos) {
            if (f.getIdProduto() == idProduto) listaFiltrada.add(f);
        }
        return listaFiltrada;
    }
}