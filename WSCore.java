import com.jsoniter.output.JsonStream;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;


class WSCore extends WebSocketServer {
    private final Clients clients = new Clients();
    private final int port;

    public WSCore (int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    private boolean clientIDVerifier (WebSocket webSocket, long id) {
        return id == clients.getID(webSocket) && id != -1;
    }

    @Override
    public void onOpen (WebSocket webSocket, @NotNull ClientHandshake clientHandshake) {
        System.out.println(clientHandshake.getResourceDescriptor()); // Debug

        Helper.ConnectionPath connectParams;

        try {
            connectParams = Helper.parseURI(clientHandshake.getResourceDescriptor());
        } catch (Helper.InvalidURIException e) {
            webSocket.close(406, "InsufficientData");
            return;
        }

        if (connectParams.params.get("id") != null && connectParams.params.get("token") != null) {
            if (PermissionOperator.validateToken(Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token"))) {
                clients.addClient(new Client(webSocket, Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token")));

                String response = JsonStream.serialize(SystemResponses.Confirmations.CONNECTION_READY);
                webSocket.send("CONNECTION%" + Helper.SHA512(response) + "%" + response);

                clients.changeStatus(webSocket, true);
            } else webSocket.close(401, "InvalidAuthorizationData");
        } else webSocket.close(406, "InsufficientData");
    }

    @Override
    public void onMessage (WebSocket webSocket, String message) {
        if (!clients.getStatus(webSocket)) {
            return;
        }

        if (Starter.DEBUG == 2) {
            System.out.println(message); // DEBUG
            clients.sendAll(message); // DEBUG
            return;
        }

        Helper.MessagePacket packet = Helper.parsePacket(message);
        Commands cmd = Commands.byIntents(packet.intention);

        try {
            packet.parseData(cmd.pattern);
        } catch (NullPointerException _) {
            webSocket.send(JsonStream.serialize(SystemResponses.Errors.NOT_VALID_INTENTIONS));
            return;
        }

        switch (cmd) {
            case ADMIN_CHANNELS_CREATE -> {
                break;
            }
            case USER_CHANNELS_MESSAGES_POST_NEW -> {
                if (!clientIDVerifier(webSocket, ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).author)) {
                    webSocket.send(JsonStream.serialize(SystemResponses.Errors.PERMISSION_DENIED));
                }
                break;
            }
            default -> webSocket.send(JsonStream.serialize(SystemResponses.Errors.MESSAGE_DAMAGED));
        }
    }

    @Override
    public void onClose (WebSocket webSocket, int status, String reason, boolean b) {
        clients.removeClient(webSocket);
    }

    @Override
    public void onError (WebSocket webSocket, Exception e) {
        clients.removeClient(webSocket);
    }

    @Override
    public void onStart () {
        System.out.printf("Server started on port %s\n", port);
    }
}