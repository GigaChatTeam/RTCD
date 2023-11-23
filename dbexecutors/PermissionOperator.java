package dbexecutors;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PermissionOperator extends DBOperator {
    public static boolean validateToken (long id, String user_token) {
        String sql = """
                    SELECT token
                    FROM users.tokens
                    WHERE
                        client = ?
                """;
        PreparedStatement stmt;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (Helper.verifierBCrypt(user_token, rs.getString(1).getBytes())) return true;
            }

            stmt.close();
            rs.close();

            return false;
        } catch (SQLException e) {
            return false;
        }
    }
}
