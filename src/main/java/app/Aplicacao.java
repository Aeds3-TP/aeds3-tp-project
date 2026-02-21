package app;

import model.Role;
import service.*; 
import static spark.Spark.*;

public class Aplicacao {

    public static void main(String[] args) {
        port(3000);
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
    }
}