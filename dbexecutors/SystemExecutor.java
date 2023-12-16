package dbexecutors;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class SystemExecutor extends DBOperator {
    public static void logAuthentication (long client, String key, String agent, boolean status) throws SQLException {
        String sql = """
                    INSERT INTO users.logins (client, key, login, agent, successfully)
                    VALUES
                        (?, ?, ?, ?, ?)
                """;
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setLong(1, client);
        stmt.setString(2, key);
        stmt.setTimestamp(3, new Timestamp(new Date( ).getTime( )));
        stmt.setString(4, agent);
        stmt.setBoolean(5, status);

        stmt.execute( );
    }

    public static void logAuthentication (long client, String key, String agent) throws SQLException {
        logAuthentication(client, key, agent, true);
    }
}
