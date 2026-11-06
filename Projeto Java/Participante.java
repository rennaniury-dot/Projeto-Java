 // CLASSE PARTICIPANTE
 
 public class Participante {

    // ATRIBUTOS DO PARTICIPANTE
    private String nome;
    private double orcamento;

    // CONSTRUTOR
    public Participante(String nome, double orcamento) {
        this.nome = nome;
        this.orcamento = orcamento;
    }

    // GET NOME
    public String getNome() {
        return nome;
    }

    // GET ORCAMENTO
    public double getOrcamento() {
        return orcamento;
    }

    // SET ORCAMENTO
    public void setOrcamento(double orcamento) {
        this.orcamento = orcamento;
    }

}