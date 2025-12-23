package bg.sofia.uni.fmi.mjt.space.algorithm;

import bg.sofia.uni.fmi.mjt.space.exception.CipherException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.InputStream;
import java.io.OutputStream;

public class Rijndael implements SymmetricBlockCipher {
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int KILOBYTE = 1024;
    private final SecretKey secretKey;

    /**
     * Encrypts/decrypts data using AES (Rijndael) algorithm with the provided secret key.
     *
     * @param secretKey the encryption/decryption key
     * @throws IllegalArgumentException if secretKey is null
     */
    public Rijndael(SecretKey secretKey) {
        if (secretKey == null) {
            throw new IllegalArgumentException("Secret key can't be null");
        }
        this.secretKey = secretKey;
    }

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            try (CipherOutputStream cos = new CipherOutputStream(outputStream, cipher)) {
                byte[] buffer = new byte[KILOBYTE];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    cos.write(buffer, 0, read);
                }
            }
        } catch (Exception e) {
            throw new CipherException("Encryption failed", e);
        }
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) throws CipherException {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            try (CipherInputStream cis = new CipherInputStream(inputStream, cipher)) {
                byte[] buffer = new byte[KILOBYTE];
                int read;
                while ((read = cis.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
        } catch (Exception e) {
            throw new CipherException("Decryption failed", e);
        }
    }
}

