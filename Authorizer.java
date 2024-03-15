import com.clickhouse.client.ClickHouseException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dbexecutors.Users;
import dbexecutors.sql.PermissionOperator;
import exceptions.InvalidAuthorizationDataException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static dbexecutors.Helper.SHA512;
import static dbexecutors.sql.PoolController.getConnection;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochSecond;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

class Authorizer {
    private static final ObjectMapper objectMapper = new ObjectMapper( );

    private final InetSocketAddress address;
    private final HttpServer authorizeServer;
    private final HashSet<ExpectedClient> expectedClients = new HashSet<>( );
    private final Thread tokensCleaningDaemon = new Thread(( ) -> {
        List<ExpectedClient> toClear;

        while (Starter.running) {
            Date current = Date.from(ofEpochSecond(currentTimeMillis( )));

            synchronized (expectedClients) {
                toClear = expectedClients.stream( )
                        .filter(client -> client.validateForClean(current))
                        .peek(ExpectedClient::close)
                        .toList( );
            }

            if (!toClear.isEmpty( )) {
                synchronized (expectedClients) {
                    toClear.forEach(expectedClients::remove);
                }
            }
        }
    });

    Authorizer (InetSocketAddress address, int backlog) throws IOException {
        authorizeServer = HttpServer.create(address, backlog);
        authorizeServer.createContext("/", new AuthorizeHandler( ));
        authorizeServer.setExecutor(null);
        this.address = address;
    }

    private String generateRandomAccessToken (long clientID) {
        while (true) {
            String randomSecret = SHA512(randomUUID( ).toString( ));

            synchronized (expectedClients) {
                if (expectedClients.stream( )
                        .noneMatch(
                                client ->
                                        Objects.equals(client.token, randomSecret) &&
                                        client.id == clientID)) {
                    return randomSecret;
                }
            }
        }
    }

    private String addClient (@NotNull Connection connection, Long id, String secret, String key, InetAddress ipAddress, String agent) throws InvalidAuthorizationDataException, SQLException {
        if (!PermissionOperator.validateToken(connection, id, secret, key))
            throw new InvalidAuthorizationDataException( );


        String token;

        do {
            token = generateRandomAccessToken(id);
        } while (!addClient(new ExpectedClient(token, id, key, ipAddress, agent)));


        return token;
    }

    protected @Nullable ExpectedClient validateClient (Long id, String token) {
//        return expectedClients.stream( )
//                .filter(client -> Objects.equals(client.id, id) && Objects.equals(client.token, token))
//                .findAny( )
//                .orElse(null);

        synchronized (expectedClients) {
            ExpectedClient expectedClient = expectedClients.stream( )
                    .filter(client -> Objects.equals(client.id, id) && Objects.equals(client.token, token))
                    .findFirst( )
                    .orElse(null);

            if (expectedClient == null) return null;
            else {
                expectedClients.remove(expectedClient);
                return expectedClient;
            }
        }
    }

    private boolean addClient (ExpectedClient client) {
        synchronized (expectedClients) {
            return expectedClients.add(client);
        }
    }

    void start ( ) {
        authorizeServer.start( );
        tokensCleaningDaemon.start( );
        System.out.println(STR."Authorizer server started on port \{address.getPort( )}");
    }

    void stop ( ) {
        authorizeServer.stop(0);
    }

    private static class AuthorizeRequest {
        @JsonProperty(required = true)
        Long client;
        @JsonProperty(required = true)
        String secret;
        @JsonProperty(required = true)
        String key;
    }

    class AuthorizeHandler implements HttpHandler {
        @Override
        public void handle (@NotNull HttpExchange exchange) throws IOException {
            AuthorizeRequest request = null;
            Headers headers = null;
            Connection connection = null;

            try {
                connection = getConnection();

                if (!"POST".equals(exchange.getRequestMethod( ))) {
                    exchange.sendResponseHeaders(405, Responses.methodNotAllowed.length( ));
                    OutputStream os = exchange.getResponseBody( );
                    os.write(Responses.methodNotAllowed.getBytes( ));
                    os.close( );
                    exchange.close( );
                    return;
                }

                request = objectMapper.readValue(
                        new String(
                                exchange.getRequestBody( ).readAllBytes( ),
                                StandardCharsets.UTF_8),
                        AuthorizeRequest.class);
                headers = exchange.getRequestHeaders( );

                String response = format(
                        Responses.success, addClient(
                                connection,
                                request.client,
                                request.secret,
                                request.key,
                                exchange.getRemoteAddress( ).getAddress( ),
                                headers.get("User-Agent").getFirst( )));

                exchange.sendResponseHeaders(200, response.length( ));
                OutputStream os = exchange.getResponseBody( );
                os.write(response.getBytes( ));
                os.close( );
            } catch (JsonProcessingException e) {
                exchange.sendResponseHeaders(400, Responses.lackOfData.length( ));
                OutputStream os = exchange.getResponseBody( );
                os.write(Responses.lackOfData.getBytes( ));
                os.close( );
            } catch (InvalidAuthorizationDataException e) {
                exchange.sendResponseHeaders(403, Responses.invalidAuthorizationData.length( ));
                OutputStream os = exchange.getResponseBody( );
                os.write(Responses.invalidAuthorizationData.getBytes( ));
                os.close( );

                try {
                    Users.Login.login(
                            requireNonNull(request).client,
                            Timestamp.from(Instant.ofEpochMilli(currentTimeMillis( ))),
                            false,
                            false,
                            headers.get("User-Agent").getFirst( ),
                            exchange.getRemoteAddress( ));
                } catch (ClickHouseException ex) {
                    ex.printStackTrace( );
                }
            } catch (IOException e) {
                e.printStackTrace( );
            } catch (SQLException e) {
                if (connection != null) {
                    try {
                        connection.rollback( );
                    } catch (SQLException ex) {
                        ex.printStackTrace( );
                    }
                }

                exchange.sendResponseHeaders(500, Responses.internalServerError.length( ));
                OutputStream os = exchange.getResponseBody( );
                os.write(Responses.internalServerError.getBytes( ));
                os.close( );
            } finally {
                if (connection != null) {
                    try {
                        connection.commit( );
                    } catch (SQLException e) {
                        e.printStackTrace( );
                    }
                }
                exchange.close( );
            }
        }

        static class Responses {
            static final String success = "{\"status\":\"Done\",\"token\":\"%s\"}"; // 200 HTTP status code

            static final String lackOfData = "{\"status\":\"Refused\",\"reason\":\"BadRequest\",\"description\":\"LackOfData\"}"; // 400 HTTP status code
            static final String invalidAuthorizationData = "{\"status\":\"Refused\",\"reason\":\"BadRequest\",\"description\":\"InvalidAuthorizationData\"}"; // 403 HTTP status code
            static final String methodNotAllowed = "{\"status\":\"Refused\",\"reason\":\"BadRequest\",\"description\":\"MethodNotAllowed\"}"; // 405 HTTP status code

            static final String internalServerError = "{\"status\":\"Refused\",\"reason\":\"ServerError\",\"description\":\"InternalServerError\"}"; // 500 HTTP status code
        }
    }
}
