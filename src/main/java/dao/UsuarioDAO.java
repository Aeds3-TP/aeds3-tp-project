package dao;
import java.util.List;

import model.Favorito;
import model.Pedido;
import model.Usuario;

public class UsuarioDAO extends FileDAO<Usuario> {
    public UsuarioDAO() { super("usuarios.db", Usuario.class); }

    public Usuario getByLogin(String login) {
        try {
            arquivo.seek(4);
            while (arquivo.getFilePointer() < arquivo.length()) {
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readInt();
                if (lapide == ' ') {
                    byte[] b = new byte[tamanho];
                    arquivo.read(b);
                    Usuario u = new Usuario();
                    u.fromByteArray(b);
                    if (u.getLogin().equals(login)) return u;
                } else { arquivo.skipBytes(tamanho); }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    @Override
    public boolean delete(int idUsuario) {
        
        // Limpa os Favoritos da conta
        FavoritoDAO favoritoDao = new FavoritoDAO();
        List<Favorito> favoritos = favoritoDao.getFavoritosByUsuario(idUsuario);
        for (Favorito fav : favoritos) {
            favoritoDao.delete(fav.getId());
        }
        
        // Limpa os Pedidos (Isso vai acionar o Cascade do PedidoDAO automaticamente!)
        PedidoDAO pedidoDao = new PedidoDAO();
        List<Pedido> pedidos = pedidoDao.getPedidosByUsuario(idUsuario);
        for (Pedido ped : pedidos) {
            pedidoDao.delete(ped.getId()); 
        }
        
        // Apaga a conta do Usuário
        return super.delete(idUsuario);
    }
}