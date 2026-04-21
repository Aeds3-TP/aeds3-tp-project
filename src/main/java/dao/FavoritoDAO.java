package dao;

import java.util.ArrayList;
import java.util.List;

import model.Favorito;

public class FavoritoDAO extends FileDAO<Favorito> {

    public FavoritoDAO() {
        super("Favorito.db", Favorito.class);
    }

    /**
     * Remove um favorito baseado no par Usuário e Produto.
     * Necessário porque o Front-end envia o ID do Produto, 
     * mas o FileDAO precisa do ID do Registro para deletar.
     */
    public boolean delete(int idUsuario, int idProduto) {
        List<Favorito> todos = super.getAll();
        int idParaRemover = -1;

        // Procura na lista qual favorito pertence a esse usuário e produto
        for (Favorito f : todos) {
            if (f.getIdUsuario() == idUsuario && f.getIdProduto() == idProduto) {
                idParaRemover = f.getId();
                break;
            }
        }

        // Se encontrou o ID do registro, usa o delete do FileDAO (super)
        if (idParaRemover != -1) {
            return super.delete(idParaRemover);
        }

        return false; // Não encontrado
    }

    // Busca todos os favoritos de um usuário
    public List<Favorito> getFavoritosByUsuario(int idUsuario) {
        List<Favorito> listaFiltrada = new ArrayList<>();
        List<Favorito> todos = super.getAll();

        for (Favorito f : todos) {
            if (f.getIdUsuario() == idUsuario) {
                listaFiltrada.add(f);
            }
        }

        return listaFiltrada;
    }

    // Busca todos os favoritos de um produto (ex: para contar curtidas)
    public List<Favorito> getFavoritosByProduto(int idProduto) {
        List<Favorito> listaFiltrada = new ArrayList<>();
        List<Favorito> todos = super.getAll();

        for (Favorito f : todos) {
            if (f.getIdProduto() == idProduto) {
                listaFiltrada.add(f);
            }
        }

        return listaFiltrada;
    }

    // Verifica se um produto já está favoritado por um usuário
    public boolean isFavorito(int idUsuario, int idProduto) {
        List<Favorito> todos = super.getAll();

        for (Favorito f : todos) {
            if (f.getIdUsuario() == idUsuario && f.getIdProduto() == idProduto) {
                return true;
            }
        }

        return false;
    }
}