import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Objects;

public class Client {
    protected final WebSocket socket;
    public final long id;
    private final String token;
    public boolean status = false;
    private ArrayList<Long> channels;

    public Client (WebSocket sock, long id, String token) {
        this.socket = sock;
        this.id = id;
        this.token = token;
    }

    public boolean verifyToken(String token) {
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
