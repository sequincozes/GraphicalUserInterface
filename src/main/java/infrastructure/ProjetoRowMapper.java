package infrastructure;

import domain.PersistenciaException;
import domain.PoliticaFomentoCnpq;
import domain.PoliticaFomentoInstitucional;
import domain.PoliticaOrcamento;
import domain.ProjetoAcademico;
import domain.ProjetoExtensao;
import domain.ProjetoPesquisa;
import domain.RegraNegocioException;
import domain.StatusProjeto;
import domain.TipoProjeto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Tradução entre o registro relacional e o domínio; visível só neste pacote. */
final class ProjetoRowMapper {
    ProjetoAcademico mapear(ResultSet rs) throws SQLException, PersistenciaException {
        try {
            Long id = rs.getLong("id");
            String codigo = rs.getString("codigo");
            String titulo = rs.getString("titulo");
            String campus = rs.getString("campus");
            String coordenador = rs.getString("coordenador");
            int bolsistas = rs.getInt("bolsistas");
            if (rs.wasNull()) throw new SQLException("Quantidade de bolsistas nula.");
            StatusProjeto status = StatusProjeto.valueOf(rs.getString("status"));
            TipoProjeto tipo = TipoProjeto.valueOf(rs.getString("tipo"));
            String detalhe = rs.getString("detalhe_especifico");
            if (detalhe == null) throw new SQLException("Detalhe específico nulo.");
            java.sql.Date dataSql = rs.getDate("data_cadastro");
            if (dataSql == null) throw new SQLException("Data de cadastro nula.");
            LocalDate data = dataSql.toLocalDate();
            PoliticaOrcamento politica = decodificarPolitica(rs.getString("politica_orcamento"));

            switch (tipo) {
                case PESQUISA:
                    return new ProjetoPesquisa(id, codigo, titulo, campus, coordenador,
                        bolsistas, status, data, detalhe, politica);
                case EXTENSAO:
                    return new ProjetoExtensao(id, codigo, titulo, campus, coordenador,
                        bolsistas, status, data, detalhe, politica);
                default:
                    throw new PersistenciaException("Modalidade de projeto não suportada.");
            }
        } catch (RegraNegocioException | IllegalArgumentException | NullPointerException e) {
            throw new PersistenciaException("Registro de projeto contém dados inválidos.", e);
        }
    }

    String detalheParaGravar(ProjetoAcademico projeto) throws PersistenciaException {
        if (projeto instanceof ProjetoPesquisa) {
            return ((ProjetoPesquisa) projeto).getAgenciaFomento();
        }
        if (projeto instanceof ProjetoExtensao) {
            return ((ProjetoExtensao) projeto).getComunidadeAtendida();
        }
        throw new PersistenciaException("Classe de projeto não suportada pela persistência.");
    }

    String codificarPolitica(PoliticaOrcamento politica) throws PersistenciaException {
        if (politica instanceof PoliticaFomentoCnpq) return "FOMENTO_CNPQ";
        if (politica instanceof PoliticaFomentoInstitucional) return "FOMENTO_INSTITUCIONAL";
        throw new PersistenciaException("Política de orçamento não suportada pela persistência.");
    }

    private PoliticaOrcamento decodificarPolitica(String codigo) throws PersistenciaException {
        if ("FOMENTO_CNPQ".equals(codigo)) return new PoliticaFomentoCnpq();
        if ("FOMENTO_INSTITUCIONAL".equals(codigo)) return new PoliticaFomentoInstitucional();
        throw new PersistenciaException("Identificador de política de orçamento inválido.");
    }
}
