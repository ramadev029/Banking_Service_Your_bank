package com.rc.Bank_Service.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM Encryption Utility.
 * Encrypts PII fields (PAN tokens, Aadhaar Vault tokens) at rest in accordance with RBI & UIDAI security mandates.
 */
public class AESEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final String SECRET_KEY_STRING = "YourBankSuperSecretKey256Bit1234"; // 32-byte secret key

    public static String encrypt(String strToEncrypt) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTE];
            new SecureRandom().nextBytes(iv);

            SecretKey secretKey = new SecretKeySpec(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            byte[] messageWithIv = new byte[IV_LENGTH_BYTE + cipherText.length];

            System.arraycopy(iv, 0, messageWithIv, 0, IV_LENGTH_BYTE);
            System.arraycopy(cipherText, 0, messageWithIv, IV_LENGTH_BYTE, cipherText.length);

            return Base64.getEncoder().encodeToString(messageWithIv);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting PII data", e);
        }
    }

    public static String decrypt(String strToDecrypt) {
        try {
            byte[] decoded = Base64.getDecoder().decode(strToDecrypt);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH_BYTE);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = new byte[decoded.length - IV_LENGTH_BYTE];
            System.arraycopy(decoded, IV_LENGTH_BYTE, cipherText, 0, cipherText.length);

            byte[] decryptedText = cipher.doFinal(cipherText);
            return new String(decryptedText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting PII data", e);
        }
    }

    public static String maskAadhaar(String aadhaar) {
        if (aadhaar == null || aadhaar.length() < 4) return "XXXX-XXXX-XXXX";
        return "XXXX-XXXX-" + aadhaar.substring(aadhaar.length() - 4);
    }
}
