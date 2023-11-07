import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.output.JsonStream;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.ParseException;

import static java.lang.String.join;

public class ResponsesPatterns {
    static class Channels {
        static class Messages {
            static class Post {
                static class New {
                    static final String[] intention = Commands.USER_CHANNELS_MESSAGES_POST_NEW.intents;

                    long author;
                    long channel;
                    String type = "TEXT MESSAGE";
                    String text;
                    String posted;

                    New (CommandsPatterns.Channels.Messages.Post.@NotNull New command, Timestamp posted) {
                        this.author = command.author;
                        this.channel = command.channel;
                        this.text = command.text;
                        this.posted = Helper.Constants.timestamp.format(posted);
                    }

                    New (long author, long channel, String text, Timestamp posted) {
                        this.author = author;
                        this.channel = channel;
                        this.text = text;
                        this.posted = Helper.Constants.timestamp.format(posted);
                    }

                    New (long author, long channel, String type, String text, Timestamp posted) {
                        this.author = author;
                        this.channel = channel;
                        this.type = type;
                        this.text = text;
                        this.posted = Helper.Constants.timestamp.format(posted);
                    }

                    String serialize (String hash) {
                        return STR. "\{ join("-", intention) }%\{ hash }%\{ JsonStream.serialize(this) }" ;
                    }
                }
            }

            static class Edit {
                @JsonIgnore
                static final String[] intention = Commands.USER_CHANNELS_MESSAGES_EDIT.intents;

                long author;
                long channel;
                String posted;
                String text;
                CommandsPatterns.Channels.Messages.Edit.Attachments attachments;

                Edit (CommandsPatterns.Channels.Messages.@NotNull Edit command) throws ParseException {
                    this.author = command.author;
                    this.channel = command.channel;
                    this.posted = Helper.Constants.timestamp.format(command.posted);
                    this.text = command.text;
                    this.attachments = command.attachments;
                }

                String serialize (String hash) {
                    return STR. "\{ join("-", intention) }%\{ hash }%\{ JsonStream.serialize(this) }" ;
                }
            }

            static class Delete {
                static final String[] intention = Commands.USER_CHANNELS_MESSAGES_DELETE.intents;

                long channel;
                String posted;

                Delete (CommandsPatterns.Channels.Messages.@NotNull Delete command) {

                }
            }
        }

        static class Settings {
            static class External {
                static class Change {
                    static class Title {
                        @JsonIgnore
                        static final String[] intention = Commands.ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_TITLE.intents;

                        long channel;
                        @JsonProperty("new-description")
                        String newTitle;

                        Title (CommandsPatterns.Channels.Settings.External.Change.@NotNull Title command) {
                            this.channel = command.channel;
                            this.newTitle = command.newTitle;
                        }

                        String serialize (String hash) {
                            return STR. "\{ join("-", intention) }%\{ hash }%\{ JsonStream.serialize(this) }" ;
                        }
                    }

                    static class Description {
                        @JsonIgnore
                        static final String[] intention = Commands.ADMIN_CHANNELS_SETTINGS_EXTERNAL_CHANGE_DESCRIPTION.intents;

                        long channel;
                        @JsonProperty("new-description")
                        String newDescription;

                        Description (CommandsPatterns.Channels.Settings.External.Change.@NotNull Description command) {
                            this.channel = command.channel;
                            this.newDescription = command.newDescription;
                        }

                        String serialize (String hash) {
                            return STR. "\{ join("-", intention) }%\{ hash }%\{ JsonStream.serialize(this) }" ;
                        }
                    }
                }
            }
        }

        static class Create {
            @JsonIgnore
            static final String[] intention = Commands.ADMIN_CHANNELS_CREATE.intents;

            long owner;
            String title;
            long id;

            Create (CommandsPatterns.Channels.@NotNull Create command, long id) {
                this.owner = command.owner;
                this.title = command.title;
                this.id = id;
            }

            String serialize (String hash) {
                return STR. "\{ join("-", intention) }%\{ hash }%\{ JsonStream.serialize(this) }" ;
            }
        }
    }

    static class System {
        static class TTokens {
            static class Generate {
                @JsonIgnore
                static final String[] intention = Commands.SYSTEM_TTOKENS_GENERATE.intents;

                @JsonProperty("intention")
                String[] intentions;
                String token;

                Generate (String[] intentions, String token) {
                    this.intentions = intentions;
                    this.token = token;
                }

                String serialize (String hash) {
                    return STR. "\{ join("-", intention) }%\{ hash }%\{ JsonStream.serialize(this) }" ;
                }
            }
        }
    }
}