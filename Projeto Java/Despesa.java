       // CLASSE DESPESA
class Despesa {

    private Orcamento orcamento;

    public Despesa(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public void registrarDespesas() {

        // DESPESAS DA ANA
        orcamento.descontarAna(30.00); // Transporte
        orcamento.descontarAna(20.00); // Almoco
        orcamento.descontarAna(15.00); // Atividade
        orcamento.descontarAna(10.00); // Sobremesa

        // DESPESAS DO BRUNO
        orcamento.descontarBruno(30.00); // Transporte
        orcamento.descontarBruno(20.00); // Almoco
        orcamento.descontarBruno(0.00);  // Atividade
        orcamento.descontarBruno(10.00); // Sobremesa

        // DESPESAS DA CARLA
        orcamento.descontarCarla(30.00); // Transporte
        orcamento.descontarCarla(20.00); // Almoco
        orcamento.descontarCarla(15.00); // Atividade
        orcamento.descontarCarla(10.00); // Sobremesa
    }
}
