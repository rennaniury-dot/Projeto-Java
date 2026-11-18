package model;

// FIXA       = o valor informado é o TOTAL e é dividido entre os participantes selecionados
// POR_PESSOA = o valor informado é cobrado de CADA participante selecionado
public enum TipoDespesa {

    FIXA("Fixa"),
    POR_PESSOA("Por pessoa");

    private final String rotulo;

    TipoDespesa(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo;
    }
}
