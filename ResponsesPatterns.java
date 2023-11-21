import com.jsoniter.annotation.JsonIgnore;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.output.JsonStream;
import dbexecutors.SystemExecutor;

import static java.lang.String.join;

public class ResponsesPatterns {
    static class System {
        static class ServerErrors {
            static class InternalError {
                @JsonIgnore
                static final String intention = "88";

                String serialize (String controlHash) {
                    return STR. "\{ intention }%\{ controlHash }%{}" ;
                }
            }

            static class OutdatedServer {
                @JsonIgnore
                static final String intention = "89";

                String serialize (String controlHash) {
                    return STR. "\{ intention }%\{ controlHash }%{}" ;
                }
            }
        }

        static class ClientErrors {
            static class DataErrors {
                static class NotValidIntentions {
                    static final String intention = "210";

                    String serialize (String controlHash) {
                        return STR. "\{ intention }%\{ controlHash }%{}" ;
                    }
                }

                static class NotValidData {
                    static final String intention = "210";

                    String serialize (String controlHash) {
                        return STR. "\{ intention }%\{ controlHash }%{}" ;
                    }
                }
            }

            static class AccessErrors {
                static class AccessDenied {
                    @JsonIgnore
                    static final String intention = "89";

                    String serialize (String controlHash) {
                        return STR. "\{ intention }%\{ controlHash }%{}" ;
                    }
                }

                static class NotFound {
                    @JsonIgnore
                    static final String intention = "89";

                    String serialize (String controlHash) {
                        return STR. "\{ intention }%\{ controlHash }%{}" ;
                    }
                }
            }
        }

        static class ConnectionParameters {
            static class ConnectionControl {
                @JsonIgnore
                static final String intention = "88";
                boolean status;

                ConnectionControl (boolean status) {
                    this.status = status;
                }

                String serialize (String controlHash) {
                    return STR. "\{ intention }%\{ controlHash }%\{ JsonStream.serialize(this) }" ;
                }
            }
        }

        static class TTokens {
            static class Generate {
                @JsonIgnore
                static final String intention = "";

                @JsonProperty("intention")
                String intentions;
                String token;

                Generate (SystemExecutor.Channels.Token token) {
                    this.intentions = join("-", token.intention);
                    this.token = token.token;
                }

                String serialize (String controlHash) {
                    return STR. "\{ intention }%\{ controlHash }%\{ JsonStream.serialize(this) }" ;
                }
            }
        }
    }

    static class Channels {
        static class System {
            static class Control {
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
                        return STR. "\{ intention }%\{ controlHash }%\{ JsonStream.serialize(this) }" ;
                    }
                }

                public static class Delete {
                    long id;
                    String reason = "";
                }
            }

            static class Notification {
                static class AddListening {
                    @JsonIgnore
                    static final String intention = "B1";

                    long channel;

                    AddListening (long channel) {
                        this.channel = channel;
                    }

                    String serialize (String controlHash) {
                        return STR. "\{ intention }%\{ controlHash }%\{ JsonStream.serialize(this) }" ;
                    }
                }

                String serialize (String hash) {
                    return STR. "\{ join("-", intention) }%\{ hash }%\{ JsonStream.serialize(this) }" ;
                }
            }
        }
    }
}