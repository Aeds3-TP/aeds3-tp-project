package util;

public class VetorDeBits {
    private byte[] bytes;
    private int numBits;

    // Construtor vazio (usado pelo Huffman)
    public VetorDeBits() {
        this.bytes = new byte[16]; // Tamanho inicial, cresce dinamicamente
        this.numBits = 0;
    }

    // Construtor com capacidade (usado pelo LZW)
    public VetorDeBits(int capacidadeBits) {
        this.bytes = new byte[(capacidadeBits + 7) / 8];
        this.numBits = capacidadeBits;
    }

    // Construtor para leitura (usado na decodificação)
    public VetorDeBits(byte[] dados) {
        this.bytes = dados;
        this.numBits = dados.length * 8;
    }

    private void garantirCapacidade(int indiceBit) {
        int indiceByte = indiceBit / 8;
        if (indiceByte >= bytes.length) {
            byte[] novosBytes = new byte[Math.max(bytes.length * 2, indiceByte + 1)];
            System.arraycopy(bytes, 0, novosBytes, 0, bytes.length);
            bytes = novosBytes;
        }
    }

    public void set(int i) {
        garantirCapacidade(i);
        bytes[i / 8] |= (1 << (7 - (i % 8)));
        if (i >= numBits) numBits = i + 1;
    }

    public void clear(int i) {
        garantirCapacidade(i);
        bytes[i / 8] &= ~(1 << (7 - (i % 8)));
        if (i >= numBits) numBits = i + 1;
    }

    public boolean get(int i) {
        if (i >= numBits) return false;
        return (bytes[i / 8] & (1 << (7 - (i % 8)))) != 0;
    }

    public int length() {
        return numBits;
    }

    public byte[] toByteArray() {
        int tamanhoBytes = (numBits + 7) / 8;
        byte[] resultado = new byte[tamanhoBytes];
        System.arraycopy(bytes, 0, resultado, 0, tamanhoBytes);
        return resultado;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numBits; i++) {
            sb.append(get(i) ? "1" : "0");
        }
        return sb.toString();
    }
}