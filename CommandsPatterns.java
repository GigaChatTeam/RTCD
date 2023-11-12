import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;

public class CommandsPatterns {
    public static class Systems {
        public static class Listen {
            public static class Channel {
                public static class Add {
                    long client;
                    long channel;
                }

                public static class Remove {
                    long client;
                    long channel;
                }
            }
        }

        public static class TTokens {
            protected static class TTokensPatterns {
                public static class Users {
                    public static class Download {
                        public static class Channels {
                            public static class Messages {
                                public static class History {
                                    long channel;
                                }
                            }

                            public static class Permissions {
                                long channel;
                            }
                        }
                    }
                }
            }

            public enum Generate {
                USERS_DOWNLOAD_CHANNELS_MESSAGES_HISTORY(new String[]{"USERS", "DOWNLOAD", "CHANNELS", "MESSAGES"}, TTokensPatterns.Users.Download.Channels.Messages.History.class),
                USERS_DOWNLOAD_CHANNELS_PERMISSIONS(new String[]{"USERS", "DOWNLOAD", "CHANNELS", "PERMISSIONS"}, TTokensPatterns.Users.Download.Channels.Permissions.class);

                final String[] intents;
                final Class<?> pattern;

                Generate (String[] intents, Class<?> pattern) {
                    this.intents = intents;
                    this.pattern = pattern;
                }

                public static Generate byIntents (String[] intents) {
                    return Arrays.stream(Generate.values())
                            .filter(v -> Arrays.equals(v.intents, intents))
                            .findFirst()
                            .orElse(null);
                }
            }
        }
    }

    public static class Channels {
        public static class Messages {
            public static class Post {
                public static class New {
                    long author;
                    long channel;
                    String text;
                }

                public static class ForwardMessage {
                    protected static class SourceMessage {
                        long channel;
                        Timestamp posted;
                    }

                    long author;
                    long channel;
                    @JsonProperty("source")
                    SourceMessage original_message;
                }

                public static class ForwardPost {
                    protected static class SourcePost {
                        long channel;
                        Timestamp posted;
                    }

                    long author;
                    long channel;
                    long original_community;
                    long original_post;
                }
            }

            public static class Edit {
                static class Attachments {
                    long[] attachments;
                    byte[][] layout;
                }

                long author;
                long channel;
                Timestamp posted;
                String text;
                Attachments attachments;

                @JsonCreator
                Edit (
                        @JsonProperty("author") long author,
                        @JsonProperty("channel") long channel,
                        @JsonProperty("posted") String posted,
                        @JsonProperty("text") String text,
                        @JsonProperty("attachments") Attachments attachments) throws ParseException {
                    this.author = author;
                    this.channel = channel;
                    this.posted = new Timestamp(Helper.Constants.timestamp.parse(posted).getTime());
                    this.text = text;
                    this.attachments = attachments;
                }
            }

            public static class Delete {
                long admin = -1L;
                long author;
                long message;
            }
        }

        public static class Reactions {
            static class Add {
                long author;
                long message;
                String[] reaction;
            }

            static class Remove {
                long author;
                long message;
            }
        }

        public static class Users {
            public static class Join {
                long user;
                long channel;
                String invitation;
            }

            public static class Leave {
                long user;
                long channel;
            }

            public static class Add {
                long admin;
                long user;
                long channel;
            }

            public static class Remove {
                long admin;
                long user;
                long channel;
            }
        }

        public static class Create {
            long owner;
            String title;
        }

        public static class Settings {
            public static class External {
                public static class Change {
                    public static class Title {
                        long client;
                        long channel;
                        @JsonProperty("new-title")
                        String newTitle;
                    }

                    public static class Description {
                        long client;
                        long channel;
                        @JsonProperty("new-description")
                        String newDescription;
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
