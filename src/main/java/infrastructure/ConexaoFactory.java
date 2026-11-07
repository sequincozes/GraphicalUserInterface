package infrastructure;
import java.sql.Connection;
import java.sql.SQLException;
/** Cada chamada entrega uma conexão que o chamador deve fechar. */
@FunctionalInterface
public interface ConexaoFactory {
    Connection abrir() throws SQLException;
}
