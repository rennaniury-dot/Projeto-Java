package model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Despesa POR PESSOA: cada participante selecionado paga o valor unitário informado.
public class DespesaPorPessoa extends Despesa {

    private static final long serialVersionUID = 1L;

    DespesaPorPessoa(int id, String descricao, BigDecimal valor, List<Participante> participantes) {
        super(id, descricao, valor, participantes);
    }

    @Override
    public TipoDespesa getTipo() {
        return TipoDespesa.POR_PESSOA;
    }

    @Override
    public Map<Integer, BigDecimal> ratear() {
        Map<Integer, BigDecimal> partes = new LinkedHashMap<>();
        for (Participante p : getParticipantes()) {
            partes.put(p.getCodigo(), getValor());
        }
        return partes;
    }
}
