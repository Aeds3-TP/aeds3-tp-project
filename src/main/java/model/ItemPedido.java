package model;

import java.io.*;


public class ItemPedido implements Registro {

    private int id;
    private int idPedido;
    private int idProduto;
    private int quantidade;
    private float precoCongelado;

    // Construtor vazio
    public ItemPedido() {
        this.id = -1;
        this.idPedido = -1;
        this.idProduto = -1;
        this.quantidade = 0;
        this.precoCongelado = 0.0f;
    }

    // Construtor com parâmetros
    public ItemPedido(int idPedido, int idProduto, int quantidade, float precoCongelado) {
        this.id = -1;
        this.idPedido = idPedido;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.precoCongelado = precoCongelado;
    }

    // Validação
    public void validar() {

        if (idPedido <= 0) {
            throw new IllegalArgumentException("Pedido inválido.");
        }

        if (idProduto <= 0) {
            throw new IllegalArgumentException("Produto inválido.");
        }

        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }


        if (precoCongelado < 0) {
            throw new IllegalArgumentException("Preço congelado inválido.");
        }
    }

    // SERIALIZAÇÃO
    @Override
    public byte[] toByteArray() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeInt(idPedido);
        dos.writeInt(idProduto);
        dos.writeInt(quantidade);
        dos.writeFloat(precoCongelado);

        return baos.toByteArray();
    }

    // DESSERIALIZAÇÃO
    @Override
    public void fromByteArray(byte[] ba) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.idPedido = dis.readInt();
        this.idProduto = dis.readInt();
        this.quantidade = dis.readInt();
        this.precoCongelado = dis.readFloat();
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

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public float getPrecoCongelado() {
        return precoCongelado;
    }

    public void setPrecoCongelado(float precoCongelado) {
        this.precoCongelado = precoCongelado;
    }
}