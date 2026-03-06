package dao;

import model.Categoria;
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
}
