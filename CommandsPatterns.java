import java.util.Map;

public class CommandsPatterns {
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
                        Map<Long, Byte[]> layout;
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

            }

            static class Permissions {

            }

            static class System {

            }
        }

        static class Delete {

        }
    }
}
