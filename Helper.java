import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    static class Constants {
        static final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static class InvalidURIException extends Exception {    }

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

        public void parseData (Class<?> pattern) {
            postData = JsonIterator.deserialize(preData, pattern);
        }
    }

    public static class TTokenQueryWrapper {
        String[] intentions;
        Any data;
    }

    @Contract("_ -> new")
    public static @NotNull ConnectionPath parseURI (@NotNull String uri) throws InvalidURIException {
        int index = uri.indexOf('?');
        if (index == -1) {
            throw new InvalidURIException();
        }

        String path = uri.substring(0, index);
        String query = uri.substring(index + 1);

        String[] pathParts = path.split("/");

        Map<String, String> params = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            params.put(entry[0], entry[1]);
        }

        return new Helper.ConnectionPath(pathParts, params);
    }

    @Contract("_ -> new")
    public static @NotNull MessagePacket parsePacket (@NotNull String packet) {
        String[] splitPacket = packet.split("%");

        MessagePacket result = new MessagePacket(splitPacket[0].split("-"), splitPacket[1], splitPacket[2]);

        try {
            return result;
        } catch (Exception e) {
            System.out.println(e.toString());
            return null;
        }
    }
}
