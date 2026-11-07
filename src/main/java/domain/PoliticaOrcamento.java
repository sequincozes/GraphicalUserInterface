package domain;

import java.math.BigDecimal;

/** Calcula o orçamento anual em reais, sem alterar o estado do projeto. */
@FunctionalInterface
public interface PoliticaOrcamento {
    /**
     * @param quantidadeBolsistas quantidade não negativa de bolsistas
     * @return orçamento não nulo e não negativo, em reais
     * @throws IllegalArgumentException se a quantidade for negativa
     */
    BigDecimal calcularOrcamento(int quantidadeBolsistas);
}
