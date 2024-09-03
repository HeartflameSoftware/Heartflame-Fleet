package dev.heartflame.fleet.crypto;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.InsecureSecretKeyAccess;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.TinkJsonProtoKeysetFormat;
import com.google.crypto.tink.aead.AeadConfig;
import dev.heartflame.fleet.data.JSONParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public class HCrypto {

    private static final String keyPath = JSONParser.fetchEncryptionKeyPath();

    public static byte[] decrypt(byte[] cipherText) throws GeneralSecurityException, IOException {
        byte[] plaintext = getAEAD().decrypt(cipherText, new byte[0]);
        return plaintext;
    }

    public static byte[] encrypt(String str) throws GeneralSecurityException, IOException {
        byte[] plainText = str.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = getAEAD().encrypt(plainText, new byte[0]);

        return cipherText;
    }

    private static Path getKeyFile() {
        Path path = Paths.get(keyPath);
        if (Files.exists(path)) {
            return path;
        } else {
            throw new IllegalArgumentException("Unknown key file path!");
        }
    }

    private static Aead getAEAD() throws GeneralSecurityException, IOException {
        AeadConfig.register();

        KeysetHandle handle = TinkJsonProtoKeysetFormat.parseKeyset(new String(Files.readAllBytes(getKeyFile()), StandardCharsets.UTF_8), InsecureSecretKeyAccess.get());
        return handle.getPrimitive(Aead.class);
    }

}
