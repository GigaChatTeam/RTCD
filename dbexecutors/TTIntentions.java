package dbexecutors;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class TTIntentions {
    static class Channels {
        static class History {
            static boolean validateLoadMessagesHistory (long client, long channel) throws SQLException {
                String sql = """
                            SELECT
                                channels.is_client_in_channel(?, ?) AND
                                channels.check_permission(?, ?, ARRAY [2::SMALLINT, 2::SMALLINT, 2::SMALLINT, 2::SMALLINT], TRUE)
                        """;

                PreparedStatement stmt = DBOperator.conn.prepareStatement(sql);
                stmt.setLong(1, client);
                stmt.setLong(2, channel);
                stmt.setLong(3, client);
                stmt.setLong(4, channel);

                ResultSet rs = stmt.executeQuery( );

                rs.next( );

                return rs.getBoolean(1);
            }

            static boolean validateLoadPermissions (long client, long channel) throws SQLException {
                String sql = """
                            SELECT channels.is_client_in_channel(?, ?)
                        """;

                PreparedStatement stmt = DBOperator.conn.prepareStatement(sql);
                stmt.setLong(1, client);
                stmt.setLong(2, channel);

                ResultSet rs = stmt.executeQuery( );

                rs.next( );

                return rs.getBoolean(1);
            }
        }
    }
}
