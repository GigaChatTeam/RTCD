import exceptions.HandlerNodeTryRegisterSubNodeException;
import exceptions.NodeNotFoundException;
import exceptions.NodePathAlreadyRegisteredException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

import static java.util.Arrays.copyOfRange;

public class Node {
    public final HashMap<String, Node> container;
    public final Consumer<String[]> handler;

    public Node (@NotNull String[] path, Consumer<String[]> handler) {
        if (path.length == 0) {
            this.handler = handler;
            this.container = null;
        } else {
            this.handler = null;
            this.container = new HashMap<>( );

            this.container.put(path[0], new Node(copyOfRange(path, 1, path.length), handler));
        }
    }

    public Node () {
        this.container = new HashMap<>( );
        this.handler = null;
    }

    public void addNode (String[] path, Consumer<String[]> handler) throws HandlerNodeTryRegisterSubNodeException, NodePathAlreadyRegisteredException {
        if (this.handler != null) throw new HandlerNodeTryRegisterSubNodeException( );
        if (container.containsKey(path[0])) throw new NodePathAlreadyRegisteredException( );

        container.put(path[0], new Node(copyOfRange(path, 1, path.length), handler));
    }

    public void call (String[] path) throws NodeNotFoundException {
        if (handler != null) {
            if (path.length > 0) {
                handler.accept(copyOfRange(path, 1, path.length));
            } else handler.accept(new String[]{ });
            return;
        }
        if (!container.containsKey(path[0])) throw new NodeNotFoundException( );

        container.get(path[0]).call(copyOfRange(path, 1, path.length));
    }
}
