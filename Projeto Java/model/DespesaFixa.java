package model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Despesa de valor FIXO: o valor total é dividido entre os participantes selecionados.
// Os centavos que sobram vão, um a um, para os participantes de menor código.
// Exemplo: R$ 100,00 entre os códigos 1, 2 e 3 -> 33,34 / 33,33 / 33,33
public class DespesaFixa extends Despesa {

    private static final long serialVersionUID = 1L;

    DespesaFixa(int id, String descricao, BigDecimal valor, List<Participante> participantes) {
        super(id, descricao, valor, participantes);
    }

    @Override
    public TipoDespesa getTipo() {
        return TipoDespesa.FIXA;
    }

    @Override
    public Map<Integer, BigDecimal> ratear() {
        List<Participante> envolvidos = getParticipantes(); // já em ordem crescente de código
        int quantidade = envolvidos.size();
        long total = Dinheiro.emCentavos(getValor());
        long base = total / quantidade;
        long resto = total % quantidade;

        Map<Integer, BigDecimal> partes = new LinkedHashMap<>();
        for (int i = 0; i < quantidade; i++) {
            long centavos = base + (i < resto ? 1 : 0);
            partes.put(envolvidos.get(i).getCodigo(), Dinheiro.deCentavos(centavos));
        }
        return partes;
    }
}
