package dbexecutors;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static dbexecutors.Helper.verifierBCrypt;

public class PermissionOperator extends DBOperator {
    public static boolean validateToken (long id, String secret, String key) {
        String sql = """
                    SELECT
                        secret,
                        key
                    FROM users.tokens
                    WHERE
                        client = ?
                """;
        PreparedStatement stmt;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, id);

            ResultSet rs = stmt.executeQuery( );

            while (rs.next( )) {
                if (
                        verifierBCrypt(secret, rs.getString(1).getBytes( )) &&
                        verifierBCrypt(key, rs.getString(2).getBytes( ))
                ) return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace( );
            return false;
        }
    }
}
