package usecase;

import domain.PersistenciaException;
import domain.ProjetoAcademico;
import domain.ProjetoPesquisa;
import domain.ProjetoExtensao;
import domain.PoliticaFomentoCnpq;
import domain.PoliticaFomentoInstitucional;
import domain.RegraNegocioException;
import domain.StatusProjeto;
import domain.TipoProjeto;
import domain.EscritaProjetoRepository;
import domain.LeituraProjetoRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso central que coordena as regras de negócio de projetos acadêmicos.
 * Depende exclusivamente de abstrações do domínio (DIP).
 */
public final class GerenciarProjetoUseCase implements GerenciarProjetos {

    private final java.time.Clock clock;

    private final LeituraProjetoRepository leituraRepo;
    private final EscritaProjetoRepository escritaRepo;

    /**
     * Constrói o caso de uso recebendo as implementações de repositório externamente (Injeção de Dependência).
     *
     * @param leituraRepo implementação de leitura de projetos.
     * @param escritaRepo implementação de escrita de projetos.
     */
    public GerenciarProjetoUseCase(LeituraProjetoRepository leituraRepo, EscritaProjetoRepository escritaRepo) {
        this(leituraRepo, escritaRepo, java.time.Clock.systemDefaultZone());
    }

    public GerenciarProjetoUseCase(LeituraProjetoRepository leituraRepo,
            EscritaProjetoRepository escritaRepo, java.time.Clock clock) {
        this.leituraRepo = java.util.Objects.requireNonNull(leituraRepo);
        this.escritaRepo = java.util.Objects.requireNonNull(escritaRepo);
        this.clock = java.util.Objects.requireNonNull(clock);
    }

    /**
     * Lista projetos acadêmicos filtrados por palavra-chave.
     *
     * @param termo texto de busca (vazio retorna todos).
     * @return lista polimórfica de {@link ProjetoAcademico}.
     * @throws RegraNegocioException caso ocorra erro na consulta.
     */
    public List<ProjetoAcademico> listarProjetos(String termo) throws RegraNegocioException, PersistenciaException {
        return this.leituraRepo.buscarPorTermo(termo);
    }

    /**
     * Cria e persiste um novo projeto acadêmico (Pesquisa ou Extensão).
     *
     * @param codigo código institucional único.
     * @param titulo título do projeto.
     * @param campus campus da UNIPAMPA.
     * @param coordenador nome do professor coordenador.
     * @param bolsistas quantidade inicial de bolsistas.
     * @param tipo modalidade escolhida ({@link TipoProjeto}).
     * @param detalheEspecifico agência de fomento (se Pesquisa) ou comunidade atendida (se Extensão).
     * @throws RegraNegocioException se houver violação nas validações de domínio ou duplicidade.
     */
    public void cadastrarProjeto(String codigo, String titulo, String campus, String coordenador,
                                 int bolsistas, TipoProjeto tipo, String detalheEspecifico)
            throws RegraNegocioException, PersistenciaException {
        if (tipo == null) throw new RegraNegocioException("A modalidade do projeto é obrigatória.");
        ProjetoAcademico novo;
        if (tipo == TipoProjeto.PESQUISA) {
            novo = new ProjetoPesquisa(null, codigo, titulo, campus, coordenador,
                    bolsistas, StatusProjeto.SUBMETIDO, LocalDate.now(clock), detalheEspecifico, new PoliticaFomentoCnpq());
        } else if (tipo == TipoProjeto.EXTENSAO) {
            novo = new ProjetoExtensao(null, codigo, titulo, campus, coordenador,
                    bolsistas, StatusProjeto.SUBMETIDO, LocalDate.now(clock), detalheEspecifico, new PoliticaFomentoInstitucional());
        }
        else { throw new RegraNegocioException("Modalidade não suportada."); }
        this.escritaRepo.salvarNovo(novo);
    }

    /**
     * Aplica a regra de negócio de aprovação sobre um projeto cadastrado.
     *
     * @param id identificador do projeto a ser aprovado.
     * @throws RegraNegocioException se o projeto não existir ou não puder ser aprovado.
     */
    public void aprovarProjeto(Long id) throws RegraNegocioException, PersistenciaException {
        ProjetoAcademico projeto = this.leituraRepo.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Projeto não encontrado para aprovação."));
        projeto.aprovar();
        this.escritaRepo.atualizarEstado(projeto);
    }

    /**
     * Aloca mais um bolsista no projeto selecionado, respeitando o limite máximo.
     *
     * @param id identificador do projeto.
     * @throws RegraNegocioException se o limite de bolsistas for excedido.
     */
    public void adicionarBolsista(Long id) throws RegraNegocioException, PersistenciaException {
        ProjetoAcademico projeto = this.leituraRepo.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Projeto não encontrado."));
        projeto.alocarBolsista();
        this.escritaRepo.atualizarEstado(projeto);
    }

    /**
     * Exclui um projeto do cadastro.
     *
     * @param id identificador do projeto a excluir.
     * @throws RegraNegocioException caso ocorra falha na remoção.
     */
    public void excluirProjeto(Long id) throws RegraNegocioException, PersistenciaException {
        this.escritaRepo.excluirPorId(id);
    }
}