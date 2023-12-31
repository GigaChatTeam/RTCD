import com.jsoniter.JsonIterator;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.spi.JsonException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dbexecutors.PermissionOperator;
import exceptions.InvalidAuthorizationDataException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static dbexecutors.SystemExecutor.logInterruptedLogin;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.ofEpochSecond;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public class Authorizer {
    private final InetSocketAddress address;
    private final HttpServer authorizeServer;
    private final HashSet<ExpectedClient> expectedClients = new HashSet<>( );
    private final Thread tokensCleaningDaemon = new Thread(() -> {
        List<ExpectedClient> toClear;
        while (Starter.running) {
            long current = Date.from(ofEpochSecond(currentTimeMillis( ))).getTime( );

            synchronized (expectedClients) {
                toClear = expectedClients.stream( )
                        .filter(client -> client.clearing(current))
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
            String randomSecret = Helper.SHA512(randomUUID( ).toString( ));
            synchronized (expectedClients) {
                if (expectedClients.stream( )
                        .noneMatch(client ->
                                Objects.equals(client.token, randomSecret) &&
                                client.id == clientID)) {
                    return randomSecret;
                }
            }
        }
    }

    private String addClient (Long id, String secret, String key, String ipAddress, String agent) throws InvalidAuthorizationDataException {
        if (!PermissionOperator.validateToken(id, secret, key)) throw new InvalidAuthorizationDataException( );
        String token = generateRandomAccessToken(id);

        synchronized (expectedClients) {
            while (!addClient(new ExpectedClient(token, id, key, ipAddress, agent))) {
                token = generateRandomAccessToken(id);
            }
        }

        return token;
    }

    protected @Nullable ExpectedClient validateClient (Long id, String token) {
        synchronized (expectedClients) {
            ExpectedClient expectedClient = expectedClients.stream( )
                    .filter(client -> client.id == id && Objects.equals(client.token, token))
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

    void start () {
        authorizeServer.start( );
        tokensCleaningDaemon.start( );
        System.out.println(STR."Authorizer server started on port \{address.getPort( )}");
    }

    void stop () {
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

            try {
                if (!"POST".equals(exchange.getRequestMethod( ))) {
                    exchange.sendResponseHeaders(405, Responses.methodNotAllowed.length( ));
                    OutputStream os = exchange.getResponseBody( );
                    os.write(Responses.methodNotAllowed.getBytes( ));
                    os.close( );
                    exchange.close( );
                    return;
                }

                request = JsonIterator.deserialize(
                        new String(
                                exchange.getRequestBody( ).readAllBytes( ),
                                StandardCharsets.UTF_8),
                        AuthorizeRequest.class);
                headers = exchange.getRequestHeaders( );

                String response = format(
                        Responses.success, addClient(
                                request.client,
                                request.secret,
                                request.key,
                                exchange.getRemoteAddress( ).getHostName( ),
                                headers.get("User-Agent").getFirst( )));

                exchange.sendResponseHeaders(200, response.length( ));
                OutputStream os = exchange.getResponseBody( );
                os.write(response.getBytes( ));
                os.close( );
            } catch (JsonException e) {
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
                    logInterruptedLogin(
                            requireNonNull(request).client,
                            Helper.SHA512(request.key),
                            headers.get("User-Agent").getFirst( ));
                } catch (SQLException ex) {
                    ex.printStackTrace( );
                }
            } catch (IOException _) {
            } catch (Exception e) {
                e.printStackTrace( );
                exchange.sendResponseHeaders(500, Responses.internalServerError.length( ));
                OutputStream os = exchange.getResponseBody( );
                os.write(Responses.internalServerError.getBytes( ));
                os.close( );
            }

            exchange.close( );
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
