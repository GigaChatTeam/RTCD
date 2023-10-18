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
                    long author;
                    long channel;
                    long original_message;
                }
                static class ForwardPost {
                    long author;
                    long channel;
                    long original_community;
                    long original_post;
                }
            }
            static class Edit {
                static class Attachments {
                    static class Reorganize {
                        long author;
                        long message;
                        Map<Long, byte[]> layout;
                    }
                }
                static class Text {
                    long message;
                    long author;
                    String new_text;
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
