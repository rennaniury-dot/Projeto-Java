import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class DespesaFixa extends Despesa {

    public DespesaFixa(String Descricao, int valor, List<Participante> participanteDespesas) {
        super(Descricao, valor, participanteDespesas);
    }

    @Override
    public int calculoPorUsuario(Participante p) {

        if (!getparticipanteDespesas().contains(p)) {
            return 0;
        }

        List<Participante> organizar = new ArrayList<>(getparticipanteDespesas());
        organizar.sort(Comparator.comparingInt(Participante::getIdentificador));

        int valorReal = this.Valor / organizar.size();
        int Valrestante = this.Valor % organizar.size();
        int posicao = organizar.indexOf(p);

        if (posicao < Valrestante) {
            return valorReal + 1;
        }
        return valorReal;
    }
}
