package dbexecutors;

import exceptions.AccessDenied;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.lang.String.valueOf;
import static java.util.UUID.randomUUID;

public class SystemExecutor extends DBOperator {
    private static String generateTToken (long client, String[] intentions) throws SQLException {
        String ttoken = Helper.SHA512(randomUUID().toString());

        String sql = """
                    INSERT INTO public.ttokens (client, token, extradition, intentions)
                    VALUES
                        (?, ?, now(), ?)
                """;

        PreparedStatement stmt = DBOperator.conn.prepareStatement(sql);
        stmt.setLong(1, client);
        stmt.setString(2, ttoken);
        stmt.setArray(3, DBOperator.conn.createArrayOf("text", intentions));

        stmt.execute();

        return ttoken;
    }

    public static class Channels {
        public static final class Token {
            public String token;
            public String[] intention;

            public Token (String token, String[] intentions) {
                this.token = token;
                this.intention = intentions;
            }
        }

        public static class History {
            @Contract("_, _ -> new")
            public static @NotNull Token loadMessagesHistory (long client, long channel) throws SQLException, AccessDenied {
                if (TTIntentions.Channels.History.validateLoadMessagesHistory(client, channel)) {
                    String[] intention = new String[]{"LOAD", "CHANNELS", "MESSAGES", "HISTORY", valueOf(channel)};
                    return new Token(generateTToken(client, intention), intention);
                } throw new AccessDenied();
            }

            @Contract("_, _ -> new")
            public static @NotNull Token loadPermissions (long client, long channel) throws SQLException, AccessDenied {
                if (TTIntentions.Channels.History.validateLoadPermissions(client, channel)) {
                    String[] intention = new String[]{"LOAD", "CHANNELS", "PERMISSIONS", valueOf(channel)};
                    return new Token(generateTToken(client, intention), intention);
                } throw new AccessDenied();
            }
        }
    }
}
