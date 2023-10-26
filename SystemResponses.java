import com.jsoniter.output.JsonStream;

import java.util.Map;

import static java.util.Map.entry;

public class SystemResponses {
    static class Errors {
        private static class Bodies {
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
            public static String NOT_FOUND = JsonStream.serialize(Map.ofEntries(
                    entry("target", "system"),
                    entry("status", "not found")
            ));
            public static String NOT_VALID_DATA = JsonStream.serialize(Map.ofEntries(
                    entry("target", "system"),
                    entry("status", "not valid data")
            ));
        }

        static class Users {
            static String MESSAGE_DAMAGED (String hash) {
                return STR. "SYSTEM-ERROR%\{ hash }%\{ Bodies.MESSAGE_DAMAGED }" ;
            }

            static String PERMISSION_DENIED (String hash) {
                return STR. "SYSTEM-ERROR%\{ hash }%\{ Bodies.PERMISSION_DENIED }" ;
            }

            static String SERVER_ERROR (String hash) {
                return STR. "SYSTEM-ERROR%\{ hash }%\{ Bodies.SERVER_ERROR }" ;
            }

            static String NOT_AUTHORIZED (String hash) {
                return STR. "SYSTEM-ERROR%\{ hash }%\{ Bodies.NOT_AUTHORIZED }" ;
            }

            public static String NOT_FOUND (String hash) {
                return STR. "SYSTEM-ERROR%\{ hash }%\{ Bodies.NOT_FOUND }" ;
            }

            static String NOT_VALID_DATA (String hash) {
                return STR. "SYSTEM-ERROR%\{ hash }%\{ Bodies.NOT_VALID_DATA }" ;
            }
        }

        static class Systems {
            static String NOT_AUTHORIZED () {
                return STR. "SYSTEM-ERROR%MISS%\{ Bodies.NOT_AUTHORIZED }" ;
            }

            static String NOT_VALID_INTENTIONS () {
                return STR. "SYSTEM-ERROR%MISS%\{ Bodies.NOT_VALID_INTENTIONS }" ;
            }

            static String NOT_VALID_ID () {
                return STR. "SYSTEM-ERROR%MISS%\{ Bodies.NOT_VALID_ID }" ;
            }
        }
    }

    public static class Confirmations {
        public static String CONNECTION_READY = JsonStream.serialize(Map.ofEntries(
                entry("target", "connection"),
                entry("status", "connected")
        ));
    }
}
