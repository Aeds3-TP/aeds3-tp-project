package model;

import java.io.*;

public class Usuario implements Registro {
    private int id;
    private String login;
    private String senha;
    private String nome;
    private String email;
    private Role role;

    // Construtor vazio
    public Usuario() {
        this.id = -1;
        this.login = "";
        this.senha = "";
        this.nome = "";
        this.email = ""; // Inicializa vazio para o writeUTF não quebrar
        this.role = Role.USUARIO;
    }

    // Construtor com parametros
    public Usuario(String login, String senha, String nome, String email, Role role) {
        this.id = -1;
        this.login = login;
        this.senha = senha;
        this.nome = nome;
        this.email = email;
        this.role = role;
    }
    
    // Metodo de validação do usuario, ve se o objeto ta na estrutura certa
    public void validar() throws IllegalArgumentException {
        if (this.login == null || this.login.trim().isEmpty()) {
            throw new IllegalArgumentException("O login é obrigatório e não pode ser vazio.");
        }
        
        // Se a senha for nula ou menor que 3 caracteres
        if (this.senha == null || this.senha.trim().length() < 3) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 3 caracteres.");
        }
        
        if (this.nome == null || this.nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome é obrigatório.");
        }
        
        if (this.email == null || !this.email.contains("@")) {
            throw new IllegalArgumentException("O e-mail fornecido é inválido.");
        }
        
        if (this.role == null) {
            this.role = Role.USUARIO;
        }
    }

    //metodos para tranformar para o arquivo binario e vice versa
    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);
        dos.writeUTF(login);
        dos.writeUTF(senha);
        dos.writeUTF(nome);
        dos.writeUTF(email);
        
        dos.writeUTF(role.name()); 

        return baos.toByteArray();
    }

    @Override
    public void fromByteArray(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        DataInputStream dis = new DataInputStream(bais);

        this.id = dis.readInt();
        this.login = dis.readUTF();
        this.senha = dis.readUTF();
        this.nome = dis.readUTF();
        this.email = dis.readUTF();
        
        try {
            this.role = Role.valueOf(dis.readUTF());
        } catch (IllegalArgumentException e) {
            this.role = Role.USUARIO;
        }
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}