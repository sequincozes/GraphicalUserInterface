import domain.PersistenciaException;
import infrastructure.ConexaoFactory;
import infrastructure.ConexoesPostgres;
import infrastructure.InicializadorBanco;
import infrastructure.PostgresProjetoRepository;
import presentation.DashboardView;
import presentation.TemaAplicacao;
import usecase.GerenciarProjetoUseCase;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Ponto de montagem: único lugar que conecta apresentação e infraestrutura. */
public final class App {
    private App() { }
    public static void main(String[] args) {
        ConexaoFactory conexoes = ConexoesPostgres.doAmbiente();
        try {
            // A preparação do banco acontece fora da EDT.
            boolean demonstracao = Boolean.parseBoolean(
                System.getenv().getOrDefault("DB_SEED_DEMO", "false"));
            new InicializadorBanco(conexoes).inicializar(demonstracao);
        } catch (PersistenciaException e) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, "Falha ao iniciar", e);
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
                e.getMessage(), "Inicialização", JOptionPane.ERROR_MESSAGE));
            return;
        }
        PostgresProjetoRepository repository = new PostgresProjetoRepository(conexoes);
        GerenciarProjetoUseCase useCase = new GerenciarProjetoUseCase(repository, repository);
        SwingUtilities.invokeLater(() -> {
            TemaAplicacao.configurar();
            new DashboardView(useCase).setVisible(true);
        });
    }
}
