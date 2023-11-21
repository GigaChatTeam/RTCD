import com.jsoniter.spi.JsonException;
import dbexecutors.ChannelsExecutor;
import dbexecutors.PermissionOperator;
import dbexecutors.SystemExecutor;
import exceptions.AccessDenied;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.text.ParseException;


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
        if (Starter.DEBUG >= 3) System.out.println(clientHandshake.getResourceDescriptor());

        Helper.ConnectionPath connectParams;

        try {
            connectParams = Helper.parseURI(clientHandshake.getResourceDescriptor());
        } catch (Helper.InvalidURIException e) {
            webSocket.close(1400, "InsufficientData");
            return;
        }

        if (connectParams.params.get("id") != null && connectParams.params.get("token") != null) {
            if (PermissionOperator.validateToken(Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token"))) {
                clients.addClient(new Client(webSocket, Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token")));

                webSocket.send(new ResponsesPatterns.System.ConnectionParameters.ConnectionControl(true).serialize("SYSTEM"));

                clients.changeClientConnectionStatus(webSocket, true);
            } else webSocket.close(1401, "InvalidAuthorizationData");
        } else webSocket.close(1406, "InsufficientData");
    }

    @Override
    public void onMessage (WebSocket webSocket, String message) {
        if (!clients.getClientConnectionStatus(webSocket)) {
            webSocket.send(SystemResponses.Errors.Systems.NOT_AUTHORIZED());
            return;
        }

        if (Starter.DEBUG >= 3) System.out.println(message);

        Helper.MessagePacket packet;
        Commands cmd;

        try {
            packet = new Helper.MessagePacket(message);
        } catch (ParseException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace();
            webSocket.send(new ResponsesPatterns.System.ClientErrors.DataErrors.NotValidData().serialize("MISS"));
            return;
        }

        cmd = Commands.byIntents(packet.intention);
        if (cmd == null) {
            webSocket.send(new ResponsesPatterns.System.ServerErrors.OutdatedServer().serialize(packet.hash));
            return;
        }

        try {
            packet.parseData(cmd.pattern);
        } catch (JsonException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace();
            webSocket.send(new ResponsesPatterns.System.ClientErrors.DataErrors.NotValidData().serialize(packet.hash));
            return;
        }

        try {
            switch (cmd) {
//                case CHANNELS_USERS_MESSAGES_POST_NEW -> {
//                    if (!clients.isUserInChannel(webSocket, ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel))
//                        throw new AccessDenied();
//
//                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
//                            new ResponsesPatterns.Channels.Messages.Post.New((CommandsPatterns.Channels.Messages.Post.New) packet.postData,
//                                    ChannelsExecutor.Messages.postMessage(
//                                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).author,
//                                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
//                                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).text)).serialize(packet.hash));
//                }
                case CHANNELS_SYSTEM_LISTENING_ADD -> {
                    if (!ChannelsExecutor.Users.Permissions.isClientOnChannel(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Notification.AddListening) packet.postData).channel)
                    ) throw new AccessDenied();

                    clients.addListeningClientToChannel(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Notification.AddListening) packet.postData).channel);

                    webSocket.send(
                            new ResponsesPatterns.Channels.System.Notification.AddListening(
                                    ((CommandsPatterns.Channels.System.Notification.AddListening) packet.postData).channel).serialize(packet.hash));
                }
                case CHANNELS_SYSTEM_LISTENING_REMOVE -> {
                    clients.removeListeningClientFromChannel(
                            clients.getID(webSocket),
                            ((CommandsPatterns.Channels.System.Notification.RemoveListening) packet.postData).channel);

                    webSocket.send(
                            new ResponsesPatterns.Channels.System.Notification.RemoveListening(
                                    ((CommandsPatterns.Channels.System.Notification.RemoveListening) packet.postData).channel).serialize(packet.hash));
                }
                case CHANNELS_SYSTEM_CREATE -> webSocket.send(new ResponsesPatterns.Channels.System.Control.Create(
                        ChannelsExecutor.create(
                                clients.getID(webSocket),
                                ((CommandsPatterns.Channels.System.Control.Create) packet.postData).title),
                        ((CommandsPatterns.Channels.System.Control.Create) packet.postData).title).serialize(packet.hash));
//                case ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_TITLE -> {
//                    ChannelsExecutor.Settings.External.changeTitle(
//                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).client,
//                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).channel,
//                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).newTitle);
//
//                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).channel,
//                            new ResponsesPatterns.Channels.Settings.External.Change.Title((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).serialize(packet.hash));
//                }
//                case ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_DESCRIPTION -> {
//                    ChannelsExecutor.Settings.External.changeTitle(
//                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).client,
//                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).channel,
//                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).newDescription);
//
//                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).channel,
//                            new ResponsesPatterns.Channels.Settings.External.Change.Description((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).serialize(packet.hash));
//                }
                case SYSTEM_TTOKENS_GENERATE -> {
                    Helper.TTokenQueryWrapper wrapper = (Helper.TTokenQueryWrapper) packet.postData;
                    CommandsPatterns.System.TTokens.Generate ttokenQuery = CommandsPatterns.System.TTokens.Generate.byIntents(wrapper.intention);
                    Object queryData = wrapper.data.as(ttokenQuery.pattern);

                    switch (ttokenQuery) {
                        case HLB_CHANNELS_USERS_DOWNLOAD ->
                                webSocket.send(new ResponsesPatterns.System.TTokens.Generate(
                                        SystemExecutor.Channels.History.loadChannels(clients.getID(webSocket))).serialize(packet.hash));
                        case HLB_CHANNELS_USERS_DOWNLOAD_MESSAGES -> {
                            if (!ChannelsExecutor.Users.Permissions.isClientOnChannel(
                                    clients.getID(webSocket),
                                    ((CommandsPatterns.System.TTokens.Patterns.HLB.Channels.Users.Messages) packet.postData).channel)
                            ) throw new AccessDenied();

                            webSocket.send(new ResponsesPatterns.System.TTokens.Generate(SystemExecutor.Channels.History.loadMessagesHistory(
                                    clients.getID(webSocket),
                                    ((CommandsPatterns.System.TTokens.Patterns.HLB.Channels.Users.Messages) queryData).channel)).serialize(packet.hash));
                        }
                        case HLB_CHANNELS_USERS_DOWNLOAD_PERMISSIONS -> {
                            if (!ChannelsExecutor.Users.Permissions.isClientOnChannel(
                                    clients.getID(webSocket),
                                    ((CommandsPatterns.System.TTokens.Patterns.HLB.Channels.Users.Messages) packet.postData).channel)
                            ) throw new AccessDenied();

                            webSocket.send(new ResponsesPatterns.System.TTokens.Generate(SystemExecutor.Channels.History.loadMessagesHistory(
                                    clients.getID(webSocket),
                                    ((CommandsPatterns.System.TTokens.Patterns.HLB.Channels.Users.Permissions) queryData).channel)).serialize(packet.hash));
                        }
                    }
                }
                default -> throw new ParseException("OUTDATED SERVER", 1);
            }
        } catch (SQLException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace();
            webSocket.send(new ResponsesPatterns.System.ServerErrors.InternalError().serialize(packet.hash));
        } catch (ParseException e) {
            if (Starter.DEBUG >= 2) e.printStackTrace();
            webSocket.send(new ResponsesPatterns.System.ClientErrors.DataErrors.NotValidData().serialize(packet.hash));
        } catch (AccessDenied e) {
            if (Starter.DEBUG >= 2) e.printStackTrace();
            webSocket.send(new ResponsesPatterns.System.ClientErrors.AccessErrors.AccessDenied().serialize(packet.hash));
        } /* catch (NotFound e) {
            if (Starter.DEBUG >= 2) e.printStackTrace();
            webSocket.send(new ResponsesPatterns.System.ClientErrors.AccessErrors.NotFound().serialize(packet.hash));
        } */
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