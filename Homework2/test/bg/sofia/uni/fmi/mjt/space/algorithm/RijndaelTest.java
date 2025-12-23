package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RijndaelTest {
    private SecretKey secretKey;
    private Rijndael rijndael;

    @BeforeEach
    void setUp() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        secretKey = keyGen.generateKey();
        rijndael = new Rijndael(secretKey);
    }

    @Test
    void testEncryptAndDecrypt() throws CipherException {
        String original = "Original data for test.";
        ByteArrayInputStream input = new ByteArrayInputStream(original.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();

        rijndael.encrypt(input, encrypted);

        ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encrypted.toByteArray());
        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();
        rijndael.decrypt(encryptedInput, decrypted);

        assertEquals(original, decrypted.toString(StandardCharsets.UTF_8));
    }

    @Test
    void testEncryptEmptyInput() throws CipherException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();

        rijndael.encrypt(input, encrypted);

        assertNotNull(encrypted.toByteArray());
    }

    @Test
    void testDecryptEmptyInput() throws CipherException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();

        rijndael.decrypt(input, decrypted);

        assertNotNull(decrypted.toByteArray());
        assertEquals(0, decrypted.size());
    }

    @Test
    void testNullSecretKeyThrowsException() {
        assertThrows(IllegalArgumentException.class,
            () -> new Rijndael(null));
    }

    @Test
    void testDecryptWithWrongKeyThrowsException() throws CipherException, NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        SecretKey wrongKey = keyGen.generateKey();

        String original = "Original data for test.";
        ByteArrayInputStream input = new ByteArrayInputStream(original.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();

        rijndael.encrypt(input, encrypted);

        Rijndael wrongRijndael = new Rijndael(wrongKey);
        ByteArrayInputStream encryptedInput = new ByteArrayInputStream(encrypted.toByteArray());
        ByteArrayOutputStream decrypted = new ByteArrayOutputStream();

        assertThrows(CipherException.class,
            () -> wrongRijndael.decrypt(encryptedInput, decrypted));
    }
}
