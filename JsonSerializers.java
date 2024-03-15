import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class JsonSerializers {
    public static class TimestampSerializer extends JsonSerializer<Timestamp> {
        @Override
        public void serialize (@NotNull Timestamp value, @NotNull JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
            jsonGenerator.writeNumber(value.toInstant().toEpochMilli() * 1000);
        }
    }

    public static class TimestampDeserializer extends JsonDeserializer<Timestamp> {
        @Override
        public Timestamp deserialize (@NotNull JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return Timestamp.from(Instant.ofEpochMilli(jsonParser.getLongValue() / 1000));
        }
    }

    public static class UUIDSerializer extends JsonSerializer<UUID> {
        @Override
        public void serialize (@NotNull UUID value, @NotNull JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
            jsonGenerator.writeString(value.toString());
        }
    }

    public static class UUIDDeserializer extends JsonDeserializer<UUID> {
        @Override
        public UUID deserialize (@NotNull JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return UUID.fromString(jsonParser.getValueAsString());
        }
    }
}
