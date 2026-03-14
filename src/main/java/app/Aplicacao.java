package app;

import dao.*;
import model.*;
import service.*;
import static spark.Spark.*;

import java.util.*;

public class Aplicacao {
	public static void menu() { // função de menu do crud ignore caso queira apenas olhar o funcionamento do
								// site em si
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
							System.out.println("Dados atuais - Nome: " + atualizar.getNome() + " | Email: "
									+ atualizar.getEmail());
							System.out.println("Digite o novo nome:");
							atualizar.setNome(sc.nextLine());
							System.out.println("Digite o novo email:");
							atualizar.setEmail(sc.nextLine());
							System.out.println("Digite a nova Senha:");
							String newSenha = sc.nextLine();
							atualizar.setSenha(CriptoService.xor(newSenha));
							try {
								if (usuarioDAO.update(atualizar))
									System.out.println("Usuario de id: " + UpdId + " Atualizado com sucesso!");
								else
									System.out.println("Erro para salvar a alteracao!");
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
							System.out.println(
									"ID: " + u.getId() + " | Nome: " + u.getNome() + " | Login: " + u.getLogin());
						}
						break;
					default:
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

		// Configuração de CORS (Basicamente configuracao do navegador isso n importa
		// muito)
		options("/*", (req, res) -> {
			String headers = req.headers("Access-Control-Request-Headers");
			if (headers != null)
				res.header("Access-Control-Allow-Headers", headers);
			String method = req.headers("Access-Control-Request-Method");
			if (method != null)
				res.header("Access-Control-Allow-Methods", method);
			return "OK";
		});
		before((req, res) -> res.header("Access-Control-Allow-Origin", "*"));

		// INSTANCIANDO OS SERVICES AQUI EM CIMA PARA TODOS OS BLOCOS PODEREM USAR
		UsuarioService usuarioService = new UsuarioService();
		CategoriaService categoriaService = new CategoriaService();
		ProdutoService produtoService = new ProdutoService();
		FavoritoService favoritoService = new FavoritoService();
		ItemPedidoService itemPedidoService = new ItemPedidoService();
		PedidoService pedidoService = new PedidoService();

		// -- Rotas que todo mundo consegue utilizar até não estando logado --
		post("/login", (req, res) -> new AuthService().login(req, res)); // Login
		post("/logout", (req, res) -> new AuthService().logout(req, res)); // Deslogar
		post("/usuario", (req, res) -> new UsuarioService().insert(req, res)); // Criar conta

		// LER PRODUTOS E CATEGORIAS (Fica aqui fora porque clientes não logados
		// precisam ver a loja)

		get("/produtos", (req, res) -> produtoService.getAll(req, res));
		get("/produtos/:id", (req, res) -> produtoService.get(req, res));
		get("/produtos/categoria/:idCategoria", (req, res) -> produtoService.listarPorCategoria(req, res));
		get("/produtos/preco/:preco", (req, res) -> produtoService.listarAtePreco(req, res));

		get("/categorias", (req, res) -> categoriaService.getAll(req, res));
		get("/categorias/:id", (req, res) -> categoriaService.get(req, res));
		get("/categorias/busca/:nome", (req, res) -> categoriaService.getPorNome(req, res));

		// -- Configuracao de como configurar os fetch, o que o adm acessa e o que o
		// usuario comum acessa --
		// Area de ADMIN (Em todo fetch que seguir um caminho tipo esse, so o admin pode
		// usar)
		before("/api/admin/*", (req, res) -> AuthService.verificarPermissao(req, Role.ADMIN));

		// Area COMUM (Em todo fetch que seguir um caminho tipo esse, os usuarios comuns
		// podem usar)
		before("/api/comum/*",
				(req, res) -> AuthService.verificarPermissao(req, Role.ADMIN, Role.GESTOR, Role.USUARIO));

		// Area de GESTÃO (Apenas Admin e Gestor podem gerenciar o estoque)
		before("/api/gestao/*", (req, res) -> AuthService.verificarPermissao(req, Role.ADMIN, Role.GESTOR));

		// Outros fetchs que apenas logado é possivel, e dependendo da role pode ou não
		// pode
		path("/api", () -> {

			// Rotas de Admin (Gerenciar outros usuários)
			path("/admin", () -> {

				get("/usuarios", (req, res) -> usuarioService.getAll(req, res));
				get("/usuarios/busca/:login", (req, res) -> usuarioService.getPorLogin(req, res));
				get("/usuarios/:id", (req, res) -> usuarioService.get(req, res));

				put("/usuarios/:id", (req, res) -> usuarioService.update(req, res));
				delete("/usuarios/:id", (req, res) -> usuarioService.delete(req, res));

				// FAVORITOS (admin pode ver e gerenciar todos)

				get("/favoritos", (req, res) -> favoritoService.getAll(req, res));
				get("/favoritos/:id", (req, res) -> favoritoService.get(req, res));

				post("/favoritos", (req, res) -> favoritoService.insert(req, res));
				put("/favoritos/:id", (req, res) -> favoritoService.update(req, res));
				delete("/favoritos/:id", (req, res) -> favoritoService.delete(req, res));

				// PEDIDOS (admin pode ver todos)

				get("/pedidos", (req, res) -> pedidoService.getAll(req, res));
				get("/pedidos/:id", (req, res) -> pedidoService.get(req, res));

				post("/pedidos", (req, res) -> pedidoService.insert(req, res));
				put("/pedidos/:id", (req, res) -> pedidoService.update(req, res));
				delete("/pedidos/:id", (req, res) -> pedidoService.delete(req, res));

				// ITENS DO PEDIDO

				get("/itens-pedido", (req, res) -> itemPedidoService.getAll(req, res));
				get("/itens-pedido/:id", (req, res) -> itemPedidoService.get(req, res));

				post("/itens-pedido", (req, res) -> itemPedidoService.insert(req, res));
				put("/itens-pedido/:id", (req, res) -> itemPedidoService.update(req, res));
				delete("/itens-pedido/:id", (req, res) -> itemPedidoService.delete(req, res));
			});

			// Rotas Comuns (Para o próprio usuário logado)
			path("/comum", () -> {

				// Rota Especial: BUSCAR os dados de QUEM ESTÁ LOGADO
				get("/meus-dados", (req, res) -> usuarioService.getMe(req, res));

				// Rota Especial: ATUALIZAR os dados de QUEM ESTÁ LOGADO
				put("/meus-dados", (req, res) -> usuarioService.updateMe(req, res));

				// FAVORITOS

				// listar favoritos do usuário logado
				get("/favoritos", (req, res) -> favoritoService.getSeguro(req, res));

				// adicionar favorito
				post("/favoritos", (req, res) -> favoritoService.insertSeguro(req, res));

				// atualizar favorito
				put("/favoritos/:id", (req, res) -> favoritoService.updateSeguro(req, res));

				// remover favorito
				delete("/favoritos/:id", (req, res) -> favoritoService.deleteSeguro(req, res));

				// ITENS DO PEDIDO

				// adicionar item ao pedido
				post("/itens-pedido", (req, res) -> itemPedidoService.adicionarItem(req, res));

				// listar itens de um pedido
				get("/itens-pedido", (req, res) -> itemPedidoService.getItensPedidoSeguro(req, res));

				// PEDIDOS

				// criar pedido
				post("/pedidos", (req, res) -> pedidoService.criarPedido(req, res));

				// listar meus pedidos
				get("/pedidos", (req, res) -> pedidoService.getPedidoSeguro(req, res));

				// ver um pedido específico
				get("/pedidos/:id", (req, res) -> pedidoService.getPedidoSeguro(req, res));
			});

			// Rotas de Gestão (NOVO BLOCO: Gerenciar Produtos e Categorias)
			path("/gestao", () -> {
				// CRUD Categorias (Só Admin/Gestor pode alterar)
				post("/categorias", (req, res) -> categoriaService.insert(req, res));
				put("/categorias/:id", (req, res) -> categoriaService.update(req, res));
				delete("/categorias/:id", (req, res) -> categoriaService.delete(req, res));

				// CRUD Produtos (Só Admin/Gestor pode alterar)
				post("/produtos", (req, res) -> produtoService.insert(req, res));
				put("/produtos/:id", (req, res) -> produtoService.update(req, res));
				delete("/produtos/:id", (req, res) -> produtoService.delete(req, res));
			});

		});
	}
}