package model;
import java.io.IOException;
import java.io.Serializable;

//Apenas um molde dos models, serve apenas para obrigar que todos os outros models tenham essas funcoes no minimo
public interface Registro extends Serializable {
    int getId();
    void setId(int id);
    byte[] toByteArray() throws IOException;
    void fromByteArray(byte[] b) throws IOException;
}