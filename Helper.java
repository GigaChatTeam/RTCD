import at.favre.lib.crypto.bcrypt.BCrypt;
import com.jsoniter.JsonIterator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        final String hash;
        final String[] intention;
        final String preData;
        Object postData;

        public MessagePacket (String[] intention, String controlSum, String data) {
            this.intention = intention;
            this.hash = controlSum;
            this.preData = data;
        }

        public void parseData (Class pattern) {
            postData = JsonIterator.deserialize(preData, pattern);
        }
    }

    @Contract("_ -> new")
    public static @Nullable ConnectionPath parseURI (@NotNull String uri) {
        int index = uri.indexOf('?');
        if (index == -1) {
            return null;
        }

        String path = uri.substring(0, index);
        String query = uri.substring(index + 1);

        String[] pathParts = path.split("/");

        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            System.out.println(Arrays.toString(entry));
            params.put(entry[0], entry[1]);
        }

        return new Helper.ConnectionPath(pathParts, params);
    }

    @Contract("_ -> new")
    public static @NotNull MessagePacket parsePacket (@NotNull String packet) {
        String[] splitPacket = packet.split("%");

        return new MessagePacket(splitPacket[0].split("-"), splitPacket[1], splitPacket[2]);
    }

    public static Boolean verifierBCrypt (@NotNull String data, byte[] hash_data) {
        return BCrypt.verifyer().verify(Arrays.copyOfRange(data.toCharArray(), 0, Math.min(data.toCharArray().length, 72)), hash_data).verified;
    }

    public static String SHA512 (String string) {
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
