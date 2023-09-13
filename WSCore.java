import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;


class WSCore extends WebSocketServer {
    private final Clients clients = new Clients();

    public WSCore (int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen (WebSocket webSocket, ClientHandshake clientHandshake) {
        Helper.ConnectionPath connectParams = Helper.parseURI(clientHandshake.getResourceDescriptor());

        if (connectParams.params.get("id") != null && connectParams.params.get("token") != null) {
            if (PermissionOperator.validateToken(Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token"))) {
                clients.addClient(new Client(webSocket, Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token")));
            } else {
                webSocket.close(401, "InvalidAuthorizationData");
            }
        } else {
            webSocket.close(406, "InsufficientData");
        }
    }

    @Override
    public void onMessage (WebSocket webSocket, String s) {
        Map<String, Any> message_preParser = JsonIterator.deserialize(s).asMap();

        System.out.print(clients.getId(webSocket)); System.out.println(message_preParser);

        switch (message_preParser.get("type").toString()) {
            case ("MESSAGE-POST") -> {
                DataCommands.MessagePost task = JsonIterator.deserialize(s, DataCommands.MessagePost.class);
                if (task == null) {
                    return;
                }

                clients.sendCommandToChannel(task.channel, JsonStream.serialize(task));
            }
            case ("CHANNEL-CONTROL-CREATE") -> {
                DataCommands.ChannelCreate task = JsonIterator.deserialize(s, DataCommands.ChannelCreate.class);
                if (task == null) {
                    return;
                }

                Integer channel_id = DataOperator.createChannel(task.title, task.id);
                if (channel_id == null) {
                    return;
                }

                DataCommands.ChannelCreate response = new DataCommands.ChannelCreate(channel_id);

                clients.joinClientToChannel(webSocket, channel_id);
                clients.sendCommandToChannel(channel_id, JsonStream.serialize(response));
            }
            case ("CHANNEL-USERCONTROL-PRESENCE") -> {
                DataCommands.ChannelUserControlAccess task = JsonIterator.deserialize(s, DataCommands.ChannelUserControlAccess.class);
                if (task == null) {
                    return;
                }

                if (task.access) {
                    DataOperator.addUserToChannel(task.id, task.user);
                    clients.sendCommandToChannel(task.id, JsonStream.serialize(message_preParser));
                } else {
                    clients.sendCommandToChannel(task.id, JsonStream.serialize(message_preParser));
                    DataOperator.removeUserFromChannel(task.id, task.user);
                }
            }
            case ("SYSTEM-CHANNELS-LISTEN") -> {
                SystemCommands.ListenChannel task = JsonIterator.deserialize(s, SystemCommands.ListenChannel.class);
                if (task == null) {
                    return;
                }

                if (task.status) {
                    clients.joinClientToChannel(webSocket, task.channel);
                } else {
                    clients.LeaveClientFromChannel(webSocket, task.channel);
                }
            }
        }
    }

    @Override
    public void onClose (WebSocket webSocket, int i, String reason, boolean b) {
        clients.removeClient(webSocket);
    }

    @Override
    public void onError (WebSocket webSocket, Exception e) {
        clients.removeClient(webSocket);
    }

    @Override
    public void onStart () {
        System.out.println("Server started on port 8080");
    }
}