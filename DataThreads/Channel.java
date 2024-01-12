package DataThreads;

import exceptions.KeyNotInitialized;

import java.util.HashMap;
import java.util.Objects;

public class Channel {
    public final long id;
    private final HashMap<Short, Boolean> rights;

    public Channel (Long id, HashMap<Short, Boolean> rights) {
        this.id = id;
        this.rights = rights;
    }

    public boolean validateRule (Short ruleID) {
        return rights.get(ruleID) != null && rights.get(ruleID);
    }

    public void updateRule (Short ruleID, Boolean newStatus) throws KeyNotInitialized {
        if (rights.containsKey(ruleID)) {
            rights.put(ruleID, newStatus);
        } else throw new KeyNotInitialized( );
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
