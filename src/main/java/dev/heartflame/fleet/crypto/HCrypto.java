package dev.heartflame.fleet.crypto;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.TinkJsonProtoKeysetFormat;
import com.google.crypto.tink.aead.AeadConfig;
import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.util.HLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Objects;

public class HCrypto {

    private static final String keyPath = JSONParser.fetchEncryptionKeyPath();

    public static byte[] decrypt(byte[] cipherText) {
        try {
            return Objects.requireNonNull(getAEAD()).decrypt(cipherText, new byte[0]);
        } catch (GeneralSecurityException error) {
            HLogger.error("An error occurred when decrypting data: " + error.getMessage());
            error.printStackTrace();
        }

        return new byte[0];
    }

    public static byte[] encrypt(String str) {
        try {
            byte[] plainText = str.getBytes(StandardCharsets.UTF_8);

            return Objects.requireNonNull(getAEAD()).encrypt(plainText, new byte[0]);
        } catch (GeneralSecurityException error) {
            HLogger.error("An error occurred when encrypting data: " + error.getMessage());
            error.printStackTrace();
        }

        return new byte[0];
    }

    private static Path getKeyFile() {
        Path path = Paths.get(keyPath);
        if (Files.exists(path)) {
            return path;
        } else {
            throw new IllegalArgumentException("Unknown key file path!");
        }
    }

    private static Aead getAEAD() {
        try {
            AeadConfig.register();

            KeysetHandle handle = TinkJsonProtoKeysetFormat.parseKeyset(new String(Files.readAllBytes(getKeyFile()), StandardCharsets.UTF_8), InsecureSecretKeyAccess.get());
            return handle.getPrimitive(Aead.class);
        } catch (GeneralSecurityException | IOException error) {
            HLogger.error("An error occurred when fetching the AEAD primitive: " + error.getMessage());
            error.printStackTrace();
        }

        return null;
    }

}
