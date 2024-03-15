import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Timestamp;
import java.util.UUID;

public class CommandsPatterns {
    public static class System {
        public static class ConnectionParameters {
            static class ConnectionControl {
                @JsonProperty(required = true)
                Boolean status;
            }
        }
    }

    public static class Channels {
        public static class System {
            public static class Control {
                public static class Create {
                    @JsonProperty(required = true)
                    String title;
                    @JsonProperty
                    String description = "";
                    @JsonProperty
                    Boolean isPublic = false;
                }

                public static class Delete {
                    @JsonProperty(required = true)
                    Long id;
                    @JsonProperty
                    String reason;
                }
            }

            public static class Notification {
                static class Listening {
                    static class Add {
                        @JsonProperty(required = true)
                        Long channel;
                    }

                    static class Remove {
                        @JsonProperty(required = true)
                        Long channel;
                    }
                }
            }

            public static class Invitations {
                public static class Create {
                    @JsonProperty(required = true)
                    Long channel;

                    @JsonProperty(value = "max-uses")
                    Integer permittedUses;
                    @JsonSerialize(using = JsonSerializers.TimestampSerializer.class)
                    @JsonDeserialize(using = JsonSerializers.TimestampDeserializer.class)
                    Timestamp expiration;
                }

                public static class Delete {
                    @JsonProperty(required = true)
                    String uri;
                }
            }
        }

        public static class Administration {
            public static class Permissions {
                public static class Groups {
                    public static class Create {
                        @JsonProperty(required = true)
                        Long channel;
                        Byte position;
                        @JsonProperty(required = true)
                        String title;
                    }

                    public static class Edit {
                        @JsonProperty(required = true)
                        Long channel;
                        @JsonProperty(required = true)
                        Byte group;
                        @JsonProperty(required = true)
                        String key;
                        @JsonProperty(required = true)
                        String value;
                    }

                    public static class Delete {
                        @JsonProperty(required = true)
                        Long channel;
                        @JsonProperty(required = true)
                        Byte group;
                    }
                }

                public static class Users {
                    public static class Groups {
                        @JsonProperty(required = true)
                        Long channel;
                        @JsonProperty(required = true)
                        Byte group;
                        @JsonProperty(required = true)
                        Long user;
                        @JsonProperty(required = true)
                        Boolean status;
                    }

                    public static class Edit {
                        @JsonProperty(required = true)
                        Long channel;
                        @JsonProperty(required = true)
                        Byte group;
                        @JsonProperty(required = true)
                        String key;
                        @JsonProperty(required = true)
                        String value;
                    }
                }
            }

            public static class Users {
                public static class Add {
                    @JsonProperty(required = true)
                    Long channel;
                    @JsonProperty(required = true)
                    Long user;
                }

                public static class Mute {
                    @JsonProperty(required = true)
                    Long channel;
                    @JsonProperty(required = true)
                    Long user;
                    String reason;
                    @JsonSerialize(using = JsonSerializers.TimestampSerializer.class)
                    @JsonDeserialize(using = JsonSerializers.TimestampDeserializer.class)
                    Timestamp ending;
                }

                public static class Ban {
                    @JsonProperty(required = true)
                    Long channel;
                    @JsonProperty(required = true)
                    Long user;
                    @JsonProperty(required = true)
                    String reason;
                    @JsonSerialize(using = JsonSerializers.TimestampSerializer.class)
                    @JsonDeserialize(using = JsonSerializers.TimestampDeserializer.class)
                    Timestamp ending;
                }

                public static class Kick {
                    @JsonProperty(required = true)
                    Long channel;
                    @JsonProperty(required = true)
                    Long client;
                }
            }
        }

        public static class User {
            public static class Messages {
                public static class Post {
                    public static class New {
                        @JsonProperty(required = true)
                        Long channel;
                        @JsonProperty(required = true)
                        String type;

                        @JsonProperty
                        String text = null;
                        @JsonSerialize(using = JsonSerializers.UUIDSerializer.class)
                        @JsonDeserialize(using = JsonSerializers.UUIDDeserializer.class)
                        UUID alias = null;
                        @JsonSerialize(using = JsonSerializers.TimestampSerializer.class)
                        @JsonDeserialize(using = JsonSerializers.TimestampDeserializer.class)
                        Long answer = null;
                        @JsonProperty
                        Long[][] media = null;
                        @JsonProperty
                        Long[] files = null;
                    }
                }
            }

            public static class Presence {
                public static class Join {
                    @JsonProperty(required = true)
                    String invitation;
                }

                public static class Leave {
                    @JsonProperty(required = true)
                    Long channel;
                }
            }
        }
    }
}
