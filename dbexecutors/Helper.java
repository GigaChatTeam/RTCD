package dbexecutors;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

class Helper {
    public static Boolean verifierBCrypt (@NotNull String data, byte[] hash_data) {
        return BCrypt.verifyer( ).verify(Arrays.copyOfRange(data.toCharArray( ), 0, Math.min(data.toCharArray( ).length, 72)), hash_data).verified;
    }

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
}
