import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ResponsesPatterns {
    static class Channels {
        static class Messages {
            static class Post {
                static class New {
                    long author;
                    long channel;
                    String text;
                    long id;

                    @Contract(pure = true)
                    New (CommandsPatterns.Channels.Messages.Post.@NotNull New command, long id) {
                        this.author = command.author;
                        this.channel = command.channel;
                        this.text = command.text;
                        this.id = id;
                    }
                }
            }
        }

        static class Create {
            long owner;
            String title;
            long id;

            @Contract(pure = true)
            Create (CommandsPatterns.Channels.@NotNull Create command, long id) {
                this.owner = command.owner;
                this.title = command.title;
                this.id = id;
            }
        }
    }
}
