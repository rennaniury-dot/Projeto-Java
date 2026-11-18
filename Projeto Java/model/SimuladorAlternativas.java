package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimuladorAlternativas {

    public static ResultadoSimulacao simular(
            Collection<Participante> participantes,
            Collection<Despesa> despesas,
            Map<Integer, Integer> prioridades) {

        List<Despesa> essenciais = new ArrayList<>();
        List<Despesa> opcionais = new ArrayList<>();

        // Classificar as despesas
        // Prioridade 0 = essencial
        // Prioridade positiva = opcional
        for (Despesa d : despesas) {

            Integer prioridade = prioridades.get(d.getId());

            if (prioridade == null || prioridade < 0) {
                throw new IllegalArgumentException(
                    "Classificacao invalida: " + d.getDescricao()
                );
            }

            if (prioridade == 0) {
                essenciais.add(d);
            } else {
                opcionais.add(d);
            }
        }

        // Limite exigido pelo professor
        if (opcionais.size() > 10) {
            throw new IllegalArgumentException(
                "Limite de 10 despesas opcionais."
            );
        }

        Map<Integer, BigDecimal> antes =
            calcularTotais(participantes, despesas);

        Map<Integer, BigDecimal> totalEssenciais =
            calcularTotais(participantes, essenciais);

        Map<Integer, BigDecimal> excessos =
            calcularExcessos(participantes, totalEssenciais);

        // Verificar se as essenciais cabem no orcamento
        if (!excessos.isEmpty()) {
            return new ResultadoSimulacao(
                false,
                0,
                essenciais,
                opcionais,
                antes,
                totalEssenciais,
                excessos
            );
        }

        long melhorPrioridade = -1;

        List<Despesa> melhoresMantidas = new ArrayList<>();
        List<Despesa> melhoresRetiradas = new ArrayList<>();

        Map<Integer, BigDecimal> melhoresTotais =
            new LinkedHashMap<>();

        int combinacoes = 1 << opcionais.size();

        // Testar todas as combinacoes possiveis
        for (int mascara = 0; mascara < combinacoes; mascara++) {

            List<Despesa> mantidas =
                new ArrayList<>(essenciais);

            List<Despesa> retiradas = new ArrayList<>();

            long prioridadeTotal = 0;

            for (int i = 0; i < opcionais.size(); i++) {

                Despesa d = opcionais.get(i);

                if ((mascara & (1 << i)) != 0) {
                    mantidas.add(d);
                    prioridadeTotal += prioridades.get(d.getId());
                } else {
                    retiradas.add(d);
                }
            }

            Map<Integer, BigDecimal> totais =
                calcularTotais(participantes, mantidas);

            boolean viavel =
                calcularExcessos(participantes, totais).isEmpty();

            if (viavel && prioridadeTotal > melhorPrioridade) {

                melhorPrioridade = prioridadeTotal;
                melhoresMantidas = mantidas;
                melhoresRetiradas = retiradas;
                melhoresTotais = totais;
            }
        }

        return new ResultadoSimulacao(
            true,
            melhorPrioridade,
            melhoresMantidas,
            melhoresRetiradas,
            antes,
            melhoresTotais,
            Map.of()
        );
    }

    // Calcular o total de cada participante
    private static Map<Integer, BigDecimal> calcularTotais(
            Collection<Participante> participantes,
            Collection<Despesa> despesas) {

        Map<Integer, BigDecimal> totais =
            new LinkedHashMap<>();

        for (Participante p : participantes) {

            BigDecimal total = BigDecimal.ZERO;

            for (Despesa d : despesas) {
                total = total.add(d.valorDe(p));
            }

            totais.put(p.getCodigo(), total);
        }

        return totais;
    }

    // Verificar os excessos de orcamento
    private static Map<Integer, BigDecimal> calcularExcessos(
            Collection<Participante> participantes,
            Map<Integer, BigDecimal> totais) {

        Map<Integer, BigDecimal> excessos =
            new LinkedHashMap<>();

        for (Participante p : participantes) {

            BigDecimal total = totais.get(p.getCodigo());

            BigDecimal excesso =
                total.subtract(p.getOrcamento());

            if (excesso.signum() > 0) {
                excessos.put(p.getCodigo(), excesso);
            }
        }

        return excessos;
    }
}