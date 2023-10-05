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
            entry("target", "message"),
            entry("status", "permission denied")
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
