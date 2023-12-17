import exceptions.HandlerNodeTryRegisterSubNodeException;
import exceptions.NodeNotFoundException;
import exceptions.NodePathAlreadyRegisteredException;

import java.util.ArrayDeque;
import java.util.Scanner;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class Console {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static final ArrayDeque<String[]> queue = new ArrayDeque<>( );
    static final Node handlers = new Node( );

    static Thread console = new Thread(() -> {
        Scanner scanner = new Scanner(System.in);

        while (Starter.running) {
            String command = scanner.nextLine( );
            if (!command.startsWith("/")) {
                continue;
            }

            try {
                handlers.call(command.substring(1).split(" "));
            } catch (NodeNotFoundException e) {
                System.out.println(STR."\{ANSI_RED}Command not found /\{command.substring(1)}\{ANSI_RESET}");
            }
        }
    });

    static Thread executor = new Thread(() -> {
        while (Starter.running) {
            String[] command = null;

            while (command == null && Starter.running) {
                command = queue.pollFirst( );
                Thread.onSpinWait( );
            }

            if (!Starter.running) return;

            try {
                handlers.call(command);
            } catch (NodeNotFoundException e) {
                System.out.println(STR."\{ANSI_RED}Command not found /\{String.join(" ", requireNonNull(command))}\{ANSI_RESET}");
            }
        }
    });

    static void registerHandler (String[] path, Consumer<String[]> handler) throws NodePathAlreadyRegisteredException, HandlerNodeTryRegisterSubNodeException {
        handlers.addNode(path, handler);
    }

    static void start () {
        executor.start( );
        console.start( );
    }

    public static void callCommand (String[] path) throws NodeNotFoundException {
        handlers.call(path);
    }
}
