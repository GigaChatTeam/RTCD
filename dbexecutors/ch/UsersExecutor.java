package dbexecutors.ch;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseNodes;
import com.clickhouse.data.ClickHouseFormat;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.sql.Timestamp;

public class UsersExecutor {
    static ClickHouseNodes servers = ClickHouseNodes.of(
            "http://localhost:8123?compress=0");

    public static class Logins {
        private static void save (
                @NotNull ClickHouseClient connection,
                @NotNull Long client,
                @NotNull Timestamp timestamp,
                @NotNull String type,
                @NotNull Boolean successfully,
                @NotNull Boolean error,
                @NotNull String agent,
                @NotNull InetSocketAddress address) throws ClickHouseException {
            connection.read(servers)
                    .format(ClickHouseFormat.RowBinary)
                    .query("""
                            INSERT INTO
                                `users`.`logins`
                            VALUES
                                (:client, :timestamp, :type, :success, :error, :agent, :address)
                            """)
                    .params(client, timestamp.toString(), type, successfully, error, agent, address.getAddress( ))
                    .executeAndWait( );
        }

        public static void login (
                @NotNull ClickHouseClient connection,
                @NotNull Long client,
                @NotNull Timestamp timestamp,
                @NotNull Boolean successfully,
                @NotNull Boolean error,
                @NotNull String agent,
                @NotNull InetSocketAddress address) throws ClickHouseException {
            save(connection, client, timestamp, "login", successfully, error, agent, address);
        }

        public static void logout (
                @NotNull ClickHouseClient connection,
                @NotNull Long client,
                @NotNull Timestamp timestamp,
                @NotNull Boolean forced,
                @NotNull Boolean error,
                @NotNull String agent,
                @NotNull InetSocketAddress address) throws ClickHouseException {
            save(connection, client, timestamp, forced ? "forced logout" : "logout", true, error, agent, address);
        }
    }
}
