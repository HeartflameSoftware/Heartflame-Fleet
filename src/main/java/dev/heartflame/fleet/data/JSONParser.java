package dev.heartflame.fleet.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.heartflame.fleet.util.HLogger;

import java.io.File;
import java.io.IOException;

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

    public static void writePersistentDataFile(int interval) {
        try {
            String val = String.format("{\"interval\":\"%d\"}", interval);

            File output = new File(fetchDataStoragePath());
            mapper.writeValue(output, val);

            HLogger.info(String.format("Successfully updated node join interval to [%d]ms.", interval));
        } catch (IOException error) {
            HLogger.error(String.format("An error occurred when trying to write persistent node interval data: [%s]", error.getMessage()));
            error.printStackTrace();
        }
    }

    public static int getJoinInterval() {
        try {
            JsonNode jsonNode = mapper.readTree(new File(fetchDataStoragePath()));

            return jsonNode.get("interval").asInt();
        } catch (IOException error) {
            HLogger.error(String.format("An error occurred when trying to read the persistent data configuration file: [%s]", error.getMessage()));
            error.printStackTrace();
        }

        return 10000;
    }
}
