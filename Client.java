import org.java_websocket.WebSocket;

import java.util.ArrayList;

public class Client {
    protected final WebSocket socket;
    private final int id;
    private final String token;
    private ArrayList<Integer> channels;

    public Client (WebSocket sock, int id, String token) {
        this.socket = sock;
        this.id = id;
        this.token = token;
    }

    public void addListen (int channel) {
        this.channels.add(channel);
    }
    public void removeListen (int channel) {
        this.channels.remove(channel);
    }

    public void send (String data) {
        socket.send(data);
    }
    public void close (int code, String reason) {
        socket.close(code, reason);
    }

    public ArrayList<Integer> getChannels () {
        return channels;
    }
}
