import com.jsoniter.annotation.JsonCreator;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.annotation.JsonWrapper;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Map;

public class CommandsPatterns {
    static class Systems {
        static class Listen {
            static class Add {
                long client;
                long channel;
            }

            static class Remove {
                long client;
                long channel;
            }
        }
    }

    static class Channels {
        static class Messages {
            static class Post {
                static class New {
                    long author;
                    long channel;
                    String text;
                }

                static class ForwardMessage {
                    protected static class SourceMessage {
                        long channel;
                        Timestamp posted;
                    }

                    long author;
                    long channel;
                    @JsonProperty("source")
                    SourceMessage original_message;
                }

                static class ForwardPost {
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

            static class Edit {
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

            static class Delete {
                long admin = -1L;
                long author;
                long message;
            }
        }

        static class Reactions {
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

        static class Users {
            static class Join {
                long user;
                long channel;
                String invitation;
            }

            static class Leave {
                long user;
                long channel;
            }

            static class Add {
                long admin;
                long user;
                long channel;
            }

            static class Remove {
                long admin;
                long user;
                long channel;
            }
        }

        static class Create {
            long owner;
            String title;
        }

        static class Edit {
            static class Visual {
                long admin;
                String key;
                String value;
            }

            static class Permissions {
                long admin;
                long user = 1;
                short[] permission;
                Boolean value = null;
            }

            static class System {

            }
        }

        static class Delete {
            long owner;
            long channel;
        }
    }
}
