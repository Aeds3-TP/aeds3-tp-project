package service;

public class CriptoService {
    private static final String CHAVE = "ChaveSecretaDoTP";

    //Funcao que faz o xor da criptografia
    public static String xor(String texto) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            sb.append((char) (texto.charAt(i) ^ CHAVE.charAt(i % CHAVE.length())));
        }
        return sb.toString();
    }
}