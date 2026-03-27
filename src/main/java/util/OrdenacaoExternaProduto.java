package util;

import model.Produto;
import java.io.*;
import java.util.*;

public class OrdenacaoExternaProduto {

    // Simulando uma memória RAM minúscula (cabe apenas 5 produtos de cada vez)
    // Isso forçará a criação de vários arquivos temporários para provar que a lógica funciona!
    private static final int TAMANHO_BLOCO = 5; 
    
    private static final String DIR_REGISTROS = "dados/registros/";
    private static final String DIR_TEMP = "dados/temp/";
    private static final String DIR_RELATORIOS = "dados/relatorios/";

    public void ordenarPorPreco() {
        System.out.println("Iniciando Ordenação Externa de Produtos por Preço...");
        
        File dirTemp = new File(DIR_TEMP);
        if (!dirTemp.exists()) dirTemp.mkdirs();
        
        File dirRelat = new File(DIR_RELATORIOS);
        if (!dirRelat.exists()) dirRelat.mkdirs();

        try {
            int qtdBlocos = gerarBlocosOrdenados();
            if (qtdBlocos > 0) {
                intercalarBlocos(qtdBlocos);
                System.out.println("Ordenação concluída! Arquivo 'produto_ordenado.db' gerado.");
            } else {
                System.out.println("Nenhum produto válido encontrado para ordenar.");
            }
        } catch (Exception e) {
            System.err.println("Erro na ordenação externa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // FASE 1: LER DO DISCO E GERAR ARQUIVOS TEMP
    private int gerarBlocosOrdenados() throws Exception {
        File arquivoOriginal = new File(DIR_REGISTROS + "produto.db");
        if (!arquivoOriginal.exists()) return 0;

        RandomAccessFile arqOrigem = new RandomAccessFile(arquivoOriginal, "r");
        if (arqOrigem.length() == 0) {
            arqOrigem.close();
            return 0;
        }

        arqOrigem.seek(4); // Pula o cabeçalho
        List<Produto> blocoMemoria = new ArrayList<>();
        int contadorBlocos = 0;

        while (arqOrigem.getFilePointer() < arqOrigem.length()) {
            byte lapide = arqOrigem.readByte();
            int tamanho = arqOrigem.readInt();

            if (lapide == ' ') {
                byte[] bytes = new byte[tamanho];
                arqOrigem.read(bytes);
                Produto p = new Produto();
                p.fromByteArray(bytes);
                blocoMemoria.add(p);
            } else {
                arqOrigem.skipBytes(tamanho); // Ignora produtos deletados
            }

            // Quando a "RAM" encher ou o arquivo acabar
            if (blocoMemoria.size() == TAMANHO_BLOCO || arqOrigem.getFilePointer() >= arqOrigem.length()) {
                if (!blocoMemoria.isEmpty()) {
                    blocoMemoria.sort((p1, p2) -> Float.compare(p1.getPreco(), p2.getPreco()));
                    salvarBlocoNoDisco(blocoMemoria, contadorBlocos);
                    contadorBlocos++;
                    blocoMemoria.clear();
                }
            }
        }
        arqOrigem.close();
        return contadorBlocos;
    }

    private void salvarBlocoNoDisco(List<Produto> lista, int numeroBloco) throws Exception {
        RandomAccessFile arqTemp = new RandomAccessFile(DIR_TEMP + "bloco_" + numeroBloco + ".tmp", "rw");
        for (Produto p : lista) {
            byte[] bytes = p.toByteArray();
            arqTemp.writeByte(' '); 
            arqTemp.writeInt(bytes.length);
            arqTemp.write(bytes);
        }
        arqTemp.close();
    }

    // FASE 2: JUNTAR OS ARQUIVOS TEMP (MERGE)
    private void intercalarBlocos(int qtdBlocos) throws Exception {
        RandomAccessFile[] arquivosTemp = new RandomAccessFile[qtdBlocos];
        Produto[] produtosAtuais = new Produto[qtdBlocos];
        
        for (int i = 0; i < qtdBlocos; i++) {
            arquivosTemp[i] = new RandomAccessFile(DIR_TEMP + "bloco_" + i + ".tmp", "r");
            produtosAtuais[i] = lerProximoProduto(arquivosTemp[i]);
        }

        RandomAccessFile arqDestino = new RandomAccessFile(DIR_RELATORIOS + "produto_ordenado.db", "rw");
        arqDestino.setLength(0); 
        arqDestino.writeInt(0); 

        while (true) {
            int indiceMenor = -1;
            float menorPreco = Float.MAX_VALUE;

            for (int i = 0; i < qtdBlocos; i++) {
                if (produtosAtuais[i] != null && produtosAtuais[i].getPreco() < menorPreco) {
                    menorPreco = produtosAtuais[i].getPreco();
                    indiceMenor = i;
                }
            }

            if (indiceMenor == -1) break; // Acabou tudo

            Produto vencedor = produtosAtuais[indiceMenor];
            byte[] bytesVencedor = vencedor.toByteArray();
            arqDestino.writeByte(' ');
            arqDestino.writeInt(bytesVencedor.length);
            arqDestino.write(bytesVencedor);

            produtosAtuais[indiceMenor] = lerProximoProduto(arquivosTemp[indiceMenor]);
        }

        arqDestino.close();

        for (int i = 0; i < qtdBlocos; i++) {
            arquivosTemp[i].close();
            new File(DIR_TEMP + "bloco_" + i + ".tmp").delete();
        }
    }

    private Produto lerProximoProduto(RandomAccessFile arq) throws Exception {
        if (arq.getFilePointer() < arq.length()) {
            arq.readByte(); 
            int tamanho = arq.readInt();
            byte[] bytes = new byte[tamanho];
            arq.read(bytes);
            Produto p = new Produto();
            p.fromByteArray(bytes);
            return p;
        }
        return null;
    }
}