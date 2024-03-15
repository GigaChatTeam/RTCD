import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.Date;
import java.util.Objects;

import static dbexecutors.Helper.SHA512;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochSecond;

public class ExpectedClient {
    final String token;
    final Long id;
    final Date start;

    final String hashKey;
    final InetAddress ipAddress;
    final String agent;

    volatile boolean open = true;

    ExpectedClient (
            @NotNull String token,
            @NotNull Long id,
            @NotNull String key,
            @NotNull InetAddress ipAddress,
            @Nullable String agent) {
        this.token = token;
        this.id = id;

        this.hashKey = SHA512(key);
        this.ipAddress = ipAddress;
        this.agent = agent;

        start = Date.from(ofEpochSecond(currentTimeMillis( )));
    }

    boolean validateForClean (@NotNull Date currentTime) {
        return ((currentTime.getTime() - start.getTime()) >= 20000000);
    }

    boolean validateForLogin (@NotNull String token, @NotNull Long id) {
        return Objects.equals(this.token, token) && Objects.equals(this.id, id);
    }

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof ExpectedClient) {
            return Objects.equals(((ExpectedClient) obj).id, id) && Objects.equals(((ExpectedClient) obj).hashKey, hashKey);
        }

        return false;
    }

    public void close ( ) {
        open = true;
    }
}
