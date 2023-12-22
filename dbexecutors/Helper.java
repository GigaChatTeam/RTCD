package dbexecutors;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class Helper {
    public static boolean verifierBCrypt (@NotNull String data, byte[] hashData) {
        return BCrypt.verifyer( ).verify(Arrays.copyOfRange(data.toCharArray( ), 0, Math.min(data.toCharArray( ).length, 72)), hashData).verified;
    }

    static @NotNull String SHA512 (@NotNull String string) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        StringBuilder sb = new StringBuilder( );

        for (byte aByte : md.digest(string.getBytes(StandardCharsets.UTF_8))) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString( );
    }
}
