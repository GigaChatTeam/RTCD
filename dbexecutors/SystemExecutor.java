package dbexecutors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SystemExecutor extends DBOperator {
    public static void logAuthentication (long client, @NotNull String key, @Nullable String agent, boolean status) throws SQLException {
        String sql = """
                    INSERT INTO users.logins (client, key, login, agent, successfully)
                    VALUES
                        (?, ?, TIMEZONE('UTC', now()), ?, ?)
                """;
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setLong(1, client);
        stmt.setString(2, key);
        stmt.setString(3, agent);
        stmt.setBoolean(4, status);

        stmt.execute( );
    }

    public static void logAuthentication (long client, @NotNull String key, @Nullable String agent) throws SQLException {
        logAuthentication(client, key, agent, true);
    }

    public static void logExit (long client, @NotNull String key) throws SQLException {
        String sql = """
                    UPDATE users.logins
                    SET
                        exit = TIMEZONE('UTC', now())
                    WHERE
                        client = ? AND
                        key = ? AND
                        exit IS NULL
                """;
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setLong(1, client);
        stmt.setString(2, key);

        stmt.execute( );
    }

    public static void logInterruptedLogin (long client, @NotNull String key, @NotNull String agent) throws SQLException {
        String sql = """
                    INSERT INTO users.logins (client, key, login, agent, successfully)
                    VALUES
                        (?, ?, TIMEZONE('UTC', now()), ?, FALSE)
                """;
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setLong(1, client);
        stmt.setString(2, key);
        stmt.setString(3, agent);

        stmt.execute( );
    }
}
