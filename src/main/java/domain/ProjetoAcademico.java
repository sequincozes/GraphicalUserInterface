package domain;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

public abstract class ProjetoAcademico {
    private static final int LIMITE_MAXIMO_BOLSISTAS = 10;

    private final Long id;
    private final String codigo;
    private final String titulo;
    private final String campus;
    private final String coordenador;
    private int bolsistas;
    private StatusProjeto status;
    private final LocalDate dataCadastro;
    private final PoliticaOrcamento politicaOrcamento;

    /**
     * Construtor base que garante a criação de um projeto em estado válido.
     *
     * @param id identificador único (pode ser null antes de persistir).
     * @param codigo código institucional (ex: PRJ-101).
     * @param titulo título descritivo do projeto.
     * @param campus campus de origem na UNIPAMPA.
     * @param coordenador nome do docente responsável.
     * @param bolsistas quantidade inicial de bolsistas.
     * @param status estado atual do projeto.
     * @param dataCadastro data de registro do projeto.
     * @param politicaOrcamento estratégia de cálculo de orçamento.
     * @throws RegraNegocioException caso algum dado essencial seja inválido.
     */
    protected ProjetoAcademico(Long id, String codigo, String titulo, String campus,
                               String coordenador, int bolsistas, StatusProjeto status,
                               LocalDate dataCadastro, PoliticaOrcamento politicaOrcamento)
            throws RegraNegocioException {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraNegocioException("O código do projeto é obrigatório.");
        }
        if (titulo == null || titulo.trim().length() < 5) {
            throw new RegraNegocioException("O título do projeto deve ter pelo menos 5 caracteres.");
        }
        if (coordenador == null || coordenador.isBlank()) {
            throw new RegraNegocioException("O nome do coordenador é obrigatório.");
        }
        if (bolsistas < 0 || bolsistas > LIMITE_MAXIMO_BOLSISTAS) {
            throw new RegraNegocioException("A quantidade de bolsistas deve estar entre 0 e " + LIMITE_MAXIMO_BOLSISTAS + ".");
        }

        this.id = id;
        this.codigo = codigo.trim().toUpperCase(Locale.ROOT);
        this.titulo = titulo.trim();
        this.campus = (campus == null || campus.isBlank()) ? "Alegrete" : campus.trim();
        this.coordenador = coordenador.trim();
        this.bolsistas = bolsistas;
        this.status = (status == null) ? StatusProjeto.SUBMETIDO : status;
        this.dataCadastro = (dataCadastro == null) ? LocalDate.now() : dataCadastro;
        this.politicaOrcamento = Objects.requireNonNull(
            politicaOrcamento, "A política de orçamento é obrigatória.");
    }

    /**
     * Aprova o projeto caso esteja em estado elegível (Regra de Negócio).
     * @throws RegraNegocioException se o projeto já estiver aprovado ou cancelado.
     */
    public void aprovar() throws RegraNegocioException {
        if (this.status == StatusProjeto.APROVADO) {
            throw new RegraNegocioException("Este projeto já se encontra APROVADO.");
        }
        if (this.status == StatusProjeto.CANCELADO) {
            throw new RegraNegocioException("Não é permitido aprovar um projeto CANCELADO.");
        }
        this.status = StatusProjeto.APROVADO;
    }

    /**
     * Aloca um novo bolsista ao projeto respeitando o teto institucional.
     * @throws RegraNegocioException se o limite máximo de bolsistas for atingido ou se estiver cancelado.
     */
    public void alocarBolsista() throws RegraNegocioException {
        if (this.status == StatusProjeto.CANCELADO) {
            throw new RegraNegocioException("Não é possível alocar bolsistas em projetos cancelados.");
        }
        if (this.bolsistas >= LIMITE_MAXIMO_BOLSISTAS) {
            throw new RegraNegocioException("Limite máximo de " + LIMITE_MAXIMO_BOLSISTAS + " bolsistas atingido para este projeto.");
        }
        this.bolsistas++;
    }

    /**
     * Calcula o orçamento anual do projeto delegando para a política configurada.
     * @return valor total do orçamento em reais.
     */
    public BigDecimal calcularOrcamentoTotal() {
        return this.politicaOrcamento.calcularOrcamento(this.bolsistas);
    }

    /**
     * Retorna a modalidade do projeto.
     * @return instância de {@link TipoProjeto}.
     */
    public abstract TipoProjeto getTipo();

    /**
     * Retorna o metadado específico da subclasse (Polimorfismo / LSP).
     * @return detalhe específico (agência de fomento ou público-alvo).
     */
    public abstract String getDetalheEspecifico();

    /**
     * Gera um resumo textual completo do projeto.
     * @return resumo formatado.
     */
    public String gerarResumo() {
        return String.format("[%s] %s (%s) - %s", this.codigo, this.titulo, getTipo().name(), getDetalheEspecifico());
    }

    public Long getId() { return this.id; }
    public String getCodigo() { return this.codigo; }
    public String getTitulo() { return this.titulo; }
    public String getCampus() { return this.campus; }
    public String getCoordenador() { return this.coordenador; }
    public int getBolsistas() { return this.bolsistas; }
    public StatusProjeto getStatus() { return this.status; }
    public LocalDate getDataCadastro() { return this.dataCadastro; }
    public PoliticaOrcamento getPoliticaOrcamento() { return this.politicaOrcamento; }
}
