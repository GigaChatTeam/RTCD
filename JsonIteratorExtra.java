import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Decoder;
import com.jsoniter.spi.Encoder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import static com.jsoniter.spi.JsoniterSpi.registerTypeDecoder;
import static com.jsoniter.spi.JsoniterSpi.registerTypeEncoder;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class JsonIteratorExtra {
    public static class UUIDSupport implements Encoder, Decoder {
        @Override
        public UUID decode (@NotNull JsonIterator jsonIterator) throws IOException {
            String value = jsonIterator.readString( );
            if (value.charAt(0) == '"' && value.charAt(value.length( ) - 1) == '"') {
                return UUID.fromString(value.substring(1, value.length( ) - 1));
            } else throw new IOException( );
        }

        @Override
        public void encode (@NotNull Object obj, @NotNull JsonStream stream) throws IOException {
            stream.writeVal(STR."\"\{obj.toString( )}\"");
        }

        public static void registerHandler () {
            UUIDSupport operator = new UUIDSupport( );
            registerTypeDecoder(UUID.class, operator);
            registerTypeEncoder(UUID.class, operator);
        }
    }

    public static class SQLTimestampSupport implements Encoder, Decoder {
        @Override
        public Timestamp decode (@NotNull JsonIterator jsonIterator) throws IOException {
            try {
                String[] values = String.format("%.6f", jsonIterator.readDouble( )).split("\\.");

                Timestamp result = new Timestamp(parseLong(values[0]) * 1000);
                result.setNanos(parseInt(values[1]) * 1000);

                return result;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        @Override
        public void encode (Object obj, @NotNull JsonStream stream) throws IOException {
            stream.writeVal(STR."\{((Timestamp) obj).getTime( )}.\{((Timestamp) obj).getNanos( )}");
        }

        public static void registerHandler () {
            SQLTimestampSupport operator = new SQLTimestampSupport( );
            registerTypeDecoder(Timestamp.class, operator);
            registerTypeEncoder(Timestamp.class, operator);
        }
    }
}
