import at.favre.lib.crypto.bcrypt.BCrypt;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    public static class ConnectionPath {
        String[] pathParts;
        Map<String, String> params;

        public ConnectionPath (String[] pathParts, Map<String, String> params) {
            this.pathParts = pathParts;
            this.params = params;
        }
    }

    public static class MessagePacket {
        String[] intention;
        String preData;
        Map<String, Any> postData;

        public MessagePacket (String[] intention, String data) {
            this.intention = intention;
            this.preData = data;
        }

        public void parseData () {
            postData = JsonIterator.deserialize(preData).asMap();
        }
    }

    @Contract("_ -> new")
    public static @NotNull ConnectionPath parseURI (@NotNull String uri) {
        int index = uri.indexOf('?');
        String path = uri.substring(0, index);
        String query = uri.substring(index + 1);

        String[] pathParts = path.split("/");

        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            params.put(entry[0], entry[1]);
        }

        return new ConnectionPath(pathParts, params);
    }

    @Contract("_ -> new")
    public static @NotNull MessagePacket parseMessagePacket (@NotNull String packet) {
        String[] parts = packet.split("%");

        String[] intent = new String[parts.length - 1];
        System.arraycopy(parts, 0, intent, 0, parts.length - 1);

        return new MessagePacket(intent, parts[parts.length - 1]);
    }

    public static Boolean verifierBCrypt (@NotNull String data, byte[] hash_data) {
        return BCrypt.verifyer().verify(Arrays.copyOfRange(data.toCharArray(), 0, Math.min(data.toCharArray().length, 72)), hash_data).verified;
    }

    public String SHA512 (String string) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            return string;
        }

        byte[] bytes = md.digest(string.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
