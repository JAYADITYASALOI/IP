package newsletter.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Password utility using PBKDF2WithHmacSHA256 (no external dependencies).
 *
 * Stored password format: iterations:saltBase64:hashBase64
 *
 * Usage:
 *   String stored = PasswordUtil.hashPassword("plainPassword");
 *   boolean ok = PasswordUtil.verifyPassword("plainPassword", stored);
 *
 * Notes:
 * - Iteration count and key length are configurable constants below.
 * - For production, choose iteration count according to current recommendations and hardware.
 */
public final class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 65536; // reasonable default
    private static final int KEY_LENGTH = 256; // bits
    private static final int SALT_LENGTH = 16; // bytes
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { /* utility */ }

    /**
     * Generate a salted PBKDF2 hash for the given password.
     * Returns a string in the format: iterations:saltBase64:hashBase64
     *
     * @param password plain text password
     * @return formatted stored password string
     * @throws Exception on cryptographic errors
     */
    public static String hashPassword(String password) throws Exception {
        Objects.requireNonNull(password, "password must not be null");
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, DEFAULT_ITERATIONS, KEY_LENGTH);
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(hash);
        return DEFAULT_ITERATIONS + ":" + saltB64 + ":" + hashB64;
    }

    /**
     * Verify a plain password against the stored value.
     *
     * @param password plain password to verify
     * @param stored   stored string in format iterations:saltBase64:hashBase64
     * @return true if password matches
     * @throws Exception on cryptographic errors or invalid stored format
     */
    public static boolean verifyPassword(String password, String stored) throws Exception {
        Objects.requireNonNull(password, "password must not be null");
        Objects.requireNonNull(stored, "stored must not be null");
        String[] parts = stored.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Stored password must have the format iterations:salt:hash");
        }
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

        byte[] actualHash = pbkdf2(password.toCharArray(), salt, iterations, expectedHash.length * 8);
        return slowEquals(expectedHash, actualHash);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        try {
            return skf.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Constant-time comparison to prevent timing attacks.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
