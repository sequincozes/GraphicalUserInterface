package usecase;
import domain.PersistenciaException;
import domain.ProjetoAcademico;
import domain.RegraNegocioException;
import domain.TipoProjeto;
import java.util.List;

/** Porta de entrada da aplicação, independente de Swing e JDBC. */
public interface GerenciarProjetos {
    List<ProjetoAcademico> listarProjetos(String termo)
        throws RegraNegocioException, PersistenciaException;
    void cadastrarProjeto(String codigo, String titulo, String campus, String coordenador,
        int bolsistas, TipoProjeto tipo, String detalhe)
        throws RegraNegocioException, PersistenciaException;
    void aprovarProjeto(Long id) throws RegraNegocioException, PersistenciaException;
    void adicionarBolsista(Long id) throws RegraNegocioException, PersistenciaException;
    void excluirProjeto(Long id) throws RegraNegocioException, PersistenciaException;
}
