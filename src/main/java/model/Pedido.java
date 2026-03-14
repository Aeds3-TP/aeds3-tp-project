package model;

import java.io.*;

public class Pedido implements Registro {

    private int id;
    private int idUsuario;
    private long dataCompra;
    private String status;
    private float valorTotal;

    // Construtor vazio
    public Pedido() {
        this.id = -1;
        this.idUsuario = -1;
        this.dataCompra = 0L;
        this.status = "";
        this.valorTotal = 0.0f;
    }

    // Construtor com parâmetros
    public Pedido(int idUsuario, long dataCompra, String status, float valorTotal) {
        this.id = -1;
        this.idUsuario = idUsuario;
        this.dataCompra = dataCompra;
        this.status = status;
        this.valorTotal = valorTotal;
    }

    // Validação
    public void validar() throws IllegalArgumentException {

        if (idUsuario <= 0) {
            throw new IllegalArgumentException("Usuário inválido.");
        }

        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status do pedido é obrigatório.");
        }

        if (valorTotal < 0) {
            throw new IllegalArgumentException("Valor total não pode ser negativo.");
        }
    }

    // SERIALIZAÇÃO
    @Override
    public byte[] toByteArray() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeInt(idUsuario);
        dos.writeLong(dataCompra);
        dos.writeUTF(status);
        dos.writeFloat(valorTotal);

        return baos.toByteArray();
    }

    // DESSERIALIZAÇÃO
    @Override
    public void fromByteArray(byte[] ba) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.idUsuario = dis.readInt();
        this.dataCompra = dis.readLong();
        this.status = dis.readUTF();
        this.valorTotal = dis.readFloat();
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

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public long getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(long dataCompra) {
        this.dataCompra = dataCompra;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public float getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(float valorTotal) {
        this.valorTotal = valorTotal;
    }
}