package domain;

import java.time.LocalDate;

public final class ProjetoPesquisa extends ProjetoAcademico {
    private final String agenciaFomento;

    /**
     * Constrói um Projeto de Pesquisa invocando o construtor da superclasse via {@code super}.
     *
     * @param id identificador único.
     * @param codigo código institucional.
     * @param titulo título do projeto.
     * @param campus campus da UNIPAMPA.
     * @param coordenador docente coordenador.
     * @param bolsistas número de bolsistas.
     * @param status status atual.
     * @param dataCadastro data de registro.
     * @param agenciaFomento agência financiadora (ex: CNPq, FAPERGS).
     * @param politicaOrcamento estratégia de cálculo, obrigatória.
     * @throws RegraNegocioException se os dados forem inválidos.
     */
    public ProjetoPesquisa(Long id, String codigo, String titulo, String campus,
                           String coordenador, int bolsistas, StatusProjeto status,
                           LocalDate dataCadastro, String agenciaFomento,
                           PoliticaOrcamento politicaOrcamento) throws RegraNegocioException {
        super(id, codigo, titulo, campus, coordenador, bolsistas, status, dataCadastro, politicaOrcamento);
        this.agenciaFomento = (agenciaFomento == null || agenciaFomento.isBlank()) ? "CNPq / FAPERGS" : agenciaFomento.trim();
    }

    @Override
    public TipoProjeto getTipo() {
        return TipoProjeto.PESQUISA;
    }

    @Override
    public String getDetalheEspecifico() {
        return "Agência: " + this.agenciaFomento;
    }
    public String getAgenciaFomento() { return this.agenciaFomento; }
}
