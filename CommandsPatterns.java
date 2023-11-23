import com.jsoniter.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Objects;

public class CommandsPatterns {
    public static class System {
        public static class ConnectionParameters {
            static class ConnectionControl {
                @JsonProperty(required = true)
                boolean status;
            }
        }

        static class TTokens {
            enum Generate {
                HLB_CHANNELS_USERS_DOWNLOAD(null, Patterns.HLB.Channels.class),
                HLB_CHANNELS_USERS_DOWNLOAD_MESSAGES(null, Patterns.HLB.Channels.Users.Messages.class),
                HLB_CHANNELS_USERS_DOWNLOAD_PERMISSIONS(null, Patterns.HLB.Channels.Users.Permissions.class);

                final String intents;
                final Class<?> pattern;

                Generate (String intents, Class<?> pattern) {
                    this.intents = intents;
                    this.pattern = pattern;
                }

                public static Generate byIntents (String intents) {
                    return Arrays.stream(Generate.values())
                            .filter(v -> Objects.equals(v.intents, intents))
                            .findFirst()
                            .orElse(null);
                }
            }

            static class Patterns {
                public static class HLB {
                    public static class Channels {
                        public static class Users {
                            public static class Messages {
                                @JsonProperty(required = true)
                                long channel;
                            }

                            public static class Permissions {
                                @JsonProperty(required = true)
                                long channel;
                            }
                        }
                    }
                }
            }
        }
    }

    public static class Channels {
        public static class System {
            public static class Control {
                public static class Create {
                    @JsonProperty(required = true)
                    String title;
                    @JsonProperty(required = true)
                    String description = "";
                }

                public static class Delete {
                    @JsonProperty(required = true)
                    long id;
                    @JsonProperty(required = true)
                    String reason = "";
                }
            }

            static class Notification {
                static class AddListening {
                    @JsonProperty(required = true)
                    long channel;
                }

                static class RemoveListening {
                    @JsonProperty(required = true)
                    long channel;
                }
            }
        }

        public static class Administration {
            public static class Permissions {
                public static class Groups {
                    public static class Create {
                        @JsonProperty(required = true)
                        long channel;
                        byte position = 0;
                        @JsonProperty(required = true)
                        String title;
                    }

                    public static class Edit {
                        @JsonProperty(required = true)
                        long channel;
                        @JsonProperty(required = true)
                        byte group;
                        @JsonProperty(required = true)
                        String key;
                        @JsonProperty(required = true)
                        String value;
                    }

                    public static class Delete {
                        @JsonProperty(required = true)
                        long channel;
                        @JsonProperty(required = true)
                        byte group;
                    }
                }

                public static class Users {
                    public static class Groups {
                        @JsonProperty(required = true)
                        long channel;
                        @JsonProperty(required = true)
                        byte group;
                        @JsonProperty(required = true)
                        long user;
                        @JsonProperty(required = true)
                        boolean status;
                    }

                    public static class Edit {
                        @JsonProperty(required = true)
                        long channel;
                        @JsonProperty(required = true)
                        byte group;
                        @JsonProperty(required = true)
                        String key;
                        @JsonProperty(required = true)
                        String value;
                    }
                }
            }
        }

        public static class Delete {
            long owner;
            long channel;
        }
    }
}
