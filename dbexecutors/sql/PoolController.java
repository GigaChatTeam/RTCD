package dbexecutors.sql;

import org.ini4j.Ini;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;

import static java.net.URLEncoder.encode;

public class PoolController {
    private static class Configurator {
        protected static String url;

        private static Ini config;
        protected static String user;
        protected static String password;

        private static boolean firstLoad = true;

        static {
            loadConfigurationFile( );
            loadConfiguration( );
            firstLoad = false;
        }

        private static void loadConfigurationFile () {
            try {
                config = new Ini(new File("./settings.ini"));
            } catch (IOException e) {
                System.out.println("Invalid settings file");
                e.printStackTrace( );
                if (firstLoad) System.exit(1);
            }
        }

        private static void loadConfiguration () {
            String database = config.get("database", "base", String.class);
            String host = config.get("database", "host", String.class);
            Integer port = config.get("database", "port", Integer.class);
            String applicationName = String.format(
                    config.get("database", "application", String.class),
                    config.get("server", "id", String.class));
            String poolName = config.get("database", "pool-name", String.class);

            user = config.get("database", "user", String.class);
            password = config.get("database", "password", String.class);

            url = STR."jdbc:postgresql://\{host}:\{port}/\{database}?ApplicationName=\{encode(STR."\{applicationName} - \{poolName}", StandardCharsets.UTF_8)}";
        }
    }

    private static final ArrayDeque<PolledConnection> connections = new ArrayDeque<>( );
    private static Thread mainThread;

    public static void start (Thread mainThread) {
        PoolController.mainThread = mainThread;

        connections.addFirst(createConnection( ));
        connections.addFirst(createConnection( ));
        connections.addFirst(createConnection( ));

        connectionCreator.start( );
        connectionCleaner.start( );
    }

    @Contract(" -> new")
    private static @Nullable PolledConnection createConnection () {
        try {
            return new PolledConnection(DriverManager.getConnection(Configurator.url, Configurator.user, Configurator.password));
        } catch (SQLException e) {
            System.out.println("Critical error: it was not possible to create database connection");
            e.printStackTrace();
            return null;
        }
    }

    private static final Thread connectionCreator = new Thread(() -> {
        while (mainThread.isAlive( )) {
            if (connections.size( ) < 3) {
                connections.addFirst(createConnection( ));
            }
        }
    });

    private static final Thread connectionCleaner = new Thread(() -> {
        PolledConnection connection;

        while (mainThread.isAlive( )) {
            Long currentTime = System.currentTimeMillis( );

            if (connections.size( ) > 3) {
                connection = connections.getLast( );
                try {
                    if (connection.clearing(currentTime)) {
                        if (connections.removeLastOccurrence(connection)) {
                            System.out.println(STR."The database connection has been released. Currently, \{connections.size( )} connections are reserved.");
                        } else {
                            System.out.println(STR."An outdated database connection could not be cleared. Currently, \{connections.size( )} connections are reserved.");
                        }
                    }
                } catch (SQLException e) {
                    if (connections.removeLastOccurrence(connection)) {
                        System.out.println(STR."The closed database connection has been successfully cleared. Currently, \{connections.size( )} connections are reserved.");
                    } else {
                        System.out.println(STR."The closed database connection could not be cleared. Currently, \{connections.size( )} connections are reserved.");
                    }
                }
            }
        }
    });

    public PolledConnection getConnection () {
        PolledConnection connection;

        do {
            connection = connections.pollLast( );
        } while (connection == null);

        connection.issue( );

        return connection;
    }

    public void returnConnection (PolledConnection connection) {
        connections.addFirst(connection);
    }
}
