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

    public static JsonNode read() {
        try {
            return mapper.readTree(new File("config.json"));
        } catch (IOException error) {
            error.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public static String fetchEndpoint() {
        return read().get("endpoint").asText();
    }

    public static int fetchSocketPort() {
        return read().get("endpoint-port").asInt();
    }

    public static int fetchStatisticInterval() {
        return read().get("statistic-interval").asInt();
    }

    public static String fetchTruststorePath() {
        return read().get("truststore-path").asText();
    }

    public static String fetchTruststorePassword() {
        return read().get("truststore-password").asText();
    }

    public static String fetchKeystorePath() {
        return read().get("keystore-path").asText();
    }

    public static String fetchKeystorePassword() {
        return read().get("keystore-password").asText();
    }

    public static String fetchEncryptionKeyPath() { return read().get("encryption-key-path").asText(); }

    public static String fetchDataStoragePath() { return read().get("persistent-data-storage-path").asText(); }

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
