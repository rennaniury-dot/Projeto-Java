import java.util.List;

public class DespesaPorParticipante extends Despesa {

    public DespesaPorParticipante(String Descricao, int valor, List<Participante> participanteDespesas) {
        super(Descricao, valor, participanteDespesas);

    }@Override
    public int calculoPorUsuario(Participante p) {

        if (!getparticipanteDespesas().contains(p)) {
            return 0;
        } else {
            return this.Valor;
        }
    }
}