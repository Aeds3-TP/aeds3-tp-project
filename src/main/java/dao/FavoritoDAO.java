package dao;

import model.Favorito;
import java.util.ArrayList;
import java.util.List;

public class FavoritoDAO extends FileDAO<Favorito> {

    public FavoritoDAO() {
        super("Favorito.db", Favorito.class);
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

    // Busca todos os favoritos de um produto
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