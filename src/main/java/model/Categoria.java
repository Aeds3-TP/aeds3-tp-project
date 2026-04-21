package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Categoria implements Registro {
    private int id;
    private String nome;
    private String descricao;

    // Construtor vazio obrigatório para o FileDAO conseguir instanciar
    public Categoria() {
        this.id = -1;
        this.nome = "";
        this.descricao = "";
    }

    // Construtor
    public Categoria(String nome, String descricao) {
        this.id = -1; 
        this.nome = nome;
        this.descricao = descricao;
    }

    public void validar() throws IllegalArgumentException {
        if (this.nome == null || this.nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório.");
        }
        if (this.descricao == null || this.descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da categoria é obrigatória.");
        }
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeInt(this.id);
        dos.writeUTF(this.nome);
        dos.writeUTF(this.descricao);
        
        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);
        
        this.id = dis.readInt();
        this.nome = dis.readUTF();
        this.descricao = dis.readUTF();
    }


    @Override 
    public int getId() { return this.id; }
    @Override 
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}