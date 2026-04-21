package model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pedido implements Registro {

    private int id;
    private int idUsuario;
    private long dataCompra;
    private String status;
    private float valorTotal;
    
    // NOVO: Lista de itens que compõem o pedido
    private List<ItemPedido> itens;

    // Construtor vazio
    public Pedido() {
        this.id = -1;
        this.idUsuario = -1;
        this.dataCompra = 0L;
        this.status = "";
        this.valorTotal = 0.0f;
        this.itens = new ArrayList<>(); // Inicializa a lista
    }

    // Construtor com parâmetros
    public Pedido(int idUsuario, long dataCompra, String status, float valorTotal) {
        this.id = -1;
        this.idUsuario = idUsuario;
        this.dataCompra = dataCompra;
        this.status = status;
        this.valorTotal = valorTotal;
        this.itens = new ArrayList<>();
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
        // Valida se há itens no pedido
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter ao menos um item.");
        }
    }

    // SERIALIZAÇÃO (Atualizada para incluir os itens)
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeInt(idUsuario);
        dos.writeLong(dataCompra);
        dos.writeUTF(status);
        dos.writeFloat(valorTotal);

        // Salva a quantidade de itens e depois cada item
        dos.writeInt(itens.size());
        for (ItemPedido ip : itens) {
            byte[] ba = ip.toByteArray();
            dos.writeInt(ba.length);
            dos.write(ba);
        }

        return baos.toByteArray();
    }

    // DESSERIALIZAÇÃO (Atualizada para ler os itens)
    @Override
    public void fromByteArray(byte[] ba) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(ba);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.idUsuario = dis.readInt();
        this.dataCompra = dis.readLong();
        this.status = dis.readUTF();
        this.valorTotal = dis.readFloat();

        // Lê a quantidade de itens e reconstrói a lista
        int qtdItens = dis.readInt();
        this.itens = new ArrayList<>();
        for (int i = 0; i < qtdItens; i++) {
            int tam = dis.readInt();
            byte[] baItem = new byte[tam];
            dis.read(baItem);
            ItemPedido ip = new ItemPedido();
            ip.fromByteArray(baItem);
            this.itens.add(ip);
        }
    }

    // GETTERS E SETTERS
    
    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    @Override
    public int getId() { return id; }

    @Override
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }

    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public long getDataCompra() { return dataCompra; }

    public void setDataCompra(long dataCompra) { this.dataCompra = dataCompra; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public float getValorTotal() { return valorTotal; }

    public void setValorTotal(float valorTotal) { this.valorTotal = valorTotal; }
}