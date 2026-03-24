package util;

import model.Registro;
import dao.FileDAO;
import java.io.*;
import java.util.*;


public class OrdenacaoExternaHelper {
    
    private static final int TAMANHO_BLOCO_MEMORIA = 500;

    public static <T extends Registro> List<T> ordenarArquivo(
            FileDAO<T> dao,
            Comparator<T> comparador,
            Class<T> classeRegistro) throws Exception {
        
        File tempDir = new File("dados/temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        
        System.out.println("[Ordenação Externa] Iniciando ordenação...");
        long inicio = System.currentTimeMillis();
        
        List<String> runs = criarRunsOrdenadas(dao, comparador, classeRegistro);
        
        if (runs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<T> resultado = intercalarRuns(runs, comparador, classeRegistro);
        
        for (String run : runs) {
            new File(run).delete();
        }
        
        long fim = System.currentTimeMillis();
        System.out.println("[Ordenação Externa] Concluída em " + (fim - inicio) + "ms. " + 
                          resultado.size() + " registros ordenados.");
        
        return resultado;
    }
    
    private static <T extends Registro> List<String> criarRunsOrdenadas(
            FileDAO<T> dao,
            Comparator<T> comparador,
            Class<T> classeRegistro) throws Exception {
        
        List<String> runs = new ArrayList<>();
        int numeroRun = 0;
        
        dao.resetPointer();
        
        while (true) {
            List<T> bloco = dao.lerProximoBloco(TAMANHO_BLOCO_MEMORIA);
            
            if (bloco.isEmpty()) {
                break;
            }
            
            System.out.println("[Ordenação Externa] Run " + numeroRun + ": " + 
                              bloco.size() + " registros lidos");
            
            bloco.sort(comparador);
            
            String nomeRun = "dados/temp/run_" + numeroRun + ".tmp";
            salvarRun(bloco, nomeRun);
            
            runs.add(nomeRun);
            numeroRun++;
        }
        
        System.out.println("[Ordenação Externa] Total de runs criadas: " + runs.size());
        return runs;
    }
    
    private static <T extends Registro> void salvarRun(
            List<T> bloco,
            String nomeArquivo) throws Exception {
        
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(nomeArquivo)))) {
            
            dos.writeInt(bloco.size());
            
            for (T registro : bloco) {
                byte[] dados = registro.toByteArray();
                dos.writeInt(dados.length);
                dos.write(dados);
            }
            
            dos.flush();
        }
    }
    
    private static <T extends Registro> List<T> intercalarRuns(
            List<String> runs,
            Comparator<T> comparador,
            Class<T> classeRegistro) throws Exception {
        
        if (runs.size() == 1) {
            return lerRunCompleta(runs.get(0), classeRegistro);
        }
        
        System.out.println("[Ordenação Externa] Intercalando " + runs.size() + " runs...");
        
        List<T> resultado = new ArrayList<>();
        
        PriorityQueue<ElementoRun<T>> heap = new PriorityQueue<>(
            (e1, e2) -> comparador.compare(e1.registro, e2.registro)
        );
        
        List<LeitorRun> leitores = new ArrayList<>();
        
        try {
            for (String run : runs) {
                LeitorRun leitor = new LeitorRun(run, classeRegistro);
                leitores.add(leitor);
                
                T primeiro = leitor.lerProximo();
                if (primeiro != null) {
                    heap.add(new ElementoRun<>(primeiro, leitor));
                }
            }
            
            // Intercalar enquanto houver elementos
            while (!heap.isEmpty()) {
                ElementoRun<T> elemento = heap.poll();
                resultado.add(elemento.registro);
                
                T proximo = elemento.leitor.lerProximo();
                if (proximo != null) {
                    heap.add(new ElementoRun<>(proximo, elemento.leitor));
                }
            }
            
        } finally {
            // Fechar todos os leitores
            for (LeitorRun leitor : leitores) {
                try { leitor.fechar(); } catch (Exception e) {}
            }
        }
        
        return resultado;
    }
    

    private static <T extends Registro> List<T> lerRunCompleta(
            String nomeArquivo,
            Class<T> classeRegistro) throws Exception {
        
        List<T> registros = new ArrayList<>();
        
        try (LeitorRun leitor = new LeitorRun(nomeArquivo, classeRegistro)) {
            T registro;
            while ((registro = leitor.lerProximo()) != null) {
                registros.add(registro);
            }
        }
        
        return registros;
    }
    

    private static class LeitorRun implements AutoCloseable {
        private final DataInputStream dis;
        private final int totalRegistros;
        private int registrosLidos;
        private final Class<? extends Registro> classeRegistro;
        
        LeitorRun(String nomeArquivo, Class<? extends Registro> classeRegistro) throws Exception {
            this.dis = new DataInputStream(new BufferedInputStream(new FileInputStream(nomeArquivo)));
            this.totalRegistros = dis.readInt();
            this.registrosLidos = 0;
            this.classeRegistro = classeRegistro;
        }
        
        @SuppressWarnings("unchecked")
        <T extends Registro> T lerProximo() throws Exception {
            if (registrosLidos >= totalRegistros) {
                return null;
            }
            
            int tamanho = dis.readInt();
            byte[] dados = new byte[tamanho];
            dis.readFully(dados);
            
            T registro = (T) classeRegistro.getDeclaredConstructor().newInstance();
            registro.fromByteArray(dados);
            
            registrosLidos++;
            return registro;
        }
        
        void fechar() throws IOException {
            if (dis != null) {
                dis.close();
            }
        }
        
        @Override
        public void close() throws Exception {
            fechar();
        }
    }
    
    private static class ElementoRun<T extends Registro> {
        final T registro;
        final LeitorRun leitor;
        
        ElementoRun(T registro, LeitorRun leitor) {
            this.registro = registro;
            this.leitor = leitor;
        }
    }
}