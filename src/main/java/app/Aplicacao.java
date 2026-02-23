package app;

import dao.*;
import model.*;
import service.*; 
//import static spark.Spark.*;

import java.util.*;

public class Aplicacao {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        int entrada = -1;
        while(entrada != 0) {
            System.out.println("1 - Continuar!\n0 - Sair!");
            entrada = sc.nextInt();
            sc.nextLine();
            if(entrada == 1) {
                System.out.println("1 - Adicionar\n2 - Atualizar\n3 - Remover\n4 - Mostrar\n0 - Sair");
                int entrada2 = sc.nextInt();
                sc.nextLine();
                switch(entrada2) {
                    case 1:
                        System.out.println("Digite o login!"); String login = sc.nextLine();
                        System.out.println("Digite o nome!"); String nome = sc.nextLine();
                        System.out.println("Digite o senha!"); String senha = sc.nextLine();
                        System.out.println("Digite o email!"); String email = sc.nextLine();
                        String senhaCripto = CriptoService.xor(senha);
                        Usuario newUsuario = new Usuario(login, senhaCripto, nome, email, Role.USUARIO);
                        try {
                            int id = usuarioDAO.insert(newUsuario);
                            System.out.println("Usuario de id: " + id + " adicionado!");
                        }
                        catch(Exception e) {
                            System.out.println("Erro para adicionar usuario");
                        }                      
                        break;
                    case 2: 
                        System.out.println("Digite o id do usuario que deseja atualizar!");
                        int UpdId = sc.nextInt();
                        sc.nextLine();
                        Usuario atualizar = usuarioDAO.get(UpdId);
                        if(atualizar != null) {
                            System.out.println("Dados atuais - Nome: " + atualizar.getNome() + " | Email: " + atualizar.getEmail());
                            System.out.println("Digite o novo nome:");
                            atualizar.setNome(sc.nextLine()); 
                            System.out.println("Digite o novo email:");
                            atualizar.setEmail(sc.nextLine()); 
                            System.out.println("Digite a nova Senha:");
                            String newSenha = sc.nextLine();
                            atualizar.setSenha(CriptoService.xor(newSenha));      
                            try {
                                if(usuarioDAO.update(atualizar)) System.out.println("Usuario de id: " + UpdId + " Atualizado com sucesso!");
                                else System.out.println("Erro para salvar a alteracao!");
                            } 
                            catch(Exception e) { 
                                e.printStackTrace(); 
                            }
                        }
                        else {
                            System.out.println("Usuario nao encontrado!");
                        }
                        break;
                    case 3:
                        System.out.println("Digite o id do usuario que deseja remover!");
                        int DelId = sc.nextInt();
                        if(usuarioDAO.delete(DelId)) {
                            System.out.println("Usuario deletado com sucesso!");
                        }
                        else {
                            System.out.println("Usuario nao encontrado!");
                        }
                        break;
                    case 4:
                        List<Usuario> lista = usuarioDAO.getAll(); 
                        for (Usuario u : lista) {
                            System.out.println("ID: " + u.getId() + " | Nome: " + u.getNome() + " | Login: " + u.getLogin());
                        }
                        break;
                    default:
                        break;
                }
            }
        }    
        System.out.println("Saindo do programa!");
        sc.close();
        /*port(3000);
        staticFiles.location("/public");
        
        // Configuração de CORS (Basicamente configuracao do navegador isso n importa muito)
        options("/*", (req, res) -> {
            String headers = req.headers("Access-Control-Request-Headers");
            if (headers != null) res.header("Access-Control-Allow-Headers", headers);
            String method = req.headers("Access-Control-Request-Method");
            if (method != null) res.header("Access-Control-Allow-Methods", method);
            return "OK";
        });
        before((req, res) -> res.header("Access-Control-Allow-Origin", "*"));

        
        
        // -- Rotas que todo mundo consegue utilizar até não estando logado --
        post("/login", (req, res) -> new AuthService().login(req, res)); //Login
        
        post("/logout", (req, res) -> new AuthService().logout(req, res)); // Deslogar
        
        post("/usuario", (req, res) -> new UsuarioService().insert(req, res)); // Criar conta

        
        // -- Configuracao de como configurar os fetch, o que o adm acessa e o que o usuario comum acessa --
        // Area de ADMIN (Em todo fetch que seguir um caminho tipo esse, so o admin pode usar)
        before("/api/admin/*", (req, res) -> AuthService.verificarPermissao(req, Role.ADMIN));
        
        // Area COMUM (Em todo fetch que seguir um caminho tipo esse, os usuarios comuns podem usar)
        before("/api/comum/*", (req, res) -> AuthService.verificarPermissao(req, Role.ADMIN, Role.GESTOR, Role.USUARIO));

        
        //Outros fetchs que apenas logado é possivel, e dependendo da role pode ou não pode
        path("/api", () -> {
            
            //Rotas de Admin (Gerenciar outros usuários)
            path("/admin", () -> {
                UsuarioService usuarioService = new UsuarioService();

                get("/usuarios", (req, res) -> usuarioService.getAll(req, res));
                get("/usuarios/busca/:login", (req, res) -> usuarioService.getPorLogin(req, res));
                get("/usuarios/:id", (req, res) -> usuarioService.get(req, res));
                
                put("/usuarios/:id", (req, res) -> usuarioService.update(req, res));
                delete("/usuarios/:id", (req, res) -> usuarioService.delete(req, res));
            });

            // Rotas Comuns (Para o próprio usuário logado)
            path("/comum", () -> {
                UsuarioService usuarioService = new UsuarioService();

                // Rota Especial: BUSCAR os dados de QUEM ESTÁ LOGADO
                get("/meus-dados", (req, res) -> usuarioService.getMe(req, res));

                // Rota Especial: ATUALIZAR os dados de QUEM ESTÁ LOGADO
                put("/meus-dados", (req, res) -> usuarioService.updateMe(req, res));
            });
            
        });
        */
    }
}