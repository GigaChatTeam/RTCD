import DataThreads.Channel;
import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Objects;

public class Client {
    public final long id;
    protected final WebSocket socket;
    private final String token;
    protected final HashSet<Channel> channels = new HashSet<>();
    public boolean status = false;

    public Client (WebSocket sock, long id, String token) {
        this.socket = sock;
        this.id = id;
        this.token = token;
    }

    public boolean verifyToken (String token) {
        return Objects.equals(token, this.token);
    }

    public void addListenChannel (long channel, boolean canPost) {
        channels.add(new Channel(
                channel, canPost
        ));
    }

    public void addListenChannel (long channel) {
        channels.add(new Channel(
                channel, false
        ));
    }

    public void removeListenChannel (long channel) {
        channels.remove(
                channels.stream()
                        .filter(c -> c.id == channel)
                        .findFirst()
                        .orElse(null)
        );
    }

    protected void send (String data) {
        socket.send(data);
    }

    protected void close (int code, String reason) {
        socket.close(code, reason);
    }
}
