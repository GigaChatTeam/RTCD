import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Helper {
    @Contract("_ -> new")
    public static @NotNull ConnectionPath parseURI (@NotNull String uri) throws InvalidURIException {
        int index = uri.indexOf('?');
        if (index == -1) {
            throw new InvalidURIException( );
        }

        String path = uri.substring(index + 1);

        if (!(path.chars( ).filter(c -> c == '.').count( ) == 2 || path.chars( ).filter(c -> c == '%').count( ) == 1))
            throw new InvalidURIException( );

        String[] elements = path.split("\\.");
        String[] tokens = elements[2].split("%");

        try {
            return new ConnectionPath(elements[0], Long.parseLong(elements[1]), tokens[0], tokens[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidURIException( );
        }
    }

    static String SHA512 (String string) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            return string;
        }

        byte[] bytes = md.digest(string.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder( );

        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString( );
    }

    static final class Constants {
        static final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static class InvalidURIException extends Exception {

    }

    public static class ConnectionPath {
        String type;
        long client;
        String secret;
        String key;

        public ConnectionPath (@NotNull String type, long client, String secret, String key) {
            this.type = type.toUpperCase( );
            this.client = client;
            this.secret = secret;
            this.key = key;
        }

        public ConnectionPath (long client, String secret, String key) {
            this.type = "USER";
            this.client = client;
            this.secret = secret;
            this.key = key;
        }
    }

    public static class MessagePacket {
        final String hash;
        final String intention;
        final String preData;
        Object postData;

        public MessagePacket (@NotNull String packet) throws ParseException {
            String[] splitPacket = packet.split("%", 3);

            if (splitPacket.length != 3) throw new ParseException("Invalid packet", 0);

            this.intention = splitPacket[0];
            this.hash = splitPacket[1];
            this.preData = splitPacket[2];
        }

        public void parseData (Class<?> pattern) {
            postData = JsonIterator.deserialize(preData, pattern);
        }
    }

    public static class TTokenQueryWrapper {
        String intention;
        Any data;
    }
}
