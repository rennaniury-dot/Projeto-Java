package model;

import java.io.Serializable;
import java.math.BigDecimal;

// Uma pessoa do planejamento.
// Encapsulamento: todos os atributos são privados. O construtor e o método atualizar()
// são visíveis só dentro do pacote, então apenas o Planejamento cria e altera participantes
// (e, por isso, só ele consegue aplicar as validações).
public class Participante implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int codigo;
    private String nome;
    private BigDecimal orcamento;

    Participante(int codigo, String nome, BigDecimal orcamento) {
        this.codigo = codigo;
        this.nome = nome;
        this.orcamento = orcamento;
    }

    void atualizar(String nome, BigDecimal orcamento) {
        this.nome = nome;
        this.orcamento = orcamento;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getOrcamento() {
        return orcamento;
    }
}
