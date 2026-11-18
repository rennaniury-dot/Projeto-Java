package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// Classe ABSTRATA: define o que toda despesa tem e sabe fazer, mas não diz COMO ratear.
// Cada subclasse (DespesaFixa e DespesaPorPessoa) sobrescreve ratear().
// O resto do sistema usa sempre a referência do tipo Despesa: isso é o polimorfismo.
//
// Relação: a Despesa REFERENCIA os participantes envolvidos (associação muitos-para-muitos).
// Quem cria e guarda os participantes é o Planejamento.
public abstract class Despesa implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String descricao;
    private final BigDecimal valor;
    private final List<Participante> participantes; // sempre em ordem crescente de código

    protected Despesa(int id, String descricao, BigDecimal valor, List<Participante> participantes) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        List<Participante> copia = new ArrayList<>(participantes); // cópia: protege a lista original
        copia.sort(Comparator.comparingInt(Participante::getCodigo));
        this.participantes = copia;
    }

    public abstract TipoDespesa getTipo();

    // Quanto cada participante envolvido paga. Chave = código do participante.
    public abstract Map<Integer, BigDecimal> ratear();

    public BigDecimal custoTotal() {
        BigDecimal total = Dinheiro.zero();
        for (BigDecimal parte : ratear().values()) {
            total = total.add(parte);
        }
        return total;
    }

    // Quem não participa tem contribuição zero
    public BigDecimal valorDe(Participante p) {
        BigDecimal parte = ratear().get(p.getCodigo());
        return parte != null ? parte : Dinheiro.zero();
    }

    public boolean participa(Participante p) {
        return participantes.contains(p);
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }
}
