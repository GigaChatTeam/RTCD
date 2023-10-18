import com.jsoniter.output.JsonStream;

import java.util.Map;

import static java.util.Map.entry;

public class SystemResponses {
    public static class Errors {
        public static String MESSAGE_DAMAGED = JsonStream.serialize(Map.ofEntries(
            entry("target", "system"),
            entry("status", "damaged")
        ));
        public static String PERMISSION_DENIED = JsonStream.serialize(Map.ofEntries(
            entry("target", "command"),
            entry("status", "permission denied")
        ));
        public static String NOT_VALID_INTENTIONS = JsonStream.serialize(Map.ofEntries(
            entry("target", "command"),
            entry("status", "not valid intentions")
        ));
        public static String SERVER_ERROR = JsonStream.serialize(Map.ofEntries(
            entry("target", "system"),
            entry("status", "server error")
        ));
        public static String NOT_AUTHORIZED = JsonStream.serialize(Map.ofEntries(
            entry("target", "system"),
            entry("status", "not authorized")
        ));
        public static String NOT_VALID_ID = JsonStream.serialize(Map.ofEntries(
            entry("target", "system"),
            entry("status", "not valid id")
        ));
    }
    public static class Confirmations {
        public static String CONNECTION_READY = JsonStream.serialize(Map.ofEntries(
            entry("target", "connection"),
            entry("status", "connected")
        ));
    }
}
