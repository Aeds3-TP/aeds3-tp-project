package model;

//é apenas um enum (tipo uma classe) pra obrigar que o usuario tenha uma role dessas
public enum Role {
    ADMIN,      // Manda em tudo
    GESTOR,     // Acesso intermediário
    USUARIO;    // Acesso básico
}