import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Objects;

public class Helper {
    static String SHA512 (String string) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            return string;
        }

        byte[] bytes = md.digest(string.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder( );

        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString( );
    }

    public static @Nullable Object firstNonNull (Object... objects) {
        return Arrays.stream(objects)
                .filter(Objects::nonNull)
                .findFirst( )
                .orElse(null);
    }

    static final class Constants {
        static final SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static class MessagePacket {
        final String hash;
        final String intention;
        final String preData;
        Object postData;

        public MessagePacket (@NotNull String packet) throws ParseException {
            String[] splitPacket = packet.split("%", 3);

            if (splitPacket.length != 3) throw new ParseException("Invalid packet", 0);

            this.intention = splitPacket[0];
            this.hash = splitPacket[1];
            this.preData = splitPacket[2];
        }

        public void parseData (Class<?> pattern) {
            postData = JsonIterator.deserialize(preData, pattern);
        }
    }

    public static class TTokenQueryWrapper {
        String intention;
        Any data;
    }
}
