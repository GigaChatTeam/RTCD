import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DataOperator extends DBOperator {
    @Nullable
    public static Integer createChannel (String title, int owner) {
        String query_createChannel =
            """
                INSERT INTO public.channels (title)
                VALUES (?)
                RETURNING id
            """;
        PreparedStatement stmt;
        int channel_id;
        try {
            stmt = conn.prepareStatement(query_createChannel);
            stmt.setString(1, title);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            channel_id = rs.getInt(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        try {
            conn.prepareStatement(String.format(
                """
                    CREATE TABLE channels.users_%d (
                        id INTEGER NOT NULL,
                        join TIMESTAMP,
                        left TIMESTAMP,
                        FOREIGN KEY (client) REFERENCES public.accounts (id)
                    )
                """, channel_id)).executeQuery();
            conn.prepareStatement(String.format(
                """
                    CREATE TABLE channels.messages_%d (
                        id BIGSERIAL PRIMARY KEY,
                        author INTEGER NOT NULL,
                        type CHAR(8) NOT NULL,
                        text_content TEXT,
                        bytea_content BYTEA,
                        attachments INTEGER[],
                        reply INTEGER,
                        reposting INTEGER[2],
                        user_time TIMESTAMP,
                        server_time TIMESTAMP,
                        FOREIGN KEY (client) REFERENCES public.accounts (id)
                    )
                """, channel_id)).executeQuery();
            conn.prepareStatement(String.format(
                """
                    CREATE TABLE channels.permissions_%d (
                        client INTEGER NOT NULL,
                        permission INTEGER NOT NULL,
                        status BOOLEAN,
                        FOREIGN KEY (client) REFERENCES public.accounts (id),
                        FOREIGN KEY (permission) REFERENCES public.permissions (id),
                    )
                """, owner)).executeQuery();
            conn.prepareStatement(String.format(
                """
                    CREATE TABLE channels.logs_%d (
                        server_time TIMESTAMP,
                        db_time TIMESTAMP DEFAULT now(),
                        data TEXT
                    )
                """, owner)).executeQuery();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        try {
            stmt = conn.prepareStatement(String.format(
                """
                    INSERT INTO channels.users_%d (id, join)
                    VALUES (?, ?)
                """, channel_id));
            stmt.setInt(1, owner);
            stmt.setTimestamp(2, Timestamp.from(Instant.now()));
            stmt.executeUpdate();

            stmt = conn.prepareStatement(String.format(
                """
                    INSERT INTO channels.permissions_%d (client, permission, status)
                    VALUES (?, ?, ?)
                """, channel_id));
            stmt.setInt(1, owner);
            stmt.setInt(2, 0);
            stmt.setBoolean(2, true);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
        return channel_id;
    }
}
