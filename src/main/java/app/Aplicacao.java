package app;

import java.util.*;

import dao.*;

import model.*;

import service.AuthService;
import service.CategoriaService;
import service.CriptoService;
import service.FavoritoService;
import service.ItemPedidoService;
import service.PedidoService;
import service.ProdutoService;
import service.UsuarioService;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

public class Aplicacao {

    public static void menu() {
        Scanner sc = new Scanner(System.in);
        int entrada = -1;
        while (entrada != 0) {
            System.out.println("\n=================================");
            System.out.println("Digite em qual tabela deseja alterar\n1 - Categoria\n2 - Favorito\n3 - Pedido\n4 - Produto\n5 - Usuario\n0 - Sair!");
            System.out.println("=================================");
            entrada = sc.nextInt();
            sc.nextLine();
            // ---------------- CATEGORIA ----------------
            if (entrada == 1) {
                CategoriaDAO categoriaDAO = new CategoriaDAO();
                int entrada2 = -1;
                while (entrada2 != 0) {
                    System.out.println("\n-- Menu Categoria --");
                    System.out.println("1 - Adicionar\n2 - Atualizar\n3 - Remover\n4 - Mostrar\n0 - Sair");
                    entrada2 = sc.nextInt();
                    sc.nextLine();
                    switch (entrada2) {
                        case 1:
                            System.out.println("Digite o nome!");
                            String nome = sc.nextLine();
                            System.out.println("Digite a descricao da categoria!");
                            String descricao = sc.nextLine();
                            Categoria newCategoria = new Categoria(nome, descricao);
                            try {
                                int id = categoriaDAO.insert(newCategoria);
                                System.out.println("Categoria de id: " + id + " adicionada!");
                            } catch (Exception e) {
                                System.out.println("Erro para adicionar categoria");
                            }
                            break;
                        case 2:
                            System.out.println("Digite o id da categoria que deseja atualizar!");
                            int UpdId = sc.nextInt();
                            sc.nextLine();
                            Categoria atualizar = categoriaDAO.get(UpdId);
                            if (atualizar != null) {
                                System.out.println("Dados atuais - Nome: " + atualizar.getNome() + " | Descricao: " + atualizar.getDescricao());
                                System.out.println("Digite o novo nome:");
                                atualizar.setNome(sc.nextLine());
                                System.out.println("Digite a nova descricao:");
                                atualizar.setDescricao(sc.nextLine());
                                try {
                                    if (categoriaDAO.update(atualizar)) {
                                        System.out.println("Categoria Atualizada com sucesso!");
                                    } else {
                                        System.out.println("Erro para salvar a alteracao!");
                                    }
                                } catch (Exception e) { e.printStackTrace(); }
                            } else {
                                System.out.println("Categoria nao encontrada!");
                            }
                            break;
                        case 3:
                            System.out.println("Digite o id da categoria que deseja remover!");
                            int DelId = sc.nextInt();
                            if (categoriaDAO.delete(DelId)) {
                                System.out.println("Categoria deletada com sucesso!");
                            } else {
                                System.out.println("Categoria nao encontrada!");
                            }
                            break;
                        case 4:
                            List<Categoria> lista = categoriaDAO.getAll();
                            if (lista.isEmpty()) {
                                System.out.println("Nenhuma categoria encontrada!");
                            } else {
                                for (Categoria u : lista) {
                                    System.out.println("ID: " + u.getId() + " | Nome: " + u.getNome() + " | Descricao: " + u.getDescricao());
                                }
                            }
                            break;
                    }
                }
            } 
            // ---------------- FAVORITO ----------------
            else if (entrada == 2) {
                FavoritoDAO favoritoDAO = new FavoritoDAO();
                int entrada2 = -1;
                while (entrada2 != 0) {
                    System.out.println("\n-- Menu Favoritos --");
                    System.out.println("1 - Adicionar\n2 - Atualizar\n3 - Remover\n4 - Mostrar\n0 - Sair");
                    entrada2 = sc.nextInt();
                    sc.nextLine();
                    switch (entrada2) {
                        case 1:
                            System.out.println("Digite o id do produto!");
                            int idProduto = sc.nextInt();
                            System.out.println("Digite o id do usuario!");
                            int idUsuario = sc.nextInt();
                            sc.nextLine();
                            if (favoritoDAO.isFavorito(idUsuario, idProduto)) {
                                System.out.println("Erro: Este usuário já favoritou este produto!");
                            } else {
                                Favorito newFavorito = new Favorito(idProduto, idUsuario);
                                try {
                                    int id = favoritoDAO.insert(newFavorito);
                                    System.out.println("Favorito de ID: " + id + " adicionado com sucesso!");
                                } catch (Exception e) {
                                    System.out.println("Erro ao adicionar favorito.");
                                }
                            }
                            break;
                        case 2:
                            System.out.println("Digite o id do favorito que deseja atualizar!");
                            int UpdId = sc.nextInt();
                            sc.nextLine();
                            Favorito atualizar = favoritoDAO.get(UpdId);
                            if (atualizar != null) {
                                System.out.println("Dados atuais - id Produto: " + atualizar.getIdProduto() + " | id Usuario " + atualizar.getIdUsuario());
                                System.out.println("Digite o novo idProduto:");
                                atualizar.setIdProduto(sc.nextInt());
                                System.out.println("Digite o novo IdUsuario:");
                                atualizar.setIdUsuario(sc.nextInt());
                                sc.nextLine();
                                try {
                                    if (favoritoDAO.update(atualizar)) {
                                        System.out.println("Favorito Atualizado com sucesso!");
                                    } else {
                                        System.out.println("Erro para salvar a alteracao!");
                                    }
                                } catch (Exception e) { e.printStackTrace(); }
                            } else {
                                System.out.println("Favorito nao encontrado!");
                            }
                            break;
                        case 3:
                            System.out.println("Digite o id do favorito que deseja remover!");
                            int DelId = sc.nextInt();
                            if (favoritoDAO.delete(DelId)) {
                                System.out.println("Favorito deletado com sucesso!");
                            } else {
                                System.out.println("Favorito nao encontrado!");
                            }
                            break;
                        case 4:
                            List<Favorito> lista = favoritoDAO.getAll();
                            if (lista.isEmpty()) {
                                System.out.println("Nenhum favorito encontrado!");
                            } else {
                                for (Favorito u : lista) {
                                    System.out.println("ID: " + u.getId() + " | IdProduto: " + u.getIdProduto() + " | IdUsuario: " + u.getIdUsuario());
                                }
                            }
                            break;
                    }
                }
            } 
            // ---------------- PEDIDO ----------------
            else if (entrada == 3) {
                PedidoDAO pedidoDAO = new PedidoDAO();
                int entrada2 = -1;
                while (entrada2 != 0) {
                    System.out.println("\n-- Menu Pedidos --");
                    System.out.println("1 - Adicionar\n2 - Atualizar\n3 - Remover\n4 - Mostrar\n0 - Sair");
                    entrada2 = sc.nextInt();
                    sc.nextLine();
                    switch (entrada2) {
                        case 1:
                            System.out.println("Digite o id do Usuario");
                            int idUsuario = sc.nextInt();
                            sc.nextLine();
                            long dataAtual = System.currentTimeMillis();
                            System.out.println("Digite o status do pedido");
                            String status = sc.nextLine();
                            System.out.println("Digite o valor total do pedido (ex: 150,50)");
                            float valorTotal = sc.nextFloat();
                            sc.nextLine();
                            Pedido newPedido = new Pedido(idUsuario, dataAtual, status, valorTotal);
                            List<model.ItemPedido> listaItens = new ArrayList<>();
                            int addMais = 1;
                            while(addMais != 0) {
                                System.out.println("Digite o ID do Produto:");
                                int idProd = sc.nextInt();
                                System.out.println("Digite a Quantidade:");
                                int qtd = sc.nextInt();
                                sc.nextLine();
                                model.ItemPedido item = new model.ItemPedido();
                                item.setIdProduto(idProd);
                                item.setQuantidade(qtd);
                                listaItens.add(item);
                                System.out.println("Adicionar mais produtos? (1 - Sim / 0 - Não)");
                                addMais = sc.nextInt();
                                sc.nextLine();
                            }
                            newPedido.setItens(listaItens);
                            try {
                                newPedido.validar();
                                int id = pedidoDAO.insert(newPedido);
                                System.out.println("Pedido de ID: " + id + " gerado com sucesso!");
                            } catch (Exception e) {
                                System.out.println("Erro ao salvar pedido: " + e.getMessage());
                            }
                            break;
                        case 2:
                            System.out.println("Digite o id do pedido que deseja atualizar!");
                            int UpdId = sc.nextInt();
                            sc.nextLine();
                            Pedido atualizar = pedidoDAO.get(UpdId);
                            if (atualizar != null) {
                                System.out.println("Dados atuais - id Pedido: " + atualizar.getId() + " | id usuario: " + atualizar.getIdUsuario() + " | status: " + atualizar.getStatus() + " | valorTotal: " + atualizar.getValorTotal());
                                System.out.println("Digite o novo status do pedido:");
                                atualizar.setStatus(sc.nextLine());
                                System.out.println("Digite o novo valor total (ex: 100,50):");
                                atualizar.setValorTotal(sc.nextFloat());
                                sc.nextLine();
                                try {
                                    if (pedidoDAO.update(atualizar)) {
                                        System.out.println("Pedido Atualizado com sucesso!");
                                    } else {
                                        System.out.println("Erro para salvar a alteracao!");
                                    }
                                } catch (Exception e) { e.printStackTrace(); }
                            } else {
                                System.out.println("Pedido nao encontrado!");
                            }
                            break;
                        case 3:
                            System.out.println("Digite o id do pedido que deseja remover!");
                            int DelId = sc.nextInt();
                            if (pedidoDAO.delete(DelId)) {
                                System.out.println("Pedido deletado com sucesso!");
                            } else {
                                System.out.println("Pedido nao encontrado!");
                            }
                            break;
                        case 4:
                            List<Pedido> lista = pedidoDAO.getAll();
                            if (lista.isEmpty()) {
                                System.out.println("Nenhum pedido encontrado!");
                            } else {
                                for (Pedido u : lista) {
                                    System.out.println("ID: " + u.getId() + " | IdUsuario: " + u.getIdUsuario() + " | Data Compra: " + u.getDataCompra() + " | Status: " + u.getStatus() + " | Valor Total: " + u.getValorTotal());
                                }
                            }
                            break;
                    }
                }
            } 
            // ---------------- PRODUTO ----------------
            else if (entrada == 4) {
                ProdutoDAO produtoDAO = new ProdutoDAO();
                int entrada2 = -1;
                while (entrada2 != 0) {
                    System.out.println("\n-- Menu Produtos --");
                    System.out.println("1 - Adicionar\n2 - Atualizar\n3 - Remover\n4 - Mostrar\n0 - Sair");
                    entrada2 = sc.nextInt();
                    sc.nextLine();
                    switch (entrada2) {
                        case 1:
                            System.out.println("Digite o id da categoria!");
                            int idCategoria = sc.nextInt();
                            sc.nextLine();
                            System.out.println("Digite o nome do produto!");
                            String nome = sc.nextLine();
                            System.out.println("Digite a descricao do produto!");
                            String descricao = sc.nextLine();
                            System.out.println("Digite o preco do produto (ex: 99,90)!");
                            float preco = sc.nextFloat();
                            System.out.println("Digite a data de validade em milissegundos (ou 0 se não tiver)!");
                            long data = sc.nextLong();
                            System.out.println("Digite a quantidade inicial do produto!");
                            int quant = sc.nextInt();
                            sc.nextLine(); 
                            System.out.println("Digite as tags do produto separadas por vírgula (ex: graos, vegetais):");
                            String strTags = sc.nextLine();
                            String[] tags = strTags.isEmpty() ? new String[0] : strTags.split(",");
                            Produto newProduto = new Produto(idCategoria, nome, descricao, preco, data, quant, tags, null);
                            try {
                                newProduto.validar();
                                int id = produtoDAO.insert(newProduto);
                                System.out.println("Produto ID " + id + " cadastrado com sucesso");
                            } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }                  
                            break;
                        case 2:
                            System.out.println("Digite o id do produto que deseja atualizar!");
                            int UpdId = sc.nextInt();
                            sc.nextLine();
                            Produto atualizar = produtoDAO.get(UpdId);
                            if (atualizar != null) {
                                System.out.println("Nome atual: " + atualizar.getNome() + " | Preço: " + atualizar.getPreco());
                                System.out.println("Digite o novo nome:");
                                atualizar.setNome(sc.nextLine());
                                System.out.println("Digite o novo preço (ex: 15,00):");
                                atualizar.setPreco(sc.nextFloat());
                                System.out.println("Digite a nova data de validade (milissegundos):");
                                atualizar.setDataValidade(sc.nextLong());
                                System.out.println("Digite a nova quantidade de estoque:");
                                atualizar.setQuantidadeEstoque(sc.nextInt());
                                sc.nextLine(); 
                                System.out.println("Digite as novas tags separadas por vírgula:");
                                String strTagsUpd = sc.nextLine();
                                atualizar.setTags(strTagsUpd.isEmpty() ? new String[0] : strTagsUpd.split(","));
                                try {
                                    atualizar.validar();
                                    if (produtoDAO.update(atualizar)) System.out.println("Produto atualizado com sucesso!");
                                    else System.out.println("Erro ao atualizar!");
                                } catch (Exception e) { System.out.println("Erro: " + e.getMessage()); }
                            } else { System.out.println("Produto não encontrado."); }
                            break;
                        case 3:
                            System.out.println("Digite o id do produto que deseja remover!");
                            int DelId = sc.nextInt();
                            if (produtoDAO.delete(DelId)) {
                                System.out.println("Produto deletado com sucesso!");
                            } else {
                                System.out.println("Produto nao encontrado!");
                            }
                            break;
                        case 4:
                            List<Produto> lista = produtoDAO.getAll();
                            if (lista.isEmpty()) {
                                System.out.println("Nenhum produto encontrado!");
                            } else {
                                for (Produto u : lista) {
                                    System.out.print("ID: " + u.getId() + " | Cat: " + u.getIdCategoria() + " | Nome: " + u.getNome() +
                                            " | Preco: " + u.getPreco() + " | Validade: " + u.getDataValidade() + " | Estoque: " + u.getQuantidadeEstoque() + " | Tags: [");
                                    
                                    if(u.getTags() != null) {
                                        for(int i = 0; i < u.getTags().length; i++) {
                                            System.out.print(u.getTags()[i] + (i < u.getTags().length - 1 ? ", " : ""));
                                        }
                                    }
                                    System.out.println("]");
                                }
                            }
                            break;
                    }
                }
            } 
            // ---------------- USUARIO ----------------
            else if (entrada == 5) {
                UsuarioDAO usuarioDAO = new UsuarioDAO();
                int entrada2 = -1;
                while (entrada2 != 0) {
                    System.out.println("\n-- Menu Usuarios --");
                    System.out.println("1 - Adicionar\n2 - Atualizar\n3 - Remover\n4 - Mostrar\n0 - Sair");
                    entrada2 = sc.nextInt();
                    sc.nextLine();
                    switch (entrada2) {
                        case 1:
                            System.out.println("Digite o login!");
                            String login = sc.nextLine();
                            System.out.println("Digite o nome!");
                            String nome = sc.nextLine();
                            System.out.println("Digite o senha!");
                            String senha = sc.nextLine();
                            System.out.println("Digite o email!");
                            String email = sc.nextLine();
                            String senhaCripto = CriptoService.xor(senha);
                            Usuario newUsuario = new Usuario(login, senhaCripto, nome, email, Role.USUARIO);
                            try {
                                int id = usuarioDAO.insert(newUsuario);
                                System.out.println("Usuario de id: " + id + " adicionado!");
                            } catch (Exception e) {
                                System.out.println("Erro para adicionar usuario");
                            }
                            break;
                        case 2:
                            System.out.println("Digite o id do usuario que deseja atualizar!");
                            int UpdId = sc.nextInt();
                            sc.nextLine();
                            Usuario atualizar = usuarioDAO.get(UpdId);
                            if (atualizar != null) {
                                System.out.println("Dados atuais - Nome: " + atualizar.getNome() + " | Email: " + atualizar.getEmail());
                                System.out.println("Digite o novo nome:");
                                atualizar.setNome(sc.nextLine());
                                System.out.println("Digite o novo email:");
                                atualizar.setEmail(sc.nextLine());
                                System.out.println("Digite a nova Senha:");
                                String newSenha = sc.nextLine();
                                atualizar.setSenha(CriptoService.xor(newSenha));
                                try {
                                    if (usuarioDAO.update(atualizar)) {
                                        System.out.println("Usuario Atualizado com sucesso!");
                                    } else {
                                        System.out.println("Erro para salvar a alteracao!");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                System.out.println("Usuario nao encontrado!");
                            }
                            break;
                        case 3:
                            System.out.println("Digite o id do usuario que deseja remover!");
                            int DelId = sc.nextInt();
                            if (usuarioDAO.delete(DelId)) {
                                System.out.println("Usuario deletado com sucesso!");
                            } else {
                                System.out.println("Usuario nao encontrado!");
                            }
                            break;
                        case 4:
                            List<Usuario> lista = usuarioDAO.getAll();
                            for (Usuario u : lista) {
                                System.out.println("ID: " + u.getId() + " | Nome: " + u.getNome() + " | Login: " + u.getLogin());
                            }
                            break;
                    }
                }
            }
        }
        System.out.println("Saindo do programa!");
        sc.close();
    }

    public static void main(String[] args) {
        port(3000);
        staticFiles.location("/public");
        // Isso vai imprimir QUALQUER coisa que chegar no servidor
        before((req, res) -> {
            System.out.println(">>> Requisição recebida em: " + req.pathInfo());
        });

        // CONFIGURAÇÃO DE CORS
        options("/*", (req, res) -> {
            String headers = req.headers("Access-Control-Request-Headers");
            if (headers != null) {
                res.header("Access-Control-Allow-Headers", headers);
            }
            String method = req.headers("Access-Control-Request-Method");
            if (method != null) {
                res.header("Access-Control-Allow-Methods", method);
            }
            return "OK";
        });

        before((req, res) -> {
            // Agora usando localhost para casar com o seu navegador
            res.header("Access-Control-Allow-Origin", "http://localhost:3001");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
            res.header("Access-Control-Allow-Credentials", "true");

            if (req.pathInfo().startsWith("/api")) {
                res.type("application/json");
            }
        });

        // --- INSTANCIANDO SERVICES ---
        UsuarioService usuarioService = new UsuarioService();
        CategoriaService categoriaService = new CategoriaService();
        ProdutoService produtoService = new ProdutoService();
        FavoritoService favoritoService = new FavoritoService();
        ItemPedidoService itemPedidoService = new ItemPedidoService();
        PedidoService pedidoService = new PedidoService();

        // --- ROTAS PÚBLICAS (NÃO EXIGEM LOGIN) ---
        post("/login", (req, res) -> new AuthService().login(req, res));
        post("/logout", (req, res) -> new AuthService().logout(req, res));
        post("/usuario", (req, res) -> new UsuarioService().insert(req, res));

        get("/produtos", (req, res) -> produtoService.getAll(req, res));
        get("/produtos/:id", (req, res) -> produtoService.get(req, res));
        get("/produtos/categoria/:idCategoria", (req, res) -> produtoService.listarPorCategoria(req, res));
        get("/produtos/preco/:preco", (req, res) -> produtoService.listarAtePreco(req, res));
        get("/produtos/relatorio/ordenados-preco", (req, res) -> produtoService.listarOrdenadosPorPreco(req, res));

        get("/categorias", (req, res) -> categoriaService.getAll(req, res));
        get("/categorias/:id", (req, res) -> categoriaService.get(req, res));
        get("/categorias/busca/:nome", (req, res) -> categoriaService.getPorNome(req, res));

        before("/api/admin/*", (req, res) -> {
            if (req.requestMethod().equals("OPTIONS")) {
                return;
            }
            AuthService.verificarPermissao(req, Role.ADMIN);
        });

        before("/api/gestao/*", (req, res) -> {
            if (req.requestMethod().equals("OPTIONS")) {
                return;
            }
            AuthService.verificarPermissao(req, Role.ADMIN, Role.GESTOR);
        });

        before("/api/comum/*", (req, res) -> {
            if (req.requestMethod().equals("OPTIONS")) {
                return;
            }
            AuthService.verificarPermissao(req, Role.ADMIN, Role.GESTOR, Role.USUARIO);
        });

        // --- BLOCO DE ROTAS PROTEGIDAS /API ---
        path("/api", () -> {

            // Rotas de Admin
            path("/admin", () -> {
                get("/usuarios", (req, res) -> usuarioService.getAll(req, res));
                get("/usuarios/busca/:login", (req, res) -> usuarioService.getPorLogin(req, res));
                get("/usuarios/:id", (req, res) -> usuarioService.get(req, res));
                put("/usuarios/:id", (req, res) -> usuarioService.update(req, res));
                delete("/usuarios/:id", (req, res) -> usuarioService.delete(req, res));

                get("/favoritos", (req, res) -> favoritoService.getAll(req, res));
                get("/favoritos/:id", (req, res) -> favoritoService.get(req, res));
                delete("/favoritos/:id", (req, res) -> favoritoService.delete(req, res));

                get("/pedidos", (req, res) -> pedidoService.getAll(req, res));
                get("/pedidos/:id", (req, res) -> pedidoService.get(req, res));
                put("/pedidos/:id", (req, res) -> pedidoService.update(req, res));
                delete("/pedidos/:id", (req, res) -> pedidoService.delete(req, res));

                get("/itens-pedido", (req, res) -> itemPedidoService.getAll(req, res));
                get("/itens-pedido/:id", (req, res) -> itemPedidoService.get(req, res));
                delete("/itens-pedido/:id", (req, res) -> itemPedidoService.delete(req, res));
            });

            // Rotas Comuns
            path("/comum", () -> {
                get("/meus-dados", (req, res) -> usuarioService.getMe(req, res));
                put("/meus-dados", (req, res) -> usuarioService.updateMe(req, res));

                get("/favoritos", (req, res) -> favoritoService.getMeusFavoritos(req, res));
                post("/favoritos", (req, res) -> favoritoService.insert(req, res));
                delete("/favoritos/:id", (req, res) -> favoritoService.delete(req, res));

                post("/pedidos", (req, res) -> pedidoService.insert(req, res));
                get("/pedidos", (req, res) -> pedidoService.getMeusPedidos(req, res));
                get("/pedidos/:id", (req, res) -> pedidoService.get(req, res));

                post("/itens-pedido", (req, res) -> itemPedidoService.insert(req, res));
                get("/pedidos/:idPedido/itens", (req, res) -> itemPedidoService.getItensPedidoSeguro(req, res));
            });

            // Rotas de Gestão
            path("/gestao", () -> {
                post("/categorias", (req, res) -> categoriaService.insert(req, res));
                put("/categorias/:id", (req, res) -> categoriaService.update(req, res));
                delete("/categorias/:id", (req, res) -> categoriaService.delete(req, res));
                // Dentro de path("/gestao", () -> { ...
                put("/pedidos/:id/status", (req, res) -> pedidoService.atualizarStatus(req, res));
                post("/produtos", (req, res) -> produtoService.insert(req, res));
                put("/produtos/:id", (req, res) -> produtoService.update(req, res));
                delete("/produtos/:id", (req, res) -> produtoService.delete(req, res));

                get("/pedidos/status/:status", (req, res) -> pedidoService.getPorStatus(req, res));
                get("/pedidos/data/:dataLimite", (req, res) -> pedidoService.getAteData(req, res));
                get("/pedidos/valor/:valorMaximo", (req, res) -> pedidoService.getAteValor(req, res));

                get("/produtos/:idProduto/itens-vendidos",
                        (req, res) -> itemPedidoService.getItensPorProduto(req, res));
                get("/produtos/:idProduto/favoritos", (req, res) -> favoritoService.getFavoritosPorProduto(req, res));
            });
        });

        spark.Spark.awaitInitialization();
        System.out.println("\n=====================================================");
        System.out.println("API RESTful rodando na porta 3000!");
        System.out.println("Frontend liberado para requisições.");
        System.out.println("=====================================================\n");
        menu(); // Chama terminal interativo
    }
}
