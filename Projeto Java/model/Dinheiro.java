package model;

import java.math.BigDecimal;

// Ajuda a trabalhar com dinheiro sem erro de arredondamento.
// Todo valor em reais é um BigDecimal com exatamente 2 casas decimais.
// O rateio é calculado em centavos (números inteiros).
public final class Dinheiro {

    private Dinheiro() {
    }

    // Valida e padroniza para 2 casas. Recusa valor nulo ou com mais de 2 casas.
    public static BigDecimal normalizar(BigDecimal valor, String campo) throws RegraNegocioException {
        if (valor == null) {
            throw new RegraNegocioException("Informe o " + campo + ".");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new RegraNegocioException("O " + campo + " deve ter no máximo 2 casas decimais.");
        }
        return valor.setScale(2);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2);
    }

    static long emCentavos(BigDecimal valor) {
        return valor.movePointRight(2).longValueExact();
    }

    static BigDecimal deCentavos(long centavos) {
        return BigDecimal.valueOf(centavos, 2);
    }
}
