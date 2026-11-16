 // CLASSE PARTICIPANTE
 import java.util.Objects;
 public class Participante {

     // ATRIBUTOS DO PARTICIPANTE
     private int identificador;
     private String nome;
     private int orcamento;

     // CONSTRUTOR
     public Participante(int identificador, String nome, int orcamento) {
         this.identificador = identificador; //alterei: construir id
         this.nome = nome;
         this.orcamento = orcamento;
     }

     // GET NOME
     public String getNome() {
         return nome;
     }

     // GET ORCAMENTO
     public int getOrcamento() {
         return orcamento;
     }

     //GET ID
     public int getIdentificador() {
         return identificador;
     }

     // SET ORCAMENTO
     public void setOrcamento(int orcamento) {
         this.orcamento = orcamento;
     }

     // SET NOME
     public void setNome(String nome) {
         this.nome = nome;
     }

     @Override
     public boolean equals(Object comparado) {
         if (this == comparado) return true;
         if (comparado == null || getClass() != comparado.getClass()) return false;
         Participante that = (Participante) comparado;
         return identificador == that.identificador;
     }
     @Override
     public int hashCode(){
         return Objects.hash(identificador);
     }

 }


