package util;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.PriorityQueue;

class HuffmanNode implements Comparable<HuffmanNode> {
    byte b;
    int frequencia;
    HuffmanNode esquerdo, direito;

    public HuffmanNode(byte b, int f) {
        this.b = b;
        this.frequencia = f;
        esquerdo = direito = null;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return this.frequencia - o.frequencia;
    }
}

public class Huffman {

    public static HashMap<Byte, String> codifica(byte[] sequencia) {
        HashMap<Byte, Integer> mapaDeFrequencias = new HashMap<>();
        for (byte c : sequencia) {
            mapaDeFrequencias.put(c, mapaDeFrequencias.getOrDefault(c, 0) + 1);
        }

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Byte b : mapaDeFrequencias.keySet()) {
            pq.add(new HuffmanNode(b, mapaDeFrequencias.get(b)));
        }

        if (pq.isEmpty()) return new HashMap<>();

        while (pq.size() > 1) {
            HuffmanNode esquerdo = pq.poll();
            HuffmanNode direito = pq.poll();

            HuffmanNode pai = new HuffmanNode((byte)0, esquerdo.frequencia + direito.frequencia);
            pai.esquerdo = esquerdo;
            pai.direito = direito;

            pq.add(pai);
        }

        HuffmanNode raiz = pq.poll();
        HashMap<Byte, String> codigos = new HashMap<>();
        constroiCodigos(raiz, "", codigos);

        return codigos;
    }

    private static void constroiCodigos(HuffmanNode no, String codigo, HashMap<Byte, String> codigos) {
        if (no == null) return;
        if (no.esquerdo == null && no.direito == null) {
            codigos.put(no.b, codigo.length() > 0 ? codigo : "0");
            return;
        }
        constroiCodigos(no.esquerdo, codigo + "0", codigos);
        constroiCodigos(no.direito, codigo + "1", codigos);
    }

    public static byte[] codificar(byte[] dados, HashMap<Byte, String> codigos) {
        VetorDeBits sequenciaCodificada = new VetorDeBits();
        int i = 0;
        for (byte b : dados) {
            String codigo = codigos.get(b);
            if(codigo != null) {
                for(char c : codigo.toCharArray()) {
                    if(c == '0') sequenciaCodificada.clear(i++);
                    else sequenciaCodificada.set(i++);
                }
            }
        }
        return sequenciaCodificada.toByteArray();
    }

    public static byte[] decodificar(byte[] dadosCompactados, HashMap<Byte, String> codigos) {
        VetorDeBits vetorBits = new VetorDeBits(dadosCompactados);
        String sequenciaBits = vetorBits.toString(); 
        
        ByteArrayOutputStream sequenciaDecodificada = new ByteArrayOutputStream();
        StringBuilder codigoAtual = new StringBuilder();

        HashMap<String, Byte> decodificador = new HashMap<>();
        for(java.util.Map.Entry<Byte, String> entry : codigos.entrySet()) {
            decodificador.put(entry.getValue(), entry.getKey());
        }

        for (int i = 0; i < sequenciaBits.length(); i++) {
            codigoAtual.append(sequenciaBits.charAt(i));
            if (decodificador.containsKey(codigoAtual.toString())) {
                sequenciaDecodificada.write(decodificador.get(codigoAtual.toString()));
                codigoAtual = new StringBuilder();
            }
        }
        return sequenciaDecodificada.toByteArray();
    }
}