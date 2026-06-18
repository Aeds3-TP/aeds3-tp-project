package utilidades;

import java.util.*;
import model.*;

public class KMP {
    private static int[] computarLPS(String padrao) {
        // Função que calcula o sufixo e o prefixo mais longo
        int m = padrao.length();
        int[] lps = new int[m];
        
        int len = 0;
        int i = 1;
        
        lps[0] = 0;

        while (i < m) {
            if (padrao.charAt(i) == padrao.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        return lps;
    }

    public static boolean pesquisar(String texto, String padrao) {
        if (padrao == null || padrao.isEmpty()) return false;
        if (texto == null || texto.isEmpty()) return false;

        int n = texto.length();
        int m = padrao.length();

        // Pré-processa o padrão para construir a tabela LPS
        int[] lps = computarLPS(padrao);

        int i = 0; // índice do texto
        int j = 0; // índice do padrão

        while (i < n) {
            if (padrao.charAt(j) == texto.charAt(i)) {
                j++;
                i++;
            }
            if (j == m) {
                return true;
            } else if (i < n && padrao.charAt(j) != texto.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        return false;
    }

    //Pesquisa sem case
    public static boolean pesquisarIgnoreCase(String texto, String padrao) {
        if (texto == null || padrao == null) return false;
        return pesquisar(texto.toLowerCase(), padrao.toLowerCase());
    }


    public ArrayList<Receita> pesquisar(ArrayList<Receita> texto, String padrao){
        ArrayList<Receita> compativeis = new ArrayList<>();
        for(Receita palavra: texto){
            if(pesquisarIgnoreCase(palavra.getDescricao(), padrao)){
                compativeis.add(palavra);
            }
        }
        return compativeis;
    }
}