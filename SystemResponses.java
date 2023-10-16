import java.util.Map;

import static java.util.Map.entry;

public class SystemResponses {
    public static class Errors {
        public static Map<String, String> MESSAGE_DAMAGED = Map.ofEntries(
            entry("type", "system"),
            entry("target", "message"),
            entry("status", "damaged")
        );
        public static Map<String, String> PERMISSION_DENIED = Map.ofEntries(
            entry("type", "system"),
            entry("target", "system"),
            entry("status", "permission denied")
        );
        public static Map<String, String> NOT_VALID_INTENTIONS = Map.ofEntries(
            entry("type", "system"),
            entry("target", "message"),
            entry("status", "permission denied")
        );
        public static Map<String, String> SERVER_ERROR = Map.ofEntries(
            entry("type", "system"),
            entry("target", "message"),
            entry("status", "server error")
        );
        public static Map<String, String> NOT_AUTHORIZED = Map.ofEntries(
            entry("type", "system"),
            entry("target", "system"),
            entry("status", "not authorized")
        );
    }
    public static class Confirmations {
        public static Map<String, String> CONNECTION_READY = Map.ofEntries(
            entry("type", "system"),
            entry("target", "connection"),
            entry("status", "connected")
        );
    }
}
