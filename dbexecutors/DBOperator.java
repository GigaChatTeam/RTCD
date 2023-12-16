package dbexecutors;

import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static java.net.URLEncoder.encode;

public abstract class DBOperator {
    static String url;

    static Ini config;
    static String database;
    static String user;
    static String password;
    static String host;
    static int port;
    static String applicationName;
    static Connection conn;

    static {
        try {
            config = new Ini(new File("./settings.ini"));
        } catch (IOException e) {
            System.out.println("Invalid settings file");
            e.printStackTrace( );
            System.exit(1);
        }
    }

    static {
        try {
            database = config.get("database", "base", String.class);
            user = config.get("database", "user", String.class);
            password = config.get("database", "password", String.class);
            host = config.get("database", "host", String.class);
            port = config.get("database", "port", int.class);
            applicationName = String.format(
                    config.get("database", "application", String.class),
                    config.get("server", "id", String.class));

            url = STR."jdbc:postgresql://\{host}:\{port}/\{database}?ApplicationName=\{encode(applicationName, StandardCharsets.UTF_8)}";

            try {
                conn = DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                System.out.println("DB connection error");
                e.printStackTrace( );
                System.exit(1);
            }
        } catch (Exception e) {
            e.printStackTrace( );
        }
    }
}

