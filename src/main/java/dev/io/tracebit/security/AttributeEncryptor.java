package dev.io.tracebit.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Converter
@Component
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKeySpec key;

    public AttributeEncryptor(@Value("${tracebit.encryption.key:#{environment.TRACEBIT_ENCRYPTION_KEY}}") String secretKey) {
        // Use a default key for development only if environment variable is not set
        if (secretKey == null || secretKey.equals("#{environment.TRACEBIT_ENCRYPTION_KEY}")) {
            secretKey = "audittrailkey123"; // Default key for backward compatibility
            log.warn("Using default encryption key. This is not secure for production!");
        }

        // Ensure key is exactly 16, 24, or 32 bytes (128, 192, or 256 bits)
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            // Use SHA-256 or another method to derive a proper key
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length > 32) {
                byte[] temp = new byte[32];
                System.arraycopy(keyBytes, 0, temp, 0, 32);
                keyBytes = temp;
            } else if (keyBytes.length < 16) {
                byte[] temp = new byte[16];
                System.arraycopy(keyBytes, 0, temp, 0, keyBytes.length);
                keyBytes = temp;
            }
        }
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plainText) {
        if (plainText == null) return null;
        try {
            // Generate a random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Initialize cipher with GCM parameters
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            // Encrypt the data
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine IV and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);

            // Encode as Base64 string
            return Base64.getEncoder().encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Error encrypting data", e);
            throw new RuntimeException("Encryption failed");
        }
    }

    @Override
    public String convertToEntityAttribute(String encrypted) {
        if (encrypted == null) return null;
        try {
            // Decode from Base64
            byte[] encryptedBytes = Base64.getDecoder().decode(encrypted);

            // Check if this is a legacy format (no IV)
            if (encryptedBytes.length < GCM_IV_LENGTH) {
                return legacyDecrypt(encrypted);
            }

            // Extract IV and encrypted data
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] encryptedData = new byte[byteBuffer.remaining()];
            byteBuffer.get(encryptedData);

            // Initialize cipher with GCM parameters
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encryptedData);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting data", e);
            try {
                // Try legacy decryption as fallback
                return legacyDecrypt(encrypted);
            } catch (Exception ex) {
                log.error("Legacy decryption also failed", ex);
                throw new RuntimeException("Decryption failed");
            }
        }
    }

    // Method to handle decryption of data encrypted with the old method
    private String legacyDecrypt(String encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)));
    }
}
