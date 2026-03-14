package model;

import java.io.*;

public class Favorito implements Registro {

    private int idProduto;
    private int idUsuario;

    // Construtor vazio
    public Favorito() {
        this.idProduto = -1;
        this.idUsuario = -1;
    }

    // Construtor com parâmetros
    public Favorito(int idProduto, int idUsuario) {
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

        dos.writeInt(idProduto);
        dos.writeInt(idUsuario);

        return baos.toByteArray();
    }

    // DESSERIALIZAÇÃO
    @Override
    public void fromByteArray(byte[] ba) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.idProduto = dis.readInt();
        this.idUsuario = dis.readInt();
    }

    // GETTERS E SETTERS

    @Override
    public int getId() {
        return idProduto;
    }

    @Override
    public void setId(int id) {
        this.idProduto = id;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}