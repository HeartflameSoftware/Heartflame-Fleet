package dev.heartflame.fleet.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JSONParser {

    private static ObjectMapper mapper = new ObjectMapper();

    public static JsonNode read() {
        System.out.println("Reading JSON Configuration file...");
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
}
