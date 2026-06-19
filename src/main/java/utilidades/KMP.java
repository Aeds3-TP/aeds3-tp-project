package utilidades;

import java.util.*;
import model.Produto;

public class KMP {
    private static int[] computarLPS(String padrao) {
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

        int[] lps = computarLPS(padrao);

        int i = 0; 
        int j = 0; 

        while (i < n) {
            if (padrao.charAt(j) == texto.charAt(i)) {
                i++;
                j++;
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

    public static boolean pesquisarIgnoreCase(String texto, String padrao) {
        if (texto == null || padrao == null) return false;
        return pesquisar(texto.toLowerCase(), padrao.toLowerCase());
    }
    public static List<Produto> pesquisarEmProdutos(List<Produto> produtos, String padrao) {
        List<Produto> encontrados = new ArrayList<>();
        for (Produto p : produtos) {
            if (pesquisarIgnoreCase(p.getNome(), padrao)) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }
}
