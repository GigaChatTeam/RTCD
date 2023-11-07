import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.util.UUID.randomUUID;

public class SystemExecutor extends DBOperator {
    static String generateTToken (long client, String[] intentions) throws SQLException {
        String ttoken = Helper.SHA512(randomUUID().toString());

        String sql = """
                    INSERT INTO public.ttokens (client, token, extradition, intentions)
                    VALUES
                        (?, ?, now(), ?)
                """;

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, client);
        stmt.setString(2, ttoken);
        stmt.setArray(3, conn.createArrayOf("text", intentions));

        stmt.execute();

        return ttoken;
    }
}
