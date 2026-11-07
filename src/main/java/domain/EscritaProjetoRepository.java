package domain;
public interface EscritaProjetoRepository {
    void salvarNovo(ProjetoAcademico projeto) throws PersistenciaException;
    /** Exige ID; registro inexistente gera PersistenciaException. */
    void atualizarEstado(ProjetoAcademico projeto) throws PersistenciaException;
    /** Ausência do registro não gera erro. */
    void excluirPorId(Long id) throws PersistenciaException;
}
