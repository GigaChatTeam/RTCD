import java.util.Date;

import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochSecond;

public class ExpectedClient {
    final String token;
    final long id;
    final long start;

    final String key;
    final String ipAddress;
    final String agent;

    ExpectedClient (String token, long id, String key, String ipAddress, String agent) {
        this.token = token;
        this.id = id;

        this.key = key;
        this.ipAddress = ipAddress;
        this.agent = agent;

        start = Date.from(ofEpochSecond(currentTimeMillis( ))).getTime( );
    }

    boolean clearing (long currentTimeMillis) {
        return ((currentTimeMillis - start) >= 20000000);
    }
}
