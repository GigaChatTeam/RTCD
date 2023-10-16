import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelsExecutor extends DBOperator {
    static long create (long owner, String title) throws SQLException {
        String sql = """
            SELECT channels.create(?, ?)
        """;
        PreparedStatement stmt;

        stmt = conn.prepareStatement(sql);
        stmt.setLong(1, owner);
        stmt.setString(2, title);

        ResultSet rs = stmt.executeQuery();

        rs.next();

        return rs.getLong(1);
    }

    static class Users {
        static class Permissions {
            static boolean isClientOnChannel (long client, long channel) throws SQLException {
                String sql = """
                    SELECT EXISTS (
                        SELECT true
                        FROM channels.users
                        WHERE
                            client = ? AND
                            channel = ? AND
                            leaved IS NULL
                    )
                """;
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sql);

                stmt.setLong(1, client);
                stmt.setLong(2, channel);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                return rs.getBoolean(1);
            }
        }

        static void join (long user, long channel, String uri) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.join_user(?, ?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);
            stmt.setString(3, uri);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            if (!rs.getBoolean(1)) throw new AccessDenied();
        }

        static void leave (long user, long channel) throws SQLException, AccessDenied {
            String sql = """
                        SELECT channels.leave_user(?, ?)
                    """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            if (!rs.getBoolean(1)) throw new AccessDenied();
        }
    }
    static class Invitations {
        static @NotNull String create(long user, long channel) throws SQLException, AccessDenied {
            String sql = """
                SELECT channels.create_invitation(?, ?)
            """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, user);
            stmt.setLong(2, channel);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            String uri = rs.getString(1);

            if (uri != null) return uri;
            else throw new AccessDenied();
        }
//        void create (long user, long channel, String uri) throws SQLException, AccessDenied {
//
//        }
static void delete(long user, String uri) throws SQLException, AccessDenied {

}
    }

    static class Messages {
        static long postMessage (long author, long channel, String text) throws SQLException {
            String sql = """
                SELECT channels.post_message(?, ?, ?)
            """;
            PreparedStatement stmt;

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, author);
            stmt.setLong(2, channel);
            stmt.setString(3, text);

            ResultSet rs = stmt.executeQuery();

            rs.next();

            return rs.getLong(1);
        }
    }
}
