package teste;

import model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TestesSimulador {

    public static void main(String[] args) throws Exception {

        Planejamento p = new Planejamento("Passeio da turma");

        p.cadastrarParticipante(1, "Ana", d("70.00"));
        p.cadastrarParticipante(2, "Bruno", d("60.00"));
        p.cadastrarParticipante(3, "Carla", d("70.00"));

        p.incluirDespesa(
            TipoDespesa.FIXA, "Transporte",
            d("90.00"), List.of(1, 2, 3)
        );

        p.incluirDespesa(
            TipoDespesa.POR_PESSOA, "Almoco",
            d("20.00"), List.of(1, 2, 3)
        );

        p.incluirDespesa(
            TipoDespesa.FIXA, "Atividade recreativa",
            d("30.00"), List.of(1, 3)
        );

        p.incluirDespesa(
            TipoDespesa.POR_PESSOA, "Sobremesa",
            d("10.00"), List.of(1, 2, 3)
        );

        List<Despesa> despesas = p.getDespesas();

        List<Participante> participantes =
            despesas.get(0).getParticipantes();

        Map<Integer, Integer> prioridades = Map.of(
            despesas.get(0).getId(), 0,
            despesas.get(1).getId(), 0,
            despesas.get(2).getId(), 3,
            despesas.get(3).getId(), 1
        );

        ResultadoSimulacao resultado =
            SimuladorAlternativas.simular(
                participantes, despesas, prioridades
            );

        System.out.println("=== SIMULADOR DE ALTERNATIVAS ===");

        verificar(resultado.viavel(), "Solucao viavel");

        verificar(resultado.prioridadeTotal() == 3,
            "Prioridade maxima");

        verificar(resultado.mantidas().size() == 3,
            "Tres despesas mantidas");

        verificar(resultado.retiradas().size() == 1
            && resultado.retiradas().get(0).getDescricao()
                .equals("Sobremesa"),
            "Sobremesa retirada");

        verificar(resultado.totaisDepois().get(1)
            .compareTo(d("65.00")) == 0, "Total de Ana");

        verificar(resultado.totaisDepois().get(2)
            .compareTo(d("50.00")) == 0, "Total de Bruno");

        verificar(resultado.totaisDepois().get(3)
            .compareTo(d("65.00")) == 0, "Total de Carla");

        BigDecimal total = BigDecimal.ZERO;

        for (BigDecimal valor : resultado.totaisDepois().values()) {
            total = total.add(valor);
        }

        verificar(total.compareTo(d("180.00")) == 0,
            "Total geral de R$ 180,00");

        verificar(p.getDespesas().size() == 4,
            "Planejamento original preservado");

        System.out.println("\n--- ANTES E DEPOIS ---");

        for (Participante pessoa : participantes) {

            int codigo = pessoa.getCodigo();

            System.out.println(
                pessoa.getNome()
                + ": Antes R$ " + resultado.totaisAntes().get(codigo)
                + " | Depois R$ " + resultado.totaisDepois().get(codigo)
            );
        }

        System.out.println("Total simulado: R$ " + total);

        // Segundo cenario: ausencia de solucao

        p.alterarParticipante(1, "Ana", d("40.00"));

        ResultadoSimulacao semSolucao =
            SimuladorAlternativas.simular(
                participantes, p.getDespesas(), prioridades
            );

        verificar(!semSolucao.viavel(),
            "Ausencia de solucao identificada");

        verificar(semSolucao.excessos().containsKey(1)
            && semSolucao.excessos().get(1)
                .compareTo(d("10.00")) == 0,
            "Excesso de Ana: R$ 10,00");

        System.out.println("\nTODAS AS VERIFICACOES PASSARAM!");
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    private static void verificar(
            boolean condicao, String mensagem) {

        if (!condicao) {
            throw new AssertionError("FALHA: " + mensagem);
        }

        System.out.println("[OK] " + mensagem);
    }
}