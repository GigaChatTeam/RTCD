import dbexecutors.sql.PoolController;
import exceptions.HandlerNodeTryRegisterSubNodeException;
import exceptions.NodePathAlreadyRegisteredException;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.Thread.onSpinWait;

public class Starter {
    public static volatile boolean running = false;

    static byte DEBUG;
    static Ini configFile;
    static int wsCorePort;
    static int authorizerPort;

    static WSCore wsCore;
    static Authorizer authorizer;

    static {
        try {
            Console.registerHandler(new String[]{ "stop" }, (String[] _) -> running = false);
        } catch (NodePathAlreadyRegisteredException | HandlerNodeTryRegisterSubNodeException e) {
            System.out.println("Initial server error");
            e.printStackTrace( );
            System.exit(1);
        }
    }

    public static void main (String[] args) {
        try {
            File file = new File("./settings.ini");

            if (file.createNewFile( )) {
                createConfiguration(file);
            }

            configFile = new Ini(file);

            DEBUG = configFile.get("server", "debug", byte.class);

            PoolController.initializePool(new PoolController.Configuration(configFile));

            wsCorePort = configFile.get("ws-core", "port", int.class);
            authorizerPort = configFile.get("http-authorizer", "port", int.class);
        } catch (IOException e) {
            System.out.println("Settings file error");
            e.printStackTrace( );
            System.exit(1);
        }

        running = true;

        try {
            wsCore = new WSCore(wsCorePort);
            authorizer = new Authorizer(new InetSocketAddress(authorizerPort), 0);
        } catch (Exception e) {
            System.out.println("Initialize servers`s cores error");
            e.printStackTrace( );
            System.exit(1);
        }

        Console.start( );
        if (!running) System.exit(1);

        authorizer.start( );
        wsCore.start( );

        while (running) onSpinWait( );

        try {
            wsCore.stop( );
        } catch (InterruptedException e) {
            e.printStackTrace( );
        } finally {
            System.out.println("WSCore thread has been stopped");
        }

        try {
            authorizer.stop( );
        } finally {
            System.out.println("HTTP Authorizer thread has been stopped");
        }
    }

    private static void createConfiguration (File file) throws IOException {
        FileWriter writer = new FileWriter(file);

        writer.write("""
                [server]
                id = LOCAL
                debug = 0

                [ws-core]
                port = 8080

                [http-authorizer]
                port = 8081
                
                [database-pool]
                min-connections = 0
                max-connections = 1
                timeout = 30000
                
                [database]
                base = postgres
                user = postgres
                password = password
                host = localhost
                port = 5432
                application = RTCD | %s
                pool-name = pool connection
                                
                [elastic]
                secure = true
                url = localhost:9200
                key = key
                """);
        writer.flush( );
        writer.close( );
    }

    public static void stopRuntimeByCriticalError (@NotNull Exception error) {
        System.out.println("Critical error");
        error.printStackTrace( );

        running = false;
    }
}
