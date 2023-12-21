import exceptions.HandlerNodeTryRegisterSubNodeException;
import exceptions.NodePathAlreadyRegisteredException;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Starter {
    public static volatile boolean running = false;

    static byte DEBUG;
    static Ini config;
    static int wsCorePort;
    static int authorizerPort;

    static WSCore wsCore;
    static Authorizer authorizer;

    static {
        JsonIteratorExtra.UUIDSupport.registerModule( );
        new JsonIteratorExtra.SQLTimestampSupport(Helper.Constants.timestamp).registerModule( );
    }

    static {
        try {
            Console.registerHandler(new String[]{ "stop" }, (String[] _) -> running = false);
        } catch (NodePathAlreadyRegisteredException | HandlerNodeTryRegisterSubNodeException e) {
            System.out.println("Initial server error");
            e.printStackTrace( );
            System.exit(1);
        }
    }

    public static void main (String[] args) throws IOException {
        try {
            File file = new File("./settings.ini");

            if (file.createNewFile( )) {
                createConfiguration(file);
            }

            config = new Ini(file);

            DEBUG = config.get("server", "debug", byte.class);

            wsCorePort = config.get("ws-core", "port", int.class);
            authorizerPort = config.get("http-authorizer", "port", int.class);
        } catch (IOException e) {
            System.out.println("Settings file error");
            e.printStackTrace( );
            System.exit(1);
        }

        running = true;

        wsCore = new WSCore(wsCorePort);
        authorizer = new Authorizer(new InetSocketAddress(authorizerPort), 0);

        authorizer.start( );
        wsCore.start( );
        Console.start( );

        while (running) {
            Thread.onSpinWait( );
        }

        try {
            wsCore.stop( );
            System.out.println("WSCore thread has been stopped");
        } catch (InterruptedException e) {
            System.out.println("WSCore thread has been stopped");
            e.printStackTrace( );
        }

        authorizer.stop( );
        System.out.println("HTTP Authorizer thread has been stopped");
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
                      
                [database]
                base = postgres
                user = postgres
                password = password
                host = localhost
                port = 5432
                application = RTCD | %s
                """);
        writer.flush( );
        writer.close( );
    }
}
