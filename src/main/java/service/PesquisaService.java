package service;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dao.ProdutoDAO;
import model.Produto;
import spark.Request;
import spark.Response;
import utilidades.BoyerMoore;
import utilidades.KMP;

public class PesquisaService {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final Gson gson = new Gson();

    public Object pesquisarProdutos(Request req, Response res) {
        try {
            String padrao    = req.queryParams("padrao");
            String algoritmo = req.queryParams("algoritmo");

            if (padrao == null || padrao.trim().isEmpty()) {
                res.status(400);
                return "{\"erro\": \"O parâmetro 'padrao' é obrigatório.\"}";
            }

            // Algoritmo padrão: KMP
            if (algoritmo == null || algoritmo.isBlank()) {
                algoritmo = "KMP";
            }

            List<Produto> todos = produtoDAO.getAll();
            List<Produto> resultado;

            long inicio = System.nanoTime();

            switch (algoritmo.toUpperCase()) {
                case "BM":
                case "BOYERMOORE":
                case "BOYER-MOORE":
                    resultado = BoyerMoore.pesquisarEmProdutos(todos, padrao);
                    algoritmo = "BoyerMoore";
                    break;
                case "KMP":
                default:
                    resultado = KMP.pesquisarEmProdutos(todos, padrao);
                    algoritmo = "KMP";
                    break;
            }

            long fim = System.nanoTime();
            long tempoMs = (fim - inicio) / 1_000_000;

            // Monta resposta com metadados úteis
            JsonObject resposta = new JsonObject();
            resposta.addProperty("algoritmo", algoritmo);
            resposta.addProperty("padrao", padrao);
            resposta.addProperty("totalEncontrados", resultado.size());
            resposta.addProperty("tempoMs", tempoMs);
            resposta.add("registros", gson.toJsonTree(resultado));

            res.status(200);
            return gson.toJson(resposta);

        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}
