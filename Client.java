import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Objects;

public class Client {
    public final long id;
    protected final WebSocket socket;
    private final String token;
    private final ArrayList<Long> channels = new ArrayList<>();
    public boolean status = false;

    public Client (WebSocket sock, long id, String token) {
        this.socket = sock;
        this.id = id;
        this.token = token;
    }

    public boolean verifyToken (String token) {
        return Objects.equals(token, this.token);
    }

    public void addListenChannel (long channel) {
        if (!channels.contains(channel)) {
            channels.add(channel);
        }
    }

    public void removeListenChannel (long channel) {
        channels.remove(channel);
    }

    public void send (String data) {
        socket.send(data);
    }

    public void close (int code, String reason) {
        socket.close(code, reason);
    }

    public ArrayList<Long> getChannels () {
        return channels;
    }
}
