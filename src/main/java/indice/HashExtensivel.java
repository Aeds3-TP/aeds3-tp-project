package indice;

import java.io.*;

public class HashExtensivel {

    private String nomeArquivoDiretorio;
    private String nomeArquivoBuckets;
    private RandomAccessFile arqDiretorio;
    private RandomAccessFile arqBuckets;
    private int quantidadeDadosPorBucket;
    private Diretorio diretorio;

    public HashExtensivel(int qtdDadosPorBucket, String nomeArqDiretorio, String nomeArqBuckets) throws Exception {
        this.quantidadeDadosPorBucket = qtdDadosPorBucket;
        this.nomeArquivoDiretorio = nomeArqDiretorio;
        this.nomeArquivoBuckets = nomeArqBuckets;

        // Cria as pastas se não existirem
        File fileDir = new File(nomeArquivoDiretorio);
        if (fileDir.getParentFile() != null && !fileDir.getParentFile().exists()) {
            fileDir.getParentFile().mkdirs();
        }

        arqDiretorio = new RandomAccessFile(nomeArquivoDiretorio, "rw");
        arqBuckets = new RandomAccessFile(nomeArquivoBuckets, "rw");

        // Se estiver vazio, inicializa o Diretório e o primeiro Bucket
        if (arqDiretorio.length() == 0 || arqBuckets.length() == 0) {
            diretorio = new Diretorio();
            arqDiretorio.write(diretorio.toByteArray());

            Bucket b = new Bucket(quantidadeDadosPorBucket);
            arqBuckets.seek(0);
            arqBuckets.write(b.toByteArray());
        } else {
            // Carrega o diretório existente para a memória
            byte[] bd = new byte[(int) arqDiretorio.length()];
            arqDiretorio.seek(0);
            arqDiretorio.read(bd);
            diretorio = new Diretorio();
            diretorio.fromByteArray(bd);
        }
    }

    public boolean create(int id, long enderecoByte) throws Exception {
        int i = diretorio.hash(id);
        long enderecoBucket = diretorio.endereco(i);
        
        Bucket b = new Bucket(quantidadeDadosPorBucket);
        byte[] ba = new byte[b.tamanhoEmBytes()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        b.fromByteArray(ba);

        // Se já existe, não insere
        if (b.read(id) != -1) return false;

        // Se tem espaço, insere e salva
        if (!b.full()) {
            b.create(id, enderecoByte);
            arqBuckets.seek(enderecoBucket);
            arqBuckets.write(b.toByteArray());
            return true;
        }

        // SPLIT (RACHAMENTO)
        
        byte pl = b.profundidadeLocal;
        if (pl >= diretorio.profundidadeGlobal) {
            diretorio.duplica();
        }
        byte pg = diretorio.profundidadeGlobal;

        // Cria os dois novos buckets
        Bucket b1 = new Bucket(quantidadeDadosPorBucket, pl + 1);
        arqBuckets.seek(enderecoBucket);
        arqBuckets.write(b1.toByteArray());

        Bucket b2 = new Bucket(quantidadeDadosPorBucket, pl + 1);
        long novoEndereco = arqBuckets.length();
        arqBuckets.seek(novoEndereco);
        arqBuckets.write(b2.toByteArray());

        // Atualiza os ponteiros no diretório
        int inicio = diretorio.hash2(id, b.profundidadeLocal);
        int deslocamento = (int) Math.pow(2, pl);
        int max = (int) Math.pow(2, pg);
        boolean troca = false;
        
        for (int j = inicio; j < max; j += deslocamento) {
            if (troca) diretorio.atualizaEndereco(j, novoEndereco);
            troca = !troca;
        }

        // Salva o diretório atualizado
        byte[] bd = diretorio.toByteArray();
        arqDiretorio.seek(0);
        arqDiretorio.write(bd);

        // Reinsere os elementos do bucket antigo que "rachou"
        for (int j = 0; j < b.quantidade; j++) {
            create(b.chaves[j], b.enderecos[j]);
        }
        // Tenta inserir o elemento novo de novo
        create(id, enderecoByte); 
        return true;
    }

    public long read(int id) throws Exception {
        int i = diretorio.hash(id);
        long enderecoBucket = diretorio.endereco(i);
        
        Bucket b = new Bucket(quantidadeDadosPorBucket);
        byte[] ba = new byte[b.tamanhoEmBytes()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        b.fromByteArray(ba);

        return b.read(id);
    }

    public boolean update(int id, long novoEnderecoByte) throws Exception {
        int i = diretorio.hash(id);
        long enderecoBucket = diretorio.endereco(i);
        
        Bucket b = new Bucket(quantidadeDadosPorBucket);
        byte[] ba = new byte[b.tamanhoEmBytes()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        b.fromByteArray(ba);

        if (b.update(id, novoEnderecoByte)) {
            arqBuckets.seek(enderecoBucket);
            arqBuckets.write(b.toByteArray());
            return true;
        }
        return false;
    }

    public boolean delete(int id) throws Exception {
        int i = diretorio.hash(id);
        long enderecoBucket = diretorio.endereco(i);
        
        Bucket b = new Bucket(quantidadeDadosPorBucket);
        byte[] ba = new byte[b.tamanhoEmBytes()];
        arqBuckets.seek(enderecoBucket);
        arqBuckets.read(ba);
        b.fromByteArray(ba);

        if (b.delete(id)) {
            arqBuckets.seek(enderecoBucket);
            arqBuckets.write(b.toByteArray());
            return true;
        }
        return false;
    }

    // CLASSES INTERNAS (Diretório e Bucket)

    private class Diretorio {
        byte profundidadeGlobal;
        long[] enderecos;

        public Diretorio() {
            profundidadeGlobal = 0;
            enderecos = new long[1];
            enderecos[0] = 0;
        }

        public boolean atualizaEndereco(int p, long e) {
            if (p >= enderecos.length) return false;
            enderecos[p] = e;
            return true;
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(profundidadeGlobal);
            for (int i = 0; i < enderecos.length; i++) {
                dos.writeLong(enderecos[i]);
            }
            return baos.toByteArray();
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            profundidadeGlobal = dis.readByte();
            int quantidade = (int) Math.pow(2, profundidadeGlobal);
            enderecos = new long[quantidade];
            for (int i = 0; i < quantidade; i++) {
                enderecos[i] = dis.readLong();
            }
        }

        protected long endereco(int p) {
            if (p >= enderecos.length) return -1;
            return enderecos[p];
        }

        protected boolean duplica() {
            if (profundidadeGlobal == 127) return false;
            profundidadeGlobal++;
            int q1 = (int) Math.pow(2, profundidadeGlobal - 1);
            int q2 = (int) Math.pow(2, profundidadeGlobal);
            long[] novosEnderecos = new long[q2];
            
            for (int i = 0; i < q1; i++) novosEnderecos[i] = enderecos[i];
            for (int i = q1; i < q2; i++) novosEnderecos[i] = enderecos[i - q1];
            
            enderecos = novosEnderecos;
            return true;
        }

        protected int hash(int chave) {
            return chave % (int) Math.pow(2, profundidadeGlobal);
        }

        protected int hash2(int chave, int pl) {
            return chave % (int) Math.pow(2, pl);
        }
    }

    private class Bucket {
        byte profundidadeLocal;
        short quantidade;
        short quantidadeMaxima;
        int[] chaves;
        long[] enderecos;

        public Bucket(int qtdmax) {
            this(qtdmax, 0);
        }

        public Bucket(int qtdmax, int pl) {
            profundidadeLocal = (byte) pl;
            quantidade = 0;
            quantidadeMaxima = (short) qtdmax;
            chaves = new int[quantidadeMaxima];
            enderecos = new long[quantidadeMaxima];
        }

        public byte[] toByteArray() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeByte(profundidadeLocal);
            dos.writeShort(quantidade);
            for (int i = 0; i < quantidadeMaxima; i++) {
                if (i < quantidade) {
                    dos.writeInt(chaves[i]);
                    dos.writeLong(enderecos[i]);
                } else {
                    dos.writeInt(-1);
                    dos.writeLong(-1);
                }
            }
            return baos.toByteArray();
        }

        public void fromByteArray(byte[] ba) throws IOException {
            ByteArrayInputStream bais = new ByteArrayInputStream(ba);
            DataInputStream dis = new DataInputStream(bais);
            profundidadeLocal = dis.readByte();
            quantidade = dis.readShort();
            for (int i = 0; i < quantidadeMaxima; i++) {
                chaves[i] = dis.readInt();
                enderecos[i] = dis.readLong();
            }
        }

        public boolean create(int chave, long endereco) {
            if (full()) return false;
            int i = 0;
            while (i < quantidade && chaves[i] < chave) i++;
            
            // Desloca os elementos para manter ordenado
            for (int j = quantidade; j > i; j--) {
                chaves[j] = chaves[j - 1];
                enderecos[j] = enderecos[j - 1];
            }
            chaves[i] = chave;
            enderecos[i] = endereco;
            quantidade++;
            return true;
        }

        public long read(int chave) {
            if (empty()) return -1;
            for (int i = 0; i < quantidade; i++) {
                if (chaves[i] == chave) return enderecos[i];
            }
            return -1;
        }

        public boolean update(int chave, long novoEndereco) {
            for (int i = 0; i < quantidade; i++) {
                if (chaves[i] == chave) {
                    enderecos[i] = novoEndereco;
                    return true;
                }
            }
            return false;
        }

        public boolean delete(int chave) {
            for (int i = 0; i < quantidade; i++) {
                if (chaves[i] == chave) {
                    // Puxa os elementos da direita para a esquerda
                    for (int j = i; j < quantidade - 1; j++) {
                        chaves[j] = chaves[j + 1];
                        enderecos[j] = enderecos[j + 1];
                    }
                    quantidade--;
                    return true;
                }
            }
            return false;
        }

        public boolean empty() { return quantidade == 0; }
        public boolean full() { return quantidade == quantidadeMaxima; }
        public int tamanhoEmBytes() { return 1 + 2 + (quantidadeMaxima * 12); } 
    }
}