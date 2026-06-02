package service;

import java.io.*;
import java.util.HashMap;

import spark.Request;
import spark.Response;
import util.Huffman;
import util.LZW;

public class BackupService {

    private static final String PASTA_REGISTROS = "dados/registros/";
    private static final String PASTA_BACKUP = "dados/backup/";

    public static long obterTamanhoOriginal() {
        long total = 0;
        
        java.io.File pastaRegistros = new java.io.File("dados/registros/");
        if (pastaRegistros.exists() && pastaRegistros.listFiles() != null) {
            for (java.io.File arq : pastaRegistros.listFiles()) total += arq.length();
        }
        
        java.io.File pastaIndices = new java.io.File("dados/indices/");
        if (pastaIndices.exists() && pastaIndices.listFiles() != null) {
            for (java.io.File arq : pastaIndices.listFiles()) total += arq.length();
        }
        
        return total;
    }

 // Junta TODAS as tabelas e TODOS os índices (Hash e Árvores) num pacote só
    private static byte[] consolidarArquivos() throws IOException {
        java.io.File pastaRegistros = new java.io.File("dados/registros/");
        java.io.File pastaIndices = new java.io.File("dados/indices/");
        
        java.util.List<java.io.File> todosArquivos = new java.util.ArrayList<>();
        
        // Pega os arquivos de dados (.db)
        if (pastaRegistros.exists() && pastaRegistros.listFiles() != null) {
            todosArquivos.addAll(java.util.Arrays.asList(pastaRegistros.listFiles()));
        }
        // Pega os arquivos de índice (.hash_d, .hash_b, .btree)
        if (pastaIndices.exists() && pastaIndices.listFiles() != null) {
            todosArquivos.addAll(java.util.Arrays.asList(pastaIndices.listFiles()));
        }

        if (todosArquivos.isEmpty()) {
            throw new IOException("Nenhum arquivo encontrado para backup.");
        }

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);

        dos.writeInt(todosArquivos.size());

        for (java.io.File arq : todosArquivos) {
            // Em vez de salvar só "produto.db", salva "registros/produto.db" ou "indices/produto_usuario_n_n.btree"
            String caminhoRelativo = arq.getParentFile().getName() + "/" + arq.getName(); 
            dos.writeUTF(caminhoRelativo);
            dos.writeLong(arq.length()); 
            
            byte[] conteudo = new byte[(int) arq.length()];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(arq)) {
                fis.read(conteudo);
            }
            dos.write(conteudo);
        }

        dos.flush();
        return baos.toByteArray();
    }

    // Método que será chamado pela rota Huffman
    public Object criarBackupHuffman(Request req, Response res) {
        try {
            long tamanhoOriginal = obterTamanhoOriginal();
            byte[] dadosConsolidados = consolidarArquivos();

            HashMap<Byte, String> codigos = Huffman.codifica(dadosConsolidados);
            byte[] dadosComprimidos = Huffman.codificar(dadosConsolidados, codigos);

            File diretorioBkp = new File(PASTA_BACKUP);
            if(!diretorioBkp.exists()) diretorioBkp.mkdirs();

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PASTA_BACKUP + "backup_huffman.backup"))) {
                oos.writeObject(codigos); 
                oos.writeObject(dadosComprimidos);
            }

            File arqComp = new File(PASTA_BACKUP + "backup_huffman.backup");
            long tamanhoComprimido = arqComp.length();

            res.status(200);
            return String.format("{\"status\":\"sucesso\",\"original\":%d,\"comprimido\":%d}", 
                    tamanhoOriginal, tamanhoComprimido);
            
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\":\"Falha ao gerar backup Huffman: " + e.getMessage() + "\"}";
        }
    }

    // Método que será chamado pela rota LZW
    public Object criarBackupLZW(Request req, Response res) {
        try {
            long tamanhoOriginal = obterTamanhoOriginal();
            byte[] dadosConsolidados = consolidarArquivos();

            byte[] dadosComprimidos = LZW.codificar(dadosConsolidados);

            File diretorioBkp = new File(PASTA_BACKUP);
            if(!diretorioBkp.exists()) diretorioBkp.mkdirs();

            try (FileOutputStream fos = new FileOutputStream(PASTA_BACKUP + "backup_lzw.backup")) {
                fos.write(dadosComprimidos);
            }

            File arqComp = new File(PASTA_BACKUP + "backup_lzw.backup");
            long tamanhoComprimido = arqComp.length();

            res.status(200);
            return String.format("{\"status\":\"sucesso\",\"original\":%d,\"comprimido\":%d}", 
                    tamanhoOriginal, tamanhoComprimido);
            
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\":\"Falha ao gerar backup LZW: " + e.getMessage() + "\"}";
        }
    }
    
    // MÉTODOS DE RESTAURAÇÃO (DESCOMPACTAÇÃO)
    // Separa o vetor de bytes único de volta nos vários arquivos .db
    // Restaura cada arquivo exatamente para a sua pasta original (Registros ou Indices)
    private void desconsolidarArquivos(byte[] dadosRestaurados) throws IOException {
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(dadosRestaurados);
        java.io.DataInputStream dis = new java.io.DataInputStream(bais);

        int qtdArquivos = dis.readInt();

        for (int i = 0; i < qtdArquivos; i++) {
            String caminhoRelativo = dis.readUTF(); // Vai ler "registros/produto.db" ou "indices/produto.db.hash_d"
            long tamanho = dis.readLong();
            
            byte[] conteudo = new byte[(int) tamanho];
            dis.readFully(conteudo);

            java.io.File arq = new java.io.File("dados/" + caminhoRelativo);
            // Garante que a pasta destino (registros ou indices) exista antes de salvar
            arq.getParentFile().mkdirs(); 
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(arq)) {
                fos.write(conteudo);
            }
        }
    }

    // Rota de Restauração Huffman
    @SuppressWarnings("unchecked")
    public Object restaurarBackupHuffman(Request req, Response res) {
        try {
            File arq = new File(PASTA_BACKUP + "backup_huffman.backup");
            if (!arq.exists()) {
                res.status(404);
                return "{\"erro\":\"Arquivo de backup Huffman não encontrado.\"}";
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arq))) {
                HashMap<Byte, String> codigos = (HashMap<Byte, String>) ois.readObject();
                byte[] dadosComprimidos = (byte[]) ois.readObject();

                byte[] dadosRestaurados = Huffman.decodificar(dadosComprimidos, codigos);
                desconsolidarArquivos(dadosRestaurados);
            }

            res.status(200);
            return "{\"status\":\"sucesso\", \"msg\":\"Banco de dados restaurado via Huffman!\"}";
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\":\"Falha ao restaurar Huffman: " + e.getMessage() + "\"}";
        }
    }

    // Rota de Restauração LZW
    public Object restaurarBackupLZW(Request req, Response res) {
        try {
            File arq = new File(PASTA_BACKUP + "backup_lzw.backup");
            if (!arq.exists()) {
                res.status(404);
                return "{\"erro\":\"Arquivo de backup LZW não encontrado.\"}";
            }

            byte[] dadosComprimidos = new byte[(int) arq.length()];
            try (FileInputStream fis = new FileInputStream(arq)) {
                fis.read(dadosComprimidos);
            }

            byte[] dadosRestaurados = LZW.decodificar(dadosComprimidos);
            desconsolidarArquivos(dadosRestaurados);

            res.status(200);
            return "{\"status\":\"sucesso\", \"msg\":\"Banco de dados restaurado via LZW!\"}";
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\":\"Falha ao restaurar LZW: " + e.getMessage() + "\"}";
        }
    }
    public Object auditoriaHuffman(Request req, Response res) {
        try {
            // 1. Pega os dados originais reais
            byte[] original = consolidarArquivos();
            
            // 2. Comprime
            HashMap<Byte, String> codigos = Huffman.codifica(original);
            byte[] comprimido = Huffman.codificar(original, codigos);
            
            // 3. Descomprime (A matriz voltará com lixo de padding no final)
            byte[] restauradoComLixo = Huffman.decodificar(comprimido, codigos);

            // 4. O PULO DO GATO: Apara a matriz para remover os bits de preenchimento (padding) do Huffman
            byte[] restauradoLimpo = java.util.Arrays.copyOf(restauradoComLixo, original.length);

            // 5. A Prova Real: Compara byte a byte
            boolean perfeito = java.util.Arrays.equals(original, restauradoLimpo);
            
            res.status(200);
            return String.format("{\"algoritmo\":\"Huffman\", \"integridade_100_porcento\": %b}", perfeito);
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\":\"Falha na auditoria Huffman: " + e.getMessage() + "\"}";
        }
    }

    public Object auditoriaLZW(Request req, Response res) {
        try {
            // 1. Pega originais
            byte[] original = consolidarArquivos();
            
            // 2. Comprime
            byte[] comprimido = LZW.codificar(original);
            
            // 3. Descomprime
            byte[] restaurado = LZW.decodificar(comprimido);

            // 4. Compara byte a byte
            boolean perfeito = java.util.Arrays.equals(original, restaurado);
            
            res.status(200);
            return String.format("{\"algoritmo\":\"LZW\", \"integridade_100_porcento\": %b}", perfeito);
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\":\"Falha na auditoria LZW: " + e.getMessage() + "\"}";
        }
    }
}