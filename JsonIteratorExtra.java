import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Decoder;
import com.jsoniter.spi.Encoder;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import static com.jsoniter.spi.JsoniterSpi.registerTypeDecoder;
import static com.jsoniter.spi.JsoniterSpi.registerTypeEncoder;

public class JsonIteratorExtra {
    public static class UUIDSupport implements Encoder, Decoder {
        public static void registerModule () {
            registerTypeDecoder(UUID.class, jsonIterator -> UUID.fromString(jsonIterator.readString( )));
            registerTypeEncoder(UUID.class, (obj, stream) -> stream.writeVal(obj.toString( )));
        }

        @Override
        public Object decode (JsonIterator jsonIterator) throws IOException {
            return UUID.fromString(jsonIterator.readString( ));
        }

        @Override
        public void encode (Object obj, JsonStream stream) throws IOException {
            stream.writeVal(obj.toString( ));
        }
    }

    public record SQLTimestampSupport(SimpleDateFormat dateFormat) implements Decoder, Encoder {
        @Override
        public Object decode (JsonIterator jsonIterator) throws IOException {
            String timestampStr = jsonIterator.readString( );
            return Timestamp.valueOf(timestampStr);
        }

        @Override
        public void encode (Object obj, JsonStream stream) throws IOException {
            Timestamp timestamp = (Timestamp) obj;
            stream.writeVal(timestamp.toString( ));
        }

        private Timestamp parse (JsonIterator jsonIterator) throws IOException {
            try {
                return new Timestamp(dateFormat.parse(jsonIterator.readString( )).getTime( ));
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }

        public void registerModule () {
            registerTypeDecoder(Timestamp.class, this::parse);
            registerTypeEncoder(Timestamp.class, (obj, stream) -> stream.writeVal(obj.toString( )));
        }
    }
}
