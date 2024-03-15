import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.UUID;

public class ResponsesPatterns {
    private static final ObjectMapper objectMapper = new ObjectMapper( );

    static {
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }


    public static class Channels {
        public static class System {
            public static class Control {
                public static class Create {
                    @JsonIgnore
                    static final String intention = "";

                    Long id;
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

                    String serialize (String controlHash) throws JsonProcessingException {
                        return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
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

                    String serialize (String controlHash) throws JsonProcessingException {
                        return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
                    }
                }

                public static class RemoveListening {
                    @JsonIgnore
                    static final String intention = "B2";

                    long channel;

                    RemoveListening (long channel) {
                        this.channel = channel;
                    }

                    String serialize (String controlHash) throws JsonProcessingException {
                        return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
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

                        Long channel;
                        Long author;
                        String type;

                        String text;
                        UUID alias;
                        Timestamp posted;
                        Timestamp answer;
                        Long[][] media;
                        Long[] files;

                        New (Long channel, Long author, String type, String text, Timestamp posted, Long[][] media, Long[] files) {
                            this.channel = channel;
                            this.author = author;
                            this.type = type;
                            this.alias = null;
                            this.text = text;
                            this.posted = posted;
                            this.media = media;
                            this.files = files;
                        }

                        New (Long channel, UUID alias, String type, String text, Timestamp posted, Long[][] media, Long[] files) {
                            this.channel = channel;
                            this.author = null;
                            this.alias = alias;
                            this.text = text;
                            this.posted = posted;
                            this.media = media;
                            this.files = files;
                        }

                        String serialize (String controlHash) throws JsonProcessingException {
                            return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
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

                    String serialize (String controlHash) throws JsonProcessingException {
                        return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
                    }
                }

                public static class Leave {
                    @JsonIgnore
                    String intention = "170";

                    public Leave ( ) {

                    }

                    String serialize (String controlHash) throws JsonProcessingException {
                        return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
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

                String serialize (String controlHash) throws JsonProcessingException {
                    return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
                }
            }

            public static class Delete {
                @JsonIgnore
                static final String intention = "174";

                String uri;

                Delete (String uri) {
                    this.uri = uri;
                }

                String serialize (String controlHash) throws JsonProcessingException {
                    return STR."\{intention}%\{controlHash}%\{objectMapper.writeValueAsString(this)}";
                }
            }
        }
    }
}