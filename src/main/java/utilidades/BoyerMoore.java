package utilidades;

import java.util.*;
import model.Produto;

public class BoyerMoore {
    private static final int ALFABETO = 256; 

    private static int[] criarTabelaBadChar(String padrao) {
        int[] badChar = new int[ALFABETO];
        int m = padrao.length();

        Arrays.fill(badChar, -1);

        for (int i = 0; i < m; i++) {
            char c = padrao.charAt(i);
            if (c < ALFABETO) {
                badChar[c] = i;
            }
        }
        return badChar;
    }

    public static boolean pesquisar(String texto, String padrao) {
        if (padrao == null || padrao.isEmpty() || texto == null || texto.isEmpty()) return false;
        if (padrao.length() > texto.length()) return false;

        int m = padrao.length();
        int n = texto.length();
        int[] badChar = criarTabelaBadChar(padrao);

        int s = 0; 

        while (s <= (n - m)) {
            int j = m - 1; 

            while (j >= 0 && padrao.charAt(j) == texto.charAt(s + j)) {
                j--;
            }

            if (j < 0) {
                return true;
            } else {
                char c = texto.charAt(s + j);
                int lastOccurrence = (c < ALFABETO) ? badChar[c] : -1;
                s += Math.max(1, j - lastOccurrence);
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
