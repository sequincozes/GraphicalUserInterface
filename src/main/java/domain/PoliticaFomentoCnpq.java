package domain;

import java.math.BigDecimal;

public final class PoliticaFomentoCnpq implements PoliticaOrcamento {
    // Valores do exemplo original; não representam uma tabela oficial atualizada.
    private static final BigDecimal VALOR_BOLSA_MENSAL = new BigDecimal("700.00");
    private static final BigDecimal VALOR_FIXO_ANUAL = new BigDecimal("3500.00");

    @Override
    public BigDecimal calcularOrcamento(int quantidadeBolsistas) {
        if (quantidadeBolsistas < 0) {
            throw new IllegalArgumentException("A quantidade de bolsistas não pode ser negativa.");
        }
        return VALOR_BOLSA_MENSAL.multiply(BigDecimal.valueOf(12))
                .multiply(BigDecimal.valueOf(quantidadeBolsistas))
                .add(VALOR_FIXO_ANUAL);
    }
}
