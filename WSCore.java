import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.JsonException;
import dbexecutors.ChannelsExecutor;
import dbexecutors.PermissionOperator;
import dbexecutors.SystemExecutor;
import exceptions.AccessDenied;
import exceptions.NotFound;
import exceptions.NotValid;
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
            webSocket.close(406, "InsufficientData");
            return;
        }

        if (connectParams.params.get("id") != null && connectParams.params.get("token") != null) {
            if (PermissionOperator.validateToken(Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token"))) {
                clients.addClient(new Client(webSocket, Integer.parseInt(connectParams.params.get("id")), connectParams.params.get("token")));

                webSocket.send(STR. "CONNECTION%\{ JsonStream.serialize(SystemResponses.Confirmations.CONNECTION_READY) }" );

                clients.changeClientConnectionStatus(webSocket, true);
            } else webSocket.close(401, "InvalidAuthorizationData");
        } else webSocket.close(406, "InsufficientData");
    }

    @Override
    public void onMessage (WebSocket webSocket, String message) {
        if (!clients.getClientConnectionStatus(webSocket)) {
            webSocket.send(SystemResponses.Errors.Systems.NOT_AUTHORIZED());
            return;
        }

        if (Starter.DEBUG >= 3) System.out.println(message);

        Helper.MessagePacket packet = Helper.parsePacket(message);
        Commands cmd = Commands.byIntents(packet.intention);

        try {
            packet.parseData(cmd.pattern);
        } catch (JsonException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace();
            webSocket.send(SystemResponses.Errors.Systems.NOT_VALID_INTENTIONS());
            return;
        }

//        if (Starter.DEBUG >= 3) {
//            System.out.println(Arrays.toString(packet.intention));
//            System.out.println(packet.hash);
//            System.out.println(JsonStream.serialize(JsonStream.serialize(packet.postData)));
//        }

        try {
            switch (cmd) {
                case USER_CHANNELS_MESSAGES_POST_NEW -> {
                    if (!clients.isUserInChannel(webSocket, ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel)) {
                        webSocket.send(SystemResponses.Errors.Users.PERMISSION_DENIED(packet.hash));
                        return;
                    }

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
                            new ResponsesPatterns.Channels.Messages.Post.New((CommandsPatterns.Channels.Messages.Post.New) packet.postData,
                                    ChannelsExecutor.Messages.postMessage(
                                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).author,
                                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).channel,
                                            ((CommandsPatterns.Channels.Messages.Post.New) packet.postData).text)).serialize(packet.hash));
                }
                case SYSTEM_CHANNELS_LISTEN_ADD -> {
                    if (!ChannelsExecutor.Users.Permissions.isClientOnChannel(
                            ((CommandsPatterns.Systems.Listen.Channel.Add) packet.postData).client,
                            ((CommandsPatterns.Systems.Listen.Channel.Add) packet.postData).channel)
                    ) throw new AccessDenied();

                    clients.addListeningClientToChannel(
                            ((CommandsPatterns.Systems.Listen.Channel.Add) packet.postData).client,
                            ((CommandsPatterns.Systems.Listen.Channel.Add) packet.postData).channel);
                }
                case SYSTEM_CHANNELS_LISTEN_REMOVE -> clients.removeListeningClientFromChannel(
                        ((CommandsPatterns.Systems.Listen.Channel.Remove) packet.postData).client,
                        ((CommandsPatterns.Systems.Listen.Channel.Remove) packet.postData).channel);
                case USER_CHANNELS_MESSAGES_EDIT -> {
                    if (!clients.isUserInChannel(webSocket, ((CommandsPatterns.Channels.Messages.Edit) packet.postData).channel)) {
                        webSocket.send(SystemResponses.Errors.Users.PERMISSION_DENIED(packet.hash));
                        return;
                    }

                    ChannelsExecutor.Messages.editMessage(
                            ((CommandsPatterns.Channels.Messages.Edit) packet.postData).channel,
                            ((CommandsPatterns.Channels.Messages.Edit) packet.postData).posted,
                            ((CommandsPatterns.Channels.Messages.Edit) packet.postData).text);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Messages.Edit) packet.postData).channel,
                            new ResponsesPatterns.Channels.Messages.Edit((CommandsPatterns.Channels.Messages.Edit) packet.postData).serialize(packet.hash));
                }
                case ADMIN_CHANNELS_CREATE -> webSocket.send(new ResponsesPatterns.Channels.Create(
                        (CommandsPatterns.Channels.Create) packet.postData,
                        ChannelsExecutor.create(
                                ((CommandsPatterns.Channels.Create) packet.postData).owner,
                                ((CommandsPatterns.Channels.Create) packet.postData).title)).serialize(packet.hash));
                case ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_TITLE -> {
                    ChannelsExecutor.Settings.External.changeTitle(
                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).client,
                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).channel,
                            ((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).newTitle);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).channel,
                            new ResponsesPatterns.Channels.Settings.External.Change.Title((CommandsPatterns.Channels.Settings.External.Change.Title) packet.postData).serialize(packet.hash));
                }
                case ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_DESCRIPTION -> {
                    ChannelsExecutor.Settings.External.changeTitle(
                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).client,
                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).channel,
                            ((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).newDescription);

                    clients.sendCommandToChannel(((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).channel,
                            new ResponsesPatterns.Channels.Settings.External.Change.Description((CommandsPatterns.Channels.Settings.External.Change.Description) packet.postData).serialize(packet.hash));
                }
                case SYSTEM_TTOKENS_GENERATE -> {
                    Helper.TTokenQueryWrapper wrapper = (Helper.TTokenQueryWrapper) packet.postData;
                    CommandsPatterns.Systems.TTokens.Generate ttokenQuery = CommandsPatterns.Systems.TTokens.Generate.byIntents(wrapper.intentions);
                    Object queryData = wrapper.data.as(ttokenQuery.pattern);

                    switch (ttokenQuery) {
                        case USERS_DOWNLOAD_CHANNELS ->
                                webSocket.send(new ResponsesPatterns.System.TTokens.Generate(SystemExecutor.Channels.History.loadChannels(
                                        clients.getID(webSocket))).serialize(packet.hash));
                        case USERS_DOWNLOAD_CHANNELS_MESSAGES_HISTORY ->
                                webSocket.send(new ResponsesPatterns.System.TTokens.Generate(SystemExecutor.Channels.History.loadMessagesHistory(
                                        clients.getID(webSocket),
                                        ((CommandsPatterns.Systems.TTokens.TTokensPatterns.Users.Download.Channels.Messages.History) queryData).channel)).serialize(packet.hash));
                        case USERS_DOWNLOAD_CHANNELS_PERMISSIONS ->
                                webSocket.send(new ResponsesPatterns.System.TTokens.Generate(SystemExecutor.Channels.History.loadPermissions(
                                        clients.getID(webSocket),
                                        ((CommandsPatterns.Systems.TTokens.TTokensPatterns.Users.Download.Channels.Permissions) queryData).channel)).serialize(packet.hash));
                    }
                }
                default -> throw new ParseException("SERVER ERROR", 1);
            }
        } catch (SQLException e) {
            if (Starter.DEBUG >= 1) e.printStackTrace();
            webSocket.send(SystemResponses.Errors.Users.SERVER_ERROR(packet.hash));
        } catch (AccessDenied e) {
            if (Starter.DEBUG >= 2) e.printStackTrace();
            webSocket.send(SystemResponses.Errors.Users.PERMISSION_DENIED(packet.hash));
        } catch (ParseException e) {
            if (Starter.DEBUG >= 2) e.printStackTrace();
            webSocket.send(SystemResponses.Errors.Users.MESSAGE_DAMAGED(packet.hash));
        } catch (NotFound e) {
            if (Starter.DEBUG >= 2) e.printStackTrace();
            webSocket.send(SystemResponses.Errors.Users.NOT_FOUND(packet.hash));
        } catch (NotValid e) {
            if (Starter.DEBUG >= 2) e.printStackTrace();
            webSocket.send(SystemResponses.Errors.Users.NOT_VALID_DATA(packet.hash));
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