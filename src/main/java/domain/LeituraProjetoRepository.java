package domain;
import java.util.List;
import java.util.Optional;
public interface LeituraProjetoRepository {
    /** null ou branco: sem filtro; resultado nunca null. */
    List<ProjetoAcademico> buscarPorTermo(String termo) throws PersistenciaException;
    /** ID obrigatório; ausência é Optional.empty(). */
    Optional<ProjetoAcademico> buscarPorId(Long id) throws PersistenciaException;
}
