package infrastructure;

import domain.PersistenciaException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/** Bootstrap didático. Em produção, evolua o esquema com migrações versionadas. */
public final class InicializadorBanco {
    private final ConexaoFactory conexoes;

    public InicializadorBanco(ConexaoFactory conexoes) {
        this.conexoes = Objects.requireNonNull(conexoes, "Fábrica de conexões obrigatória.");
    }

    /** Criação, adaptação do esquema antigo e seed opcional na mesma transação. */
    public void inicializar(boolean incluirDadosDemonstracao) throws PersistenciaException {
        try (Connection conn = conexoes.abrir()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS projetos_academicos (
                        id BIGSERIAL PRIMARY KEY,
                        codigo VARCHAR(20) NOT NULL UNIQUE,
                        titulo VARCHAR(180) NOT NULL,
                        campus VARCHAR(80) NOT NULL,
                        coordenador VARCHAR(120) NOT NULL,
                        bolsistas INT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        tipo VARCHAR(30) NOT NULL,
                        detalhe_especifico VARCHAR(140) NOT NULL,
                        data_cadastro DATE NOT NULL,
                        politica_orcamento VARCHAR(40) NOT NULL
                    )
                    """);
                stmt.executeUpdate("""
                    ALTER TABLE projetos_academicos
                    ADD COLUMN IF NOT EXISTS politica_orcamento VARCHAR(40)
                    """);
                // Reconstrói a associação fixa que existia nos construtores antigos.
                stmt.executeUpdate("""
                    UPDATE projetos_academicos
                    SET politica_orcamento = CASE tipo
                        WHEN 'PESQUISA' THEN 'FOMENTO_CNPQ'
                        WHEN 'EXTENSAO' THEN 'FOMENTO_INSTITUCIONAL'
                    END
                    WHERE politica_orcamento IS NULL
                    """);
                stmt.executeUpdate("""
                    ALTER TABLE projetos_academicos
                    ALTER COLUMN politica_orcamento SET NOT NULL
                    """);
                if (incluirDadosDemonstracao) inserirDemonstracaoSeVazio(stmt);
                conn.commit();
            } catch (SQLException e) {
                try { conn.rollback(); }
                catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível inicializar o banco de projetos.", e);
        }
    }

    private void inserirDemonstracaoSeVazio(Statement stmt) throws SQLException {
        // Serializa a verificação e a inserção, evitando seeds concorrentes duplicados.
        stmt.execute("LOCK TABLE projetos_academicos IN SHARE ROW EXCLUSIVE MODE");
        try (ResultSet rs = stmt.executeQuery("SELECT EXISTS (SELECT 1 FROM projetos_academicos)")) {
            if (rs.next() && rs.getBoolean(1)) return;
        }
        stmt.executeUpdate("""
            INSERT INTO projetos_academicos
                (codigo, titulo, campus, coordenador, bolsistas, status,
                 tipo, detalhe_especifico, data_cadastro, politica_orcamento)
            VALUES
                ('PRJ-101', 'IA Generativa e Small Language Models na Educação',
                 'Alegrete', 'Profa. Camilla Quincozes', 4, 'APROVADO',
                 'PESQUISA', 'CNPq / FAPERGS', CURRENT_DATE - 20, 'FOMENTO_CNPQ'),
                ('PRJ-102', 'Detecção de Intrusão e Segurança em Redes IoT',
                 'Alegrete', 'Prof. Dr. Silvio Quincozes', 5, 'APROVADO',
                 'PESQUISA', 'FAPERGS', CURRENT_DATE - 15, 'FOMENTO_CNPQ'),
                ('PRJ-201', 'Gurias na Computação e Oficinas de Git nas Escolas',
                 'Alegrete', 'Profa. Camilla Quincozes', 3, 'SUBMETIDO',
                 'EXTENSAO', 'Rede Pública de Alegrete', CURRENT_DATE - 5, 'FOMENTO_INSTITUCIONAL'),
                ('PRJ-204', 'Codefólio: Produção de Conteúdo em Engenharia de Software',
                 'Alegrete', 'Equipe CodeTalks', 2, 'SUBMETIDO',
                 'EXTENSAO', 'Estudantes de Computação', CURRENT_DATE - 2, 'FOMENTO_INSTITUCIONAL')
            """);
    }
}
