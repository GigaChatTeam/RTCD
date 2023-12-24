import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.output.JsonStream;

import java.sql.Timestamp;
import java.util.UUID;

public class ResponsesPatterns {
    public static class System {
        public static class ServerErrors {
            public static class InternalError {
                @JsonIgnore
                static final String intention = "88";

                String serialize (String controlHash) {
                    return STR."\{intention}%\{controlHash}%{}";
                }
            }

            public static class OutdatedServer {
                @JsonIgnore
                static final String intention = "89";

                String serialize (String controlHash) {
                    return STR."\{intention}%\{controlHash}%{}";
                }
            }
        }

        public static class ClientErrors {
            public static class DataErrors {
                public static class NotValidIntentions {
                    static final String intention = "210";

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%{}";
                    }
                }

                public static class NotValidData {
                    static final String intention = "210";

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%{}";
                    }
                }
            }

            public static class AccessErrors {
                public static class AccessDenied {
                    @JsonIgnore
                    static final String intention = "89";

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%{}";
                    }
                }

                public static class AlreadyCompleted {
                    @JsonIgnore
                    static final String intention = "212";

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%{}";
                    }
                }

                public static class NotFound {
                    @JsonIgnore
                    static final String intention = "213";

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%{}";
                    }
                }
            }
        }

        public static class ConnectionParameters {
            public static class ConnectionControl {
                @JsonIgnore
                static final String intention = "88";
                boolean status;

                ConnectionControl (boolean status) {
                    this.status = status;
                }

                String serialize (String controlHash) {
                    return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                }
            }
        }
    }

    public static class Channels {
        public static class System {
            public static class Control {
                public static class Create {
                    @JsonIgnore
                    static final String intention = "";

                    long id;
                    String title;
                    String description = "";

                    Create (long id, String title, String description) {
                        this.id = id;
                        this.title = title;
                        this.description = description;
                    }

                    Create (long id, String title) {
                        this.id = id;
                        this.title = title;
                    }

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                    }
                }

                public static class Delete {
                    long id;
                    String reason = "";
                }
            }

            public static class Notification {
                public static class AddListening {
                    @JsonIgnore
                    static final String intention = "B1";

                    long channel;

                    AddListening (long channel) {
                        this.channel = channel;
                    }

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                    }
                }

                public static class RemoveListening {
                    @JsonIgnore
                    static final String intention = "B2";

                    long channel;

                    RemoveListening (long channel) {
                        this.channel = channel;
                    }

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                    }
                }
            }
        }

        public static class Administration {

        }


        public static class User {
            public static class Messages {
                public static class Post {
                    public static class New {
                        @JsonIgnore
                        static final String intention = "5CF";

                        long channel;
                        long author;
                        String type;

                        String text;
                        UUID alias;
                        Timestamp posted;
                        Timestamp answer;
                        Long[][] media;
                        Long[] files;

                        New (long channel, long author, String text, Timestamp posted, Long[][] media, Long[] files) {
                            this.channel = channel;
                            this.author = author;
                            this.text = text;
                            this.posted = posted;
                            this.media = media;
                            this.files = files;
                        }

                        New (long channel, UUID alias, String text, Timestamp posted, Long[][] media, Long[] files) {
                            this.channel = channel;
                            this.alias = alias;
                            this.text = text;
                            this.posted = posted;
                            this.media = media;
                            this.files = files;
                        }

                        String serialize (String controlHash) {
                            return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                        }
                    }
                }
            }

            public static class Presence {
                public static class Join {
                    @JsonIgnore
                    String intention = "171";

                    long channel;

                    Join (long channel) {
                        this.channel = channel;
                    }

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                    }
                }

                public static class Leave {
                    @JsonIgnore
                    String intention = "170";

                    public Leave () {

                    }

                    String serialize (String controlHash) {
                        return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                    }
                }
            }
        }

        public static class Invitations {
            public static class Create {
                @JsonIgnore
                static final String intention = "175";

                long creator;
                String uri;

                Create (long creator, String uri) {
                    this.creator = creator;
                    this.uri = uri;
                }

                String serialize (String controlHash) {
                    return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                }
            }

            public static class Delete {
                @JsonIgnore
                static final String intention = "174";

                String uri;

                Delete (String uri) {
                    this.uri = uri;
                }

                String serialize (String controlHash) {
                    return STR."\{intention}%\{controlHash}%\{JsonStream.serialize(this)}";
                }
            }
        }
    }
}