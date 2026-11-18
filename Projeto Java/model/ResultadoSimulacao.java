package model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ResultadoSimulacao(
    boolean viavel,
    long prioridadeTotal,
    List<Despesa> mantidas,
    List<Despesa> retiradas,
    Map<Integer, BigDecimal> totaisAntes,
    Map<Integer, BigDecimal> totaisDepois,
    Map<Integer, BigDecimal> excessos
) {
    public ResultadoSimulacao {
        mantidas = List.copyOf(mantidas);
        retiradas = List.copyOf(retiradas);
        totaisAntes = Map.copyOf(totaisAntes);
        totaisDepois = Map.copyOf(totaisDepois);
        excessos = Map.copyOf(excessos);
    }
}