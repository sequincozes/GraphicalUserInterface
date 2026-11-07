package infrastructure;

import domain.EscritaProjetoRepository;
import domain.LeituraProjetoRepository;
import domain.PersistenciaException;
import domain.ProjetoAcademico;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Operações de persistência; configuração e inicialização são delegadas. */
public final class PostgresProjetoRepository
        implements LeituraProjetoRepository, EscritaProjetoRepository {

    private static final String SELECT_BASE = """
        SELECT id, codigo, titulo, campus, coordenador, bolsistas,
               status, tipo, detalhe_especifico, data_cadastro, politica_orcamento
        FROM projetos_academicos
        """;

    private final ConexaoFactory conexoes;
    private final ProjetoRowMapper mapper = new ProjetoRowMapper();

    public PostgresProjetoRepository(ConexaoFactory conexoes) {
        this.conexoes = Objects.requireNonNull(conexoes, "Fábrica de conexões obrigatória.");
    }

    @Override
    public List<ProjetoAcademico> buscarPorTermo(String termo) throws PersistenciaException {
        // Usa ! como escape de LIKE: % e _ digitados são tratados como texto literal.
        String texto = termo == null ? "" : termo.trim();
        String filtro = "%" + texto.replace("!", "!!").replace("%", "!%")
                                    .replace("_", "!_") + "%";
        String sql = SELECT_BASE + """
            WHERE codigo ILIKE ? ESCAPE '!' OR titulo ILIKE ? ESCAPE '!'
               OR campus ILIKE ? ESCAPE '!' OR coordenador ILIKE ? ESCAPE '!'
            ORDER BY id ASC
            """;
        List<ProjetoAcademico> projetos = new ArrayList<>();
        try (Connection conn = conexoes.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) stmt.setString(i, filtro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) projetos.add(mapper.mapear(rs));
            }
            return projetos;
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível consultar os projetos.", e);
        }
    }

    @Override
    public Optional<ProjetoAcademico> buscarPorId(Long id) throws PersistenciaException {
        Objects.requireNonNull(id, "Identificador obrigatório.");
        try (Connection conn = conexoes.abrir();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + " WHERE id = ?")) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapper.mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível buscar o projeto.", e);
        }
    }

    @Override
    public void salvarNovo(ProjetoAcademico projeto) throws PersistenciaException {
        Objects.requireNonNull(projeto, "Projeto obrigatório.");
        // Valida os tipos suportados antes de abrir a conexão.
        String detalhe = mapper.detalheParaGravar(projeto);
        String politica = mapper.codificarPolitica(projeto.getPoliticaOrcamento());
        String sql = """
            INSERT INTO projetos_academicos
                (codigo, titulo, campus, coordenador, bolsistas, status,
                 tipo, detalhe_especifico, data_cadastro, politica_orcamento)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = conexoes.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projeto.getCodigo());
            stmt.setString(2, projeto.getTitulo());
            stmt.setString(3, projeto.getCampus());
            stmt.setString(4, projeto.getCoordenador());
            stmt.setInt(5, projeto.getBolsistas());
            stmt.setString(6, projeto.getStatus().name());
            stmt.setString(7, projeto.getTipo().name());
            stmt.setString(8, detalhe);
            stmt.setDate(9, Date.valueOf(projeto.getDataCadastro()));
            stmt.setString(10, politica);
            if (stmt.executeUpdate() != 1) {
                throw new PersistenciaException("O projeto não foi cadastrado.");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new PersistenciaException("O cadastro conflita com um registro existente.", e);
            }
            throw new PersistenciaException("Não foi possível cadastrar o projeto.", e);
        }
    }

    @Override
    public void atualizarEstado(ProjetoAcademico projeto) throws PersistenciaException {
        Objects.requireNonNull(projeto, "Projeto obrigatório.");
        Objects.requireNonNull(projeto.getId(), "O projeto precisa ter um identificador.");
        String sql = "UPDATE projetos_academicos SET status = ?, bolsistas = ? WHERE id = ?";
        try (Connection conn = conexoes.abrir();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projeto.getStatus().name());
            stmt.setInt(2, projeto.getBolsistas());
            stmt.setLong(3, projeto.getId());
            if (stmt.executeUpdate() != 1) {
                throw new PersistenciaException("O projeto a atualizar não foi encontrado.");
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível atualizar o projeto.", e);
        }
    }

    @Override
    public void excluirPorId(Long id) throws PersistenciaException {
        Objects.requireNonNull(id, "Identificador obrigatório.");
        try (Connection conn = conexoes.abrir();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM projetos_academicos WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível excluir o projeto.", e);
        }
    }
}
