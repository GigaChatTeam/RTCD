import org.ini4j.Ini;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Starter {
    static byte DEBUG;
    static int port;
    static Ini config;

    static {
        JsonIteratorExtra.UUIDSupport.registerModule();
        new JsonIteratorExtra.SQLTimestampSupport(Helper.Constants.timestamp).registerModule();
    }

    public static void main (String[] args) {
        try {
            File file = new File("./settings.ini");

            if (!file.createNewFile( )) {
                getFileWriter(file);
            }

            config = new Ini(file);
            port = config.get("server", "port", int.class);
            DEBUG = config.get("server", "debug", byte.class);
        } catch (IOException e) {
            System.out.println("Settings file error");
            e.printStackTrace( );
            System.exit(1);
        }

        WSCore server = new WSCore(port);
        server.start();
    }

    private static void getFileWriter (File file) throws IOException {
        FileWriter writer = new FileWriter(file);

        writer.write("""
                [server]
                id = LOCAL
                debug = 0
                port = 8080
                                        
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
