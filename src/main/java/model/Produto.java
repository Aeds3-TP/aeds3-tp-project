package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Produto implements Registro {
    private int id;
    private int idCategoria;
    private String nome;
    private String descricaoProd;
    private float preco;
    private long dataValidade;
    private int quantidadeEstoque;
    private String[] tags;
    private String[] imagensUrls;

    // Construtor vazio
    public Produto() {
        this.id = -1;
        this.idCategoria = -1;
        this.nome = "";
        this.descricaoProd = "";
        this.preco = 0.0f;
        this.dataValidade = 0L;
        this.quantidadeEstoque = 0;
        this.tags = new String[0];
        this.imagensUrls = new String[0];
    }

    // Construtor com parâmetros
    public Produto(int idCategoria, String nome, String descricaoProd,float preco, long dataValidade, int quantidadeEstoque,String[] tags, String[] imagensUrls) {
        this.id = -1;
        this.idCategoria = idCategoria;
        this.nome = nome;
        this.descricaoProd = descricaoProd;
        this.preco = preco;
        this.dataValidade = dataValidade;
        this.quantidadeEstoque = quantidadeEstoque;
        this.tags = tags != null ? tags : new String[0];
        this.imagensUrls = imagensUrls != null ? imagensUrls : new String[0];
    }

    //validação
    public void validar() throws IllegalArgumentException {
        if (this.nome == null || this.nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (this.preco <= 0) {
            throw new IllegalArgumentException("O preço do produto deve ser maior que zero.");
        }
        if (this.quantidadeEstoque < 0) {
            throw new IllegalArgumentException("O estoque inicial não pode ser negativo.");
        }
        if (this.idCategoria <= 0) {
            throw new IllegalArgumentException("O produto deve estar vinculado a uma categoria válida.");
        }
    }

    //SERIALIZAÇÃO (De Objeto para Bytes)
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(id);
        dos.writeInt(idCategoria);
        dos.writeUTF(nome);
        dos.writeUTF(descricaoProd);
        dos.writeFloat(preco);
        dos.writeLong(dataValidade);
        dos.writeInt(quantidadeEstoque);
        
        // Arrays multivalorados (Tamanho + Itens)
        dos.writeInt(tags.length);
        for (String tag : tags) { dos.writeUTF(tag); }
        
        dos.writeInt(imagensUrls.length);
        for (String img : imagensUrls) { dos.writeUTF(img); }
        
        return baos.toByteArray();
    }

    //DESSERIALIZAÇÃO (De Bytes para Objeto)
    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        
        this.id = dis.readInt();
        this.idCategoria = dis.readInt();
        this.nome = dis.readUTF();
        this.descricaoProd = dis.readUTF();
        this.preco = dis.readFloat();
        this.dataValidade = dis.readLong();
        this.quantidadeEstoque = dis.readInt();
        
        // Arrays multivalorados (Lê o tamanho + Itens)
        int qtdTags = dis.readInt();
        this.tags = new String[qtdTags];
        for (int i = 0; i < qtdTags; i++) { this.tags[i] = dis.readUTF(); }
        
        int qtdImagens = dis.readInt();
        this.imagensUrls = new String[qtdImagens];
        for (int i = 0; i < qtdImagens; i++) { this.imagensUrls[i] = dis.readUTF(); }
    }

    // --- GETTERS E SETTERS COMPLETOS ---
    
    @Override 
    public int getId() { return id; }
    @Override 
    public void setId(int id) { this.id = id; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricaoProd() { return descricaoProd; }
    public void setDescricaoProd(String descricaoProd) { this.descricaoProd = descricaoProd; }

    public float getPreco() { return preco; }
    public void setPreco(float preco) { this.preco = preco; }

    public long getDataValidade() { return dataValidade; }
    public void setDataValidade(long dataValidade) { this.dataValidade = dataValidade; }

    public int getQuantidadeEstoque() { return quantidadeEstoque; }
    public void setQuantidadeEstoque(int quantidadeEstoque) { this.quantidadeEstoque = quantidadeEstoque; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public String[] getImagensUrls() { return imagensUrls; }
    public void setImagensUrls(String[] imagensUrls) { this.imagensUrls = imagensUrls; }
}