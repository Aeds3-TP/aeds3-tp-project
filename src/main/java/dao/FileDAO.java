package dao;

import model.Registro;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class FileDAO<T extends Registro> {

    protected RandomAccessFile arquivo;
    protected String nomeArquivo;
    protected Class<T> classe;

    public FileDAO(String nomeArquivo, Class<T> classe) {
        this.nomeArquivo = nomeArquivo;
        this.classe = classe;
        try {
            File dir = new File("dados");
            if (!dir.exists()) dir.mkdir();
            
            arquivo = new RandomAccessFile("dados/" + nomeArquivo, "rw");
            if (arquivo.length() == 0) arquivo.writeInt(0); // Header: ID=0
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int insert(T elem) throws Exception {
        arquivo.seek(0);
        int novoId = arquivo.readInt() + 1;
        elem.setId(novoId);

        arquivo.seek(0);
        arquivo.writeInt(novoId); // Atualiza Header

        arquivo.seek(arquivo.length());
        byte[] dados = elem.toByteArray();
        
        arquivo.writeByte(' '); // Lapide
        arquivo.writeInt(dados.length);
        arquivo.write(dados);
        
        return novoId;
    }

    public T get(int id) {
        try {
            arquivo.seek(4);
            while (arquivo.getFilePointer() < arquivo.length()) {
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readInt();
                if (lapide == ' ') {
                    byte[] dados = new byte[tamanho];
                    arquivo.read(dados);
                    T obj = classe.getDeclaredConstructor().newInstance();
                    obj.fromByteArray(dados);
                    if (obj.getId() == id) return obj;
                } else {
                    arquivo.skipBytes(tamanho);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
    
    public List<T> getAll() {
        List<T> lista = new ArrayList<>();
        try {
            arquivo.seek(4);
            while (arquivo.getFilePointer() < arquivo.length()) {
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readInt();
                if (lapide == ' ') {
                    byte[] dados = new byte[tamanho];
                    arquivo.read(dados);
                    T obj = classe.getDeclaredConstructor().newInstance();
                    obj.fromByteArray(dados);
                    lista.add(obj);
                } else {
                    arquivo.skipBytes(tamanho);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public boolean delete(int id) {
        try {
            arquivo.seek(4); // Pula cabeçalho
            while (arquivo.getFilePointer() < arquivo.length()) {
                long posicaoLápide = arquivo.getFilePointer(); // Guarda posição
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readInt();
                
                if (lapide == ' ') {
                    byte[] dados = new byte[tamanho];
                    arquivo.read(dados);
                    T obj = classe.getDeclaredConstructor().newInstance();
                    obj.fromByteArray(dados);
                    
                    if (obj.getId() == id) {
                        // Volta para a lapide e marca como excluído
                        arquivo.seek(posicaoLápide);
                        arquivo.writeByte('*'); //lapide
                        return true;
                    }
                } else {
                    arquivo.skipBytes(tamanho);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(T elem) throws Exception {
        //Tenta apagar o registro antigo
        if (delete(elem.getId())) {
            //Se apagou com sucesso, escreve o novo no final
            arquivo.seek(arquivo.length());
            byte[] dados = elem.toByteArray();
            
            arquivo.writeByte(' '); // Ativo
            arquivo.writeInt(dados.length);
            arquivo.write(dados);
            return true;
        }
        return false; // ID não encontrado
    }
}