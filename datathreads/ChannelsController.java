package datathreads;

import java.util.Objects;

public class ChannelsController {
    public record Channel(
            long channelID,
            boolean isEnabled) {
        @Override
        public boolean equals (Object obj) {
            if (obj instanceof Channel) {
                return Objects.equals(((Channel) obj).channelID( ), channelID( ));
            }
            return false;
        }
    }
}
