package utilidades;
import java.util.*;
import model.*;

public class BoyerMoore {
    private static final int ALFABETO = 256;

    private static int[] criarTabelaBadChar(String padrao) {
        int[] badChar = new int[ALFABETO];
        int m = padrao.length();

        for (int i = 0; i < ALFABETO; i++) {
            badChar[i] = -1;
        }

        for (int i = 0; i < m; i++) {
            if(padrao.charAt(i) < ALFABETO) {
                 badChar[padrao.charAt(i)] = i;
            }
        }
        return badChar;
    }

    public static boolean pesquisar(String texto, String padrao) {
        // Edge cases
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

    public ArrayList<Receita> pesquisar(ArrayList<Receita> lista, String padrao) {
        ArrayList<Receita> encontrados = new ArrayList<>();
        for (Receita r : lista) {
            if (pesquisarIgnoreCase(r.getDescricao(), padrao)) {
                encontrados.add(r);
            }
        }
        return encontrados;
    }
}