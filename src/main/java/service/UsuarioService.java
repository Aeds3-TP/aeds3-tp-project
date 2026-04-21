package service;

import dao.UsuarioDAO;
import model.Usuario;
import spark.Request;
import spark.Response;

public class UsuarioService extends BaseService<Usuario> {

    public UsuarioService() {
        super(new UsuarioDAO());
    }

    @Override
    protected Class<Usuario> getModelClass() {
        return Usuario.class;
    }

    //--Funcoes do que é feito antes de aceitar o novo objeto--
    @Override
    protected void onBeforeInsert(Usuario u) throws Exception {
        // Validação do Model
        u.validar();

        // Todo novo cadastro público SEMPRE será de um CLIENTE (ou USUARIO).
        //u.setRole(Role.USUARIO);
        // Validação do Service
        if (((UsuarioDAO) dao).getByLogin(u.getLogin()) != null) {
            throw new Exception("Este login já está em uso.");
        }

        // 3. Modificação (Criptografia)
        u.setSenha(CriptoService.xor(u.getSenha()));
    }

    @Override
    public Object insert(Request req, Response res) {
        try {
            // Log 1: O que veio do Navegador?
            System.out.println("JSON recebido no Java: " + req.body());

            Usuario u = gson.fromJson(req.body(), Usuario.class);

            // Log 2: O GSON conseguiu preencher o objeto?
            System.out.println("Objeto após GSON: Nome=" + u.getNome() + ", Login=" + u.getLogin());

            onBeforeInsert(u);

            int id = ((UsuarioDAO) dao).insert(u);

            // Log 3: O DAO retornou um ID válido?
            System.out.println("ID gerado pelo DAO: " + id);

            if (id > 0) {
                res.status(201);
                return "{\"msg\": \"Usuario criado\", \"id\": " + id + "}";
            }
            return "{\"erro\": \"Erro desconhecido no DAO\"}";

        } catch (Exception e) {
            // Log 4: Onde o código quebrou?
            System.err.println("ERRO NO CADASTRO:");
            e.printStackTrace();
            res.status(400);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    @Override
    protected void onBeforeUpdate(Usuario u) throws Exception {
        boolean trocouSenha = u.getSenha() != null && !u.getSenha().trim().isEmpty();

        if (!trocouSenha) {
            // "Bypass" provisório na senha só para passar na validação geral
            u.setSenha("123");
        }

        // Valida o resto dos campos (Nome, Email, Login)
        u.validar();

        // Aplica a criptografia apenas se ele realmente quis trocar a senha
        if (trocouSenha) {
            u.setSenha(CriptoService.xor(u.getSenha()));
        }
    }

    //--Funcoes especificas para a tabela--
    public Object getPorLogin(Request req, Response res) {
        try {
            String loginBuscado = req.params(":login");
            UsuarioDAO usuarioDAO = (UsuarioDAO) this.dao;
            Usuario u = usuarioDAO.getByLogin(loginBuscado);

            if (u != null) {
                u.setSenha("");
                res.status(200);
                return gson.toJson(u);
            } else {
                res.status(404);
                return "{\"erro\": \"Login não encontrado\"}";
            }
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    //--funcoes que permitem o proprio usuario editar e ver seu proprio perfil--
    public Object getMe(Request req, Response res) {
        try {
            // Descobre quem está logado pelo Token (Crachá)
            String loginLogado = AuthService.getLoginFromToken(req);

            if (loginLogado == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não autenticado\"}";
            }

            // Busca o usuário no banco
            UsuarioDAO usuarioDAO = (UsuarioDAO) dao;
            Usuario usuario = usuarioDAO.getByLogin(loginLogado);

            //  Retorna os dados
            if (usuario != null) {
                usuario.setSenha(""); // NUNCA devolva a senha, nem mesmo criptografada!
                res.status(200);
                return gson.toJson(usuario);
            } else {
                res.status(404);
                return "{\"erro\": \"Usuário não encontrado\"}";
            }
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object updateMe(Request req, Response res) {
        try {
            // Descobre quem é (Pelo Token)
            String loginLogado = AuthService.getLoginFromToken(req);

            // Busca o original no banco
            UsuarioDAO usuarioDAO = (UsuarioDAO) dao;
            Usuario original = usuarioDAO.getByLogin(loginLogado);

            if (original == null) {
                res.status(401);
                return "{\"erro\": \"Usuário não encontrado\"}";
            }

            // Le o JSON que veio da tela
            Usuario atualizado = gson.fromJson(req.body(), Usuario.class);

            // TRAVA DE SEGURANÇA
            // Força o ID e o Cargo a serem os originais do banco
            atualizado.setId(original.getId());
            atualizado.setRole(original.getRole());

            // Trata a senha (se vazia, mantém a velha; se nova, cifra)
            if (atualizado.getSenha() != null && !atualizado.getSenha().isEmpty()) {
                atualizado.setSenha(CriptoService.xor(atualizado.getSenha()));
            } else {
                atualizado.setSenha(original.getSenha());
            }

            // Salva
            if (dao.update(atualizado)) {
                res.status(200);
                atualizado.setSenha(""); // Esconde senha no retorno
                return gson.toJson(atualizado);
            } else {
                res.status(500);
                return "{\"erro\": \"Erro ao salvar\"}";
            }
        } catch (Exception e) {
            res.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }
}
