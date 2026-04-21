package service;

import dao.UsuarioDAO;
import model.Role;
import model.Usuario;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import spark.Request;
import spark.Response;
import java.security.Key;

//Aqui tem uma parte mais de seguranca, faz a configuracao dos cookies chama o xor, login etc
public class AuthService {
    // Chave de assinatura (O segredo do servidor)
    private static Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    private UsuarioDAO dao = new UsuarioDAO();

    // login
    public Object login(Request req, Response res) {
        Usuario dto = new Gson().fromJson(req.body(), Usuario.class);
        Usuario uBanco = dao.getByLogin(dto.getLogin());

        // Valida Senha (XOR com XOR se anulam)
        if (uBanco != null && CriptoService.xor(dto.getSenha()).equals(uBanco.getSenha())) {
            
            // Cria Token
            String token = Jwts.builder()
                    .setSubject(uBanco.getLogin()) // Salva o login no "Subject"
                    .claim("role", uBanco.getRole().name())
                    .signWith(key).compact();

            // Cookie HttpOnly
            res.cookie("auth_token", token, 3600, false, true);
            return "{\"msg\": \"Logado\", \"role\": \"" + uBanco.getRole() + "\"}";
        }
        res.status(401);
        return "{\"erro\": \"Login invalido\"}";
    }
    
    // log out
    public Object logout(Request req, Response res) {
        // A função removeCookie diz ao navegador para deletar o cookie 'auth_token'
        res.removeCookie("auth_token");
        
        // Alternativa manual (caso o navegador seja teimoso):
        // res.cookie("auth_token", "", 0, false, true); 

        res.status(200);
        return "{\"msg\": \"Deslogado com sucesso\"}";
    }

    // -- Pega o token no cookie do navegar e verifica a role/permissão do usuario --
    public static void verificarPermissao(Request req, Role... rolesPermitidos) {
        String token = req.cookie("auth_token");
        if (token == null) spark.Spark.halt(401, "{\"erro\": \"Nao autenticado\"}");

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();

            String roleStr = claims.get("role", String.class);
            Role usuarioRole = Role.valueOf(roleStr);

            boolean permitido = false;
            for (Role r : rolesPermitidos) {
                if (r == usuarioRole) {
                    permitido = true;
                    break;
                }
            }

            if (!permitido) spark.Spark.halt(403, "{\"erro\": \"Acesso Negado para seu perfil\"}");

        } catch (spark.HaltException e) {
            throw e; 
            
        }catch (Exception e) {
            spark.Spark.halt(401, "{\"erro\": \"Token invalido\"}");
        }
    }

    // -- Verifica o login do token (é utilizado pra permitir alguns casos como o do proprio usuario ver e alterar o proprio perfil) --
    public static String getLoginFromToken(Request req) {
        String token = req.cookie("auth_token");
        if (token == null) return null;

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}