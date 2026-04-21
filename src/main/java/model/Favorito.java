package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Favorito implements Registro {

    private int id; // <-- ATRIBUTO RESTAURADO! A Chave Primária real.
    private int idProduto;
    private int idUsuario;

    // Construtor vazio
    public Favorito() {
        this.id = -1;
        this.idProduto = -1;
        this.idUsuario = -1;
    }

    // Construtor com parâmetros
    public Favorito(int idProduto, int idUsuario) {
        this.id = -1;
        this.idProduto = idProduto;
        this.idUsuario = idUsuario;
    }

    // Validação
    public void validar() throws IllegalArgumentException {
        if (idProduto <= 0) {
            throw new IllegalArgumentException("Produto inválido.");
        }
        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Usuário inválido.");
        }
    }

    // SERIALIZAÇÃO
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id); // Gravando o ID real
        dos.writeInt(idProduto);
        dos.writeInt(idUsuario);

        return baos.toByteArray();
    }

    // DESSERIALIZAÇÃO
    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt(); // Lendo o ID real
        this.idProduto = dis.readInt();
        this.idUsuario = dis.readInt();
    }

    // GETTERS E SETTERS
    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getIdProduto() {
        return idProduto;
    }
    
    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}