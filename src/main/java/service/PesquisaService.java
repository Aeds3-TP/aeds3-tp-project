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

/**
 * Serviço responsável pelas funcionalidades de:
 *  1. Pesquisa por padrão (KMP e Boyer-Moore) sobre o campo 'nome' de Produto.
 *  2. Criptografia/decriptografia XOR aplicada a campos sensíveis.
 *
 * Rotas expostas:
 *  GET  /api/pesquisa/produtos?padrao=&algoritmo=KMP|BM   → busca produtos pelo nome
 *  POST /api/pesquisa/cripto/cifrar                        → cifra um texto com XOR
 *  POST /api/pesquisa/cripto/decifrar                      → decifra um texto com XOR
 */
public class PesquisaService {

    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final Gson gson = new Gson();

    // -----------------------------------------------------------------------
    // PESQUISA POR PADRÃO
    // -----------------------------------------------------------------------

    /**
     * Endpoint: GET /api/pesquisa/produtos?padrao=texto&algoritmo=KMP
     *
     * O campo textual escolhido é 'nome' do Produto porque é o campo de
     * busca mais utilizado pelos clientes e contém texto livre, sendo ideal
     * para demonstrar casamento de padrões.
     */
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

    // -----------------------------------------------------------------------
    // CRIPTOGRAFIA XOR
    // Campo sensível: 'senha' do Usuario — campo com dados privados do usuário.
    // A mesma função XOR serve para cifrar e decifrar (operação simétrica).
    // -----------------------------------------------------------------------

    /**
     * POST /api/pesquisa/cripto/cifrar
     * Body JSON: { "texto": "valorOriginal" }
     * Retorna:   { "resultado": "valorCifrado", "metodo": "XOR" }
     */
    public Object cifrar(Request req, Response res) {
        return aplicarXor(req, res, "cifrado");
    }

    /**
     * POST /api/pesquisa/cripto/decifrar
     * Body JSON: { "texto": "valorCifrado" }
     * Retorna:   { "resultado": "valorOriginal", "metodo": "XOR" }
     */
    public Object decifrar(Request req, Response res) {
        return aplicarXor(req, res, "decifrado");
    }

    private Object aplicarXor(Request req, Response res, String operacao) {
        try {
            JsonObject body = gson.fromJson(req.body(), JsonObject.class);
            if (body == null || !body.has("texto")) {
                res.status(400);
                return "{\"erro\": \"Campo 'texto' obrigatório no body.\"}";
            }
            String texto = body.get("texto").getAsString();
            String resultado = CriptoService.xor(texto);

            JsonObject resposta = new JsonObject();
            resposta.addProperty("operacao", operacao);
            resposta.addProperty("metodo", "XOR");
            resposta.addProperty("campoCifrado", "senha");
            resposta.addProperty("resultado", resultado);

            res.status(200);
            return gson.toJson(resposta);

        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}
