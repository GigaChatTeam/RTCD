import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import static java.util.Map.entry;

public class SystemResponses {
    private static final ObjectMapper objectMapper = new ObjectMapper( );

    private enum Bodies {
        CONNECTION_READY(Map.ofEntries(
                entry("target", "connection"),
                entry("status", "ConnectionEstablished")
        )),
        MESSAGE_DAMAGED(Map.ofEntries(
                entry("target", "system"),
                entry("status", "MessageDamaged")
        )),
        PERMISSION_DENIED(Map.ofEntries(
                entry("target", "command"),
                entry("status", "PermissionDenied")
        )),
        NOT_VALID_INTENTIONS(Map.ofEntries(
                entry("target", "command"),
                entry("status", "NotValidIntentions")
        )),
        SERVER_ERROR(Map.ofEntries(
                entry("target", "system"),
                entry("status", "InternalServerError")
        )),
        NOT_AUTHORIZED(Map.ofEntries(
                entry("target", "system"),
                entry("status", "NotAuthorized")
        )),
        ALREADY_COMPLETED(Map.ofEntries(
                entry("target", "system"),
                entry("status", "AlreadyCompleted")
        )),
        NOT_VALID_ID(Map.ofEntries(
                entry("target", "system"),
                entry("status", "NotValidID")
        )),
        NOT_FOUND(Map.ofEntries(
                entry("target", "system"),
                entry("status", "NotFound")
        )),
        NOT_VALID_DATA(Map.ofEntries(
                entry("target", "system"),
                entry("status", "NotValidData")
        ));

        private final String data;

        Bodies (Map<String, String> data) {
            try {
                this.data = objectMapper.writeValueAsString(data);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class Errors {
        static class Users {
            static String ACCESS_DENIED (String hash) {
                return STR."89%\{hash}%\{Bodies.PERMISSION_DENIED.data}";
            }

            static String NOT_VALID_DATA (String hash) {
                return STR."210%\{hash}%\{Bodies.NOT_VALID_DATA}";
            }

            static String ALREADY_COMPLETED (String hash) {
                return STR."212%\{hash}\{hash}%\{Bodies.ALREADY_COMPLETED}";
            }

            public static String NOT_FOUND (String hash) {
                return STR."213%\{hash}%\{Bodies.NOT_FOUND}";
            }
        }

        static class Systems {
            static String SERVER_ERROR (String hash) {
                return STR."88%\{hash}%\{Bodies.SERVER_ERROR.data}";
            }

            static String MESSAGE_DAMAGED ( ) {
                return STR."SYSTEM-ERROR%MISS%\{Bodies.MESSAGE_DAMAGED.data}";
            }

            static String NOT_AUTHORIZED ( ) {
                return STR."SYSTEM-ERROR%MISS%\{Bodies.NOT_AUTHORIZED}";
            }

            static String NOT_VALID_INTENTIONS ( ) {
                return STR."SYSTEM-ERROR%MISS%\{Bodies.NOT_VALID_INTENTIONS}";
            }
        }
    }

    public static class Confirmations {
        public static String CONNECTION_READY (String hash) {
            return STR."SYSTEM-NOTIFICATION%\{hash}%\{Bodies.MESSAGE_DAMAGED.data}";
        }
    }
}
