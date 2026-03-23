package model;
import java.io.Serializable;

//é apenas um enum (tipo uma classe) pra obrigar que o usuario tenha uma role dessas
public enum Role implements Serializable{
    ADMIN,      // Manda em tudo
    GESTOR,     // Acesso intermediário
    USUARIO;    // Acesso básico
}