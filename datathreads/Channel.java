package datathreads;

import exceptions.KeyNotInitialized;

import java.util.HashMap;
import java.util.Objects;

public record Channel(long id, HashMap<Short, Boolean> rights) {
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
}
