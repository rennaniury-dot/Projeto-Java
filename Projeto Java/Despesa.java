       // CLASSE DESPESA
       import java.util.List;
       public abstract class Despesa {

           //Atributos despesas:
           private String Descricaodespesas;
           protected int Valor;
           private List<Participante> participanteDespesas;

           //Construtor das despelistasas
           public Despesa(String Descricao, int valor, List<Participante> participantedespesas) {
               this.Descricaodespesas = Descricao;
               this.Valor = valor;
               this.participanteDespesas = participantedespesas;
           }
           public List<Participante> getparticipanteDespesas() {
               return this.participanteDespesas;
           }

           public String getDescricaodespesas() {
               return this.Descricaodespesas;
           }

           public int getValor() {
               return this.Valor;
           }

           public abstract int calculoPorUsuario(Participante p);

           public void setDescricaodespesas(String Descricao) {
               this.Descricaodespesas = Descricao;
           }
           public void setValor(int valor) {this.Valor = valor;}
           public void setparticipanteDespesas(List<Participante> participantedespesas){ this.participanteDespesas=participantedespesas; }
       }