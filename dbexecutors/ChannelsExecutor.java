package dbexecutors;

import exceptions.AccessDenied;
import exceptions.NotFound;
import exceptions.NotValid;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ChannelsExecutor extends DBOperator {
    public static long create (long owner, String title) throws SQLException {
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

    public static class Users {
        public static void join (long user, long channel, String uri) throws SQLException, AccessDenied {
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

        public static void leave (long user, long channel) throws SQLException, AccessDenied {
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

        public static class Permissions {
            public static boolean isClientOnChannel (long client, long channel) throws SQLException {
                String sql = """
                            SELECT EXISTS (
                                SELECT *
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
    }

    public static class Invitations {
        public static @NotNull String create (long user, long channel) throws SQLException, AccessDenied {
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

        public static void delete (long user, String uri) throws SQLException, AccessDenied {

        }
    }

    public static class Messages {
        public static Timestamp post (long author, long channel, @NotNull String text, long[][] media, long[] audio) {
            return null;
        }

        public static void edit (long author, long channel, Timestamp posted, String text, long[][] media, long[] audio) {

        }
    }

    public static class Settings {
        public static class External {
            public static void changeTitle (long user, long channel, @NotNull String newTitle) throws SQLException, NotFound, NotValid {
                if (newTitle.length() > 2 && newTitle.length() < 33) throw new NotValid();

                String sql = """
                            SELECT channels.change_title(%s, %s, %s)
                        """;
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, user);
                stmt.setLong(2, channel);
                stmt.setString(3, newTitle);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                if (rs.getBoolean(1)) throw new NotFound();
            }

            public static void changeDescription (long user, long channel, @NotNull String newDescription) throws SQLException, NotFound, NotValid {
                if (newDescription.length() < 257) throw new NotValid();

                String sql = """
                            SELECT channels.change_description(%s, %s, %s)
                        """;
                PreparedStatement stmt;

                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, user);
                stmt.setLong(2, channel);
                stmt.setString(3, newDescription);

                ResultSet rs = stmt.executeQuery();

                rs.next();

                if (rs.getBoolean(1)) throw new NotFound();
            }
        }
    }
}
