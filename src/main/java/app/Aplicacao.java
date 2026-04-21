package app;

import java.util.List;
import java.util.Scanner;

import dao.UsuarioDAO;
import model.Role;
import model.Usuario;
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
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        int entrada = -1;
        while (entrada != 0) {
            System.out.println("1 - Continuar!\n0 - Sair!");
            entrada = sc.nextInt();
            sc.nextLine();
            if (entrada == 1) {
                System.out.println("1 - Adicionar\n2 - Atualizar\n3 - Remover\n4 - Mostrar\n0 - Sair");
                int entrada2 = sc.nextInt();
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
                                    System.out.println("Usuario de id: " + UpdId + " Atualizado com sucesso!");
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

                get("/produtos/:idProduto/itens-vendidos", (req, res) -> itemPedidoService.getItensPorProduto(req, res));
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
