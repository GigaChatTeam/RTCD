package dbexecutors;

import exceptions.AccessDenied;

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

    static class Channels {
        static class History {
            static String loadMessagesHistory (long client, long channel) throws SQLException, AccessDenied {
                if (TTIntentions.Channels.History.validateLoadMessagesHistory(client, channel)) {
                    return generateTToken(client, new String[]{"LOAD", "CHANNELS", "MESSAGES", "HISTORY", valueOf(channel)});
                } throw new AccessDenied();
            }

            static String loadPermissions (long client, long channel) throws SQLException, AccessDenied {
                if (TTIntentions.Channels.History.validateLoadPermissions(client, channel)) {
                    return generateTToken(client, new String[]{"LOAD", "CHANNELS", "MESSAGES", "HISTORY", valueOf(channel)});
                } throw new AccessDenied();
            }
        }
    }
}
