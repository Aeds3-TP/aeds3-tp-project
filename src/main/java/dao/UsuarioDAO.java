package dao;
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
}