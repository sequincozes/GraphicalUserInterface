package infrastructure;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/** Configuração de conexão; não executa consultas nem cria tabelas. */
public final class ConexoesPostgres implements ConexaoFactory {
    private final String url;
    private final String usuario;
    private final String senha;

    public ConexoesPostgres(String url, String usuario, String senha) {
        this.url = Objects.requireNonNull(url, "URL obrigatória.");
        this.usuario = Objects.requireNonNull(usuario, "Usuário obrigatório.");
        this.senha = Objects.requireNonNull(senha, "Senha obrigatória.");
        if (url.isBlank() || usuario.isBlank()) {
            throw new IllegalArgumentException("URL e usuário não podem estar em branco.");
        }
    }

    public static ConexoesPostgres doAmbiente() {
        Map<String, String> env = System.getenv();
        return new ConexoesPostgres(
            env.getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/poo"),
            env.getOrDefault("DB_USER", "postgres"),
            env.getOrDefault("DB_PASS", ""));
    }

    @Override
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }
}
