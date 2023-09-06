import com.jsoniter.output.JsonStream;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.HashMap;

class Clients {
    private final HashMap<WebSocket, Client> clients = new HashMap<>();

    public void sendCommandToChannel (int channel, Message data) {
        String message = JsonStream.serialize(data);

        clients.values().parallelStream()
                .filter(c -> c.getChannels().contains(channel))
                .forEach(c -> c.send(message));
    }

    public void addClient (Client client) {

    }
}
