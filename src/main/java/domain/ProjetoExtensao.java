package domain;

import java.time.LocalDate;

public final class ProjetoExtensao extends ProjetoAcademico {
    private final String comunidadeAtendida;

    /**
     * Constrói um Projeto de Extensão invocando o construtor da superclasse via {@code super}.
     *
     * @param id identificador único.
     * @param codigo código institucional.
     * @param titulo título do projeto.
     * @param campus campus da UNIPAMPA.
     * @param coordenador docente coordenador.
     * @param bolsistas número de bolsistas.
     * @param status status atual.
     * @param dataCadastro data de registro.
     * @param comunidadeAtendida público ou comunidade externa beneficiada.
     * @param politicaOrcamento estratégia de cálculo, obrigatória.
     * @throws RegraNegocioException se os dados forem inválidos.
     */
    public ProjetoExtensao(Long id, String codigo, String titulo, String campus,
                           String coordenador, int bolsistas, StatusProjeto status,
                           LocalDate dataCadastro, String comunidadeAtendida,
                           PoliticaOrcamento politicaOrcamento) throws RegraNegocioException {
        super(id, codigo, titulo, campus, coordenador, bolsistas, status, dataCadastro, politicaOrcamento);
        this.comunidadeAtendida = (comunidadeAtendida == null || comunidadeAtendida.isBlank()) ? "Escolas Públicas Locais" : comunidadeAtendida.trim();
    }

    @Override
    public TipoProjeto getTipo() {
        return TipoProjeto.EXTENSAO;
    }

    @Override
    public String getDetalheEspecifico() {
        return "Público: " + this.comunidadeAtendida;
    }
    public String getComunidadeAtendida() { return this.comunidadeAtendida; }
}
