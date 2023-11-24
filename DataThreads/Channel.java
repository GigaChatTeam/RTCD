package DataThreads;

import java.util.Objects;

public class Channel {
    public final long id;
    public boolean canPost;

    public Channel (Long id, boolean canPost) {
        this.id = id;
        this.canPost = canPost;
    }

    @Override
    public String toString () {
        return STR. "Channel \{ id }, can user post: \{ canPost }";
    }

    @Override
    public boolean equals (Object obj) {
        if (obj instanceof Channel) {
            return Objects.equals(((Channel) obj).id, id);
        }
        return false;
    }

    @Override
    public int hashCode () {
        return Long.hashCode(id);
    }
}
