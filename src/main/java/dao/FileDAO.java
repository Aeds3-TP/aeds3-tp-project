package dao;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import indice.HashExtensivel;
import model.Registro;

public abstract class FileDAO<T extends Registro> {

    protected RandomAccessFile arquivo;
    protected String nomeArquivo;
    protected Class<T> classe;
    protected HashExtensivel indiceHash; 

    public FileDAO(String nomeArquivo, Class<T> classe) {
        this.nomeArquivo = nomeArquivo;
        this.classe = classe;
        try {
            //Cria a subpasta para as TABELAS (os arquivos .db)
            File dirRegistros = new File("dados/registros");
            if (!dirRegistros.exists()) dirRegistros.mkdirs(); // mkdirs cria toda a árvore de pastas

            //Cria a subpasta para os ÍNDICES (os arquivos .hash)
            File dirIndices = new File("dados/indices");
            if (!dirIndices.exists()) dirIndices.mkdirs();
            
            //Aponta o RandomAccessFile para a pasta de registros
            arquivo = new RandomAccessFile("dados/registros/" + nomeArquivo, "rw");
            
            //Aponta o HashExtensivel para a pasta de índices
            indiceHash = new HashExtensivel(10, "dados/indices/" + nomeArquivo + ".hash_d", "dados/indices/" + nomeArquivo + ".hash_b");

            // Cria o cabeçalho se o arquivo .db for novo
            if (arquivo.length() == 0) arquivo.writeInt(0); 
            
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int insert(T elem) throws Exception {
        arquivo.seek(0);
        int novoId = arquivo.readInt() + 1;
        elem.setId(novoId);

        arquivo.seek(0);
        arquivo.writeInt(novoId); 

        arquivo.seek(arquivo.length());
        
        long enderecoEmBytes = arquivo.getFilePointer();
        
        byte[] dados = elem.toByteArray();
        
        arquivo.writeByte(' '); 
        arquivo.writeInt(dados.length);
        arquivo.write(dados);
        
        indiceHash.create(novoId, enderecoEmBytes);
        
        return novoId;
    }

    public T get(int id) {
        try {
            long endereco = indiceHash.read(id);
            
            if (endereco != -1) {
                arquivo.seek(endereco); 
                byte lapide = arquivo.readByte();
                int tamanho = arquivo.readInt();
                
                if (lapide == ' ') {
                    byte[] dados = new byte[tamanho];
                    arquivo.read(dados);
                    T obj = classe.getDeclaredConstructor().newInstance();
                    obj.fromByteArray(dados);
                    if (obj.getId() == id) return obj;
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
            long endereco = indiceHash.read(id);
            
            if (endereco != -1) {
                arquivo.seek(endereco);
                arquivo.writeByte('*'); 
                
                indiceHash.delete(id);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean update(T elem) throws Exception {
        long enderecoAntigo = indiceHash.read(elem.getId());
        
        if (enderecoAntigo != -1) {
            arquivo.seek(enderecoAntigo);
            arquivo.writeByte('*'); 
            
            arquivo.seek(arquivo.length());
            long novoEndereco = arquivo.getFilePointer();
            
            byte[] dados = elem.toByteArray();
            arquivo.writeByte(' '); 
            arquivo.writeInt(dados.length);
            arquivo.write(dados);
            
            indiceHash.update(elem.getId(), novoEndereco);
            return true;
        }
        return false; 
    }
}