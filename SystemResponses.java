import com.jsoniter.output.JsonStream;

import java.util.Map;

import static java.util.Map.entry;

public class SystemResponses {
    public static class Errors {
        public static String MESSAGE_DAMAGED = JsonStream.serialize(Map.ofEntries(
            entry("type", "system"),
            entry("target", "message"),
            entry("status", "damaged")
        ));
        public static String PERMISSION_DENIED = JsonStream.serialize(Map.ofEntries(
            entry("type", "system"),
            entry("target", "message"),
            entry("status", "permission denied")
        ));
        public static String NOT_VALID_INTENTIONS = JsonStream.serialize(Map.ofEntries(
            entry("type", "system"),
            entry("target", "message"),
            entry("status", "permission denied")
        ));
    }
    public static class Confirmations {
        public static String CONNECTION_READY = JsonStream.serialize(Map.ofEntries(
            entry("type", "system"),
            entry("target", "connection"),
            entry("status", "connected")
        ));
    }
}
