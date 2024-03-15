package dbexecutors;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseProtocol;
import dbexecutors.ch.UsersExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.sql.Timestamp;

public class Users {
    public static class Login {
        public static void login (
                @NotNull Long client,
                @NotNull Timestamp timestamp,
                @NotNull Boolean successfully,
                @NotNull Boolean error,
                @Nullable String agent,
                @NotNull InetSocketAddress address) throws ClickHouseException {

            try (ClickHouseClient connection = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP)) {
                UsersExecutor.Logins.login(
                        connection,
                        client,
                        timestamp,
                        successfully,
                        error,
                        agent == null ? "" : agent,
                        address
                );
            }
        }

        public static void logout (
                @NotNull Long client,
                @NotNull Timestamp timestamp,
                @NotNull Boolean forced,
                @NotNull Boolean error,
                @Nullable String agent,
                @NotNull InetSocketAddress address) throws ClickHouseException {
            try (ClickHouseClient connection = ClickHouseClient.newInstance(ClickHouseProtocol.HTTP)) {
                UsersExecutor.Logins.logout(
                        connection,
                        client,
                        timestamp,
                        forced,
                        error,
                        agent == null ? "" : agent,
                        address
                );
            }
        }
    }
}
