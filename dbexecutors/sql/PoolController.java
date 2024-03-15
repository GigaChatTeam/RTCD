package dbexecutors.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

import static org.postgresql.util.URLCoder.encode;

public class PoolController {
    private static final HikariConfig connectionConfig = new HikariConfig( );
    private static HikariDataSource connectionDataSource = null;

    public static class Configuration {
        protected String url;

        protected String database;
        protected Integer port;
        protected String host;
        protected String applicationName;
        protected String user;
        protected String password;

        protected Integer minConnections;
        protected Integer maxConnections;
        protected Long connectionTimeout;

        public Configuration (@NotNull Ini configFile) {
            database = configFile.get("database", "base", String.class);
            host = configFile.get("database", "host", String.class);
            port = configFile.get("database", "port", Integer.class);
            String applicationTitle = String.format(
                    configFile.get("database", "application", String.class),
                    configFile.get("server", "id", String.class));
            String poolName = configFile.get("database", "pool-name", String.class);

            user = configFile.get("database", "user", String.class);
            password = configFile.get("database", "password", String.class);
            applicationName = STR."\{applicationTitle} - \{poolName}";
            url = STR."jdbc:postgresql://\{host}:\{port}/\{database}?ApplicationName=\{encode(STR."\{applicationTitle} - \{poolName}")}";

            minConnections = configFile.get("database-pool", "min-connections", Integer.class);
            maxConnections = configFile.get("database-pool", "max-connections", Integer.class);
            connectionTimeout = configFile.get("database-pool", "timeout", Long.class);
        }
    }

    public synchronized static void initializePool (@NotNull Configuration configuration) {
        connectionConfig.setJdbcUrl(configuration.url);
        connectionConfig.setUsername(configuration.user);
        connectionConfig.setPassword(configuration.password);

        connectionConfig.setMaximumPoolSize(configuration.maxConnections);
        connectionConfig.setConnectionTimeout(configuration.connectionTimeout);

        connectionConfig.setAutoCommit(false);

        connectionDataSource = new HikariDataSource(connectionConfig);
    }

    public static @NotNull Connection getConnection ( ) throws SQLException {
        return getConnection(Connection.TRANSACTION_SERIALIZABLE);
    }

    public static @NotNull Connection getConnection (int isolation) throws SQLException {
        Connection connection = connectionDataSource.getConnection( );

        connection.setTransactionIsolation(isolation);

        return connection;
    }
}
