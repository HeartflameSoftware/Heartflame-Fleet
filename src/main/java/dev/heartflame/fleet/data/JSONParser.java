package dev.heartflame.fleet.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import dev.heartflame.fleet.model.internal.PersistentDataStorageObject;
import dev.heartflame.fleet.util.HLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JSONParser {

    private static ObjectMapper mapper = new ObjectMapper();

    
    public static String fetchEndpoint() {
        return System.getenv("ENDPOINT") != null ? System.getenv("ENDPOINT") : "localhost";
    }

    public static int fetchSocketPort() {
        return Integer.parseInt(System.getenv("ENDPOINT_PORT") != null ? System.getenv("ENDPOINT_PORT") : "9999");
    }

    public static int fetchPrometheusPort() { return Integer.parseInt(System.getenv("PROMETHEUS_PORT") != null ? System.getenv("PROMETHEUS_PORT") : "9900"); }

    public static int fetchStatisticInterval() {
        return Integer.parseInt(System.getenv("STATISTIC_INTERVAL") != null ? System.getenv("STATISTIC_INTERVAL") : "10");
    }

    public static String fetchBasePath() {
        return System.getenv("BASE_PATH") != null ? System.getenv("BASE_PATH") : "/etc/heartflame/";
    }

    public static String fetchTruststorePath() {
        return System.getenv("TRUSTSTORE_PATH") != null ? System.getenv("TRUSTSTORE_PATH") : "/etc/heartflame/heartflame.truststore";
    }

    public static String fetchTruststorePassword() {
        return System.getenv("TRUSTSTORE_PASSWORD") != null ? System.getenv("TRUSTSTORE_PASSWORD") : "password123";
    }

    public static String fetchKeystorePath() {
        return System.getenv("KEYSTORE_PATH") != null ? System.getenv("KEYSTORE_PATH") : "/etc/heartflame/heartflame.keystore";
    }

    public static String fetchKeystorePassword() {
        return System.getenv("KEYSTORE_PASSWORD") != null ? System.getenv("KEYSTORE_PASSWORD") : "password123";
    }

    public static String fetchEncryptionKeyPath() { return System.getenv("ENCRYPTION_KEY_PATH") != null ? System.getenv("ENCRYPTION_KEY_PATH") : "/etc/heartflame/aead_keyset.json"; }


    public static String fetchDataStoragePath() { return System.getenv("PERSISTENT_DATA_STORAGE_PATH") != null ? System.getenv("PERSISTENT_DATA_STORAGE_PATH") : "/etc/heartflame/data.json"; }

    public static void writePersistentInterval(int interval) {
        try {
            File output = new File(fetchDataStoragePath());
            mapper.writeValue(output, new PersistentDataStorageObject(interval, getBotCount()));

            HLogger.info(String.format("Successfully updated node join interval to [%d]ms.", interval));
        } catch (IOException error) {
            HLogger.error(String.format("An error occurred when trying to write persistent node interval data: [%s]", error.getMessage()));
            error.printStackTrace();
        }
    }

    public static void writePersistentBotCount(int count) {
        try {
            File output = new File(fetchDataStoragePath());
            mapper.writeValue(output, new PersistentDataStorageObject(getJoinInterval(), count));

            HLogger.info(String.format("Successfully updated node bot count to [%d].", count));
        } catch (IOException error) {
            HLogger.error(String.format("An error occurred when trying to write persistent node interval data: [%s]", error.getMessage()));
            error.printStackTrace();
        }
    }

    public static int getJoinInterval() {
        try {
            JsonNode jsonNode = mapper.readTree(new File(fetchDataStoragePath()));

            if (!jsonNode.has("interval")) return -1;

            return jsonNode.get("interval").asInt();
        } catch (IOException error) {
            HLogger.error(String.format("An error occurred when trying to read the persistent data configuration file: [%s]", error.getMessage()));
            error.printStackTrace();
        }

        return -1;
    }

    public static int getBotCount() {
        if (!Files.exists(Paths.get(fetchDataStoragePath()))) {
            if (System.getenv("bot_count") == null) return -1;
            return Integer.valueOf(System.getenv("bot_count"));
        }

        try {
            JsonNode jsonNode = mapper.readTree(new File(fetchDataStoragePath()));
            if (!jsonNode.has("botCount")) return -1;

            return jsonNode.get("botCount").asInt();
        } catch (IOException error) {
            HLogger.error(String.format("An error occurred when trying to read the persistent data configuration file: [%s]", error.getMessage()));
            error.printStackTrace();
        }

        return -1;
    }
}
