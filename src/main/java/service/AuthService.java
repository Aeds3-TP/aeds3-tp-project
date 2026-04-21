package service;

import java.security.Key;

import com.google.gson.Gson;

import dao.UsuarioDAO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import model.Role;
import model.Usuario;
import spark.Request;
import spark.Response;

public class AuthService {

    private static Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    private UsuarioDAO dao = new UsuarioDAO();

    public Object login(Request req, Response res) {
        // 1. Converte o JSON para objeto
        Usuario dto = new Gson().fromJson(req.body(), Usuario.class);

        // 2. Busca no banco (usuarios.db)
        Usuario uBanco = dao.getByLogin(dto.getLogin());

        // 3. A TRAVA DE SEGURANÇA (Obrigatório ser exatamente assim)
        // Se o usuário não existe (null) OU a senha XOR não bate:
        if (uBanco == null || !CriptoService.xor(dto.getSenha()).equals(uBanco.getSenha())) {
            res.status(401);
            // O halt avisa o Spark para retornar erro 401
            spark.Spark.halt(401, "{\"erro\": \"Credenciais inválidas\"}");
            // O return null garante que o Java PARE aqui e não execute o código debaixo
            return null;
        }

        // 4. SUCESSO (Só chega aqui se o IF acima for falso)
        String token = Jwts.builder()
                .setSubject(uBanco.getLogin())
                .claim("role", uBanco.getRole().name())
                .signWith(key).compact();

        res.cookie("/", "auth_token", token, 3600, false, true);
        return "{\"msg\": \"Logado\", \"role\": \"" + uBanco.getRole() + "\"}";
    }

    public Object logout(Request req, Response res) {
        res.removeCookie("auth_token");
        res.status(200);
        return "{\"msg\": \"Deslogado\"}";
    }

    public static void verificarPermissao(Request req, Role... rolesPermitidos) {
        String token = req.cookie("auth_token");
        if (token == null) {
            spark.Spark.halt(401);
        }

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            Role usuarioRole = Role.valueOf(claims.get("role", String.class));

            boolean permitido = false;
            for (Role r : rolesPermitidos) {
                if (r == usuarioRole) {
                    permitido = true;
                    break;
                }
            }
            if (!permitido) {
                spark.Spark.halt(403);
            }
        } catch (Exception e) {
            spark.Spark.halt(401);
        }
    }

    public static String getLoginFromToken(Request req) {
        try {
            String token = req.cookie("auth_token");
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
