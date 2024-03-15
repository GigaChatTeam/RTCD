package dbexecutors.patterns;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;

public class Channels {
    record Create(

    ) {

    }

    public static class Messages {
        public final static class ForwardBy {
            public final static ForwardBy NO_FORWARD = noForward( );

            public enum ForwardType {
                NO("no"),
                CHANNEL_MESSAGE("channel message");

                public final String id;

                private ForwardType (String id) {
                    this.id = id;
                }
            }

            @Contract(value = " -> new", pure = true)
            private static @NotNull ForwardBy noForward ( ) {
                return new ForwardBy(
                        false,
                        ForwardBy.ForwardType.NO,
                        new Long[0]);
            }

            ForwardBy (
                    Boolean is,
                    ForwardBy.ForwardType type,
                    Long[] by) {
                this.is = is;
                this.type = type;
                this.by = by;
            }

            public Boolean is;
            public ForwardBy.ForwardType type;
            public Long[] by;
        }

        public record Message(
                Long channelID,
                Long id,
                Timestamp timestamp,
                Long author,
                String data,
                Long[] files,
                Long[][] media,
                ForwardBy forward
        ) {
        }
    }
}
