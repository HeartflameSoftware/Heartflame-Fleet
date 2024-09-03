package dev.heartflame.fleet.packet.senders;

import dev.heartflame.fleet.util.ActionType;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BotActions {
    private static final Logger log = LoggerFactory.getLogger("Bot Actions");
    public static Map<String, Session> activeBots = Collections.synchronizedMap(new ConcurrentHashMap<>());

    public static void chat(String message, ActionType actionType) {

        switch (actionType) {
            case ALL:

                for (Map.Entry<String, Session> entry : activeBots.entrySet()) {
                    Session client = entry.getValue();
                    log.info("Sending Chat Action packet for: {}", entry.getKey());

                    client.send(new ServerboundChatPacket(
                            message, // Message to be sent.
                            Instant.now().toEpochMilli(), // Message timestamp.
                            0L, // message salt
                            null, // No signature.
                            0, // Offset
                            new BitSet() // Acknowledged messages.
                    ));
                }
                break;

            case RANDOM:
                Map.Entry<String, Session> randomEntry = getRandomEntry();

                String username = randomEntry.getKey();
                Session client = randomEntry.getValue();

                log.info("Sending Chat Action packet for: {}", username);
                client.send(new ServerboundChatPacket(
                        message, // Message to be sent.
                        Instant.now().toEpochMilli(), // Message timestamp.
                        0L, // message salt
                        null, // No signature.
                        0, // Offset
                        new BitSet() // Acknowledged messages.
                ));
                break;

            case SINGLE:
                Session singleClient = activeBots.get(ActionType.SINGLE.getUsername());
                if (singleClient != null) {

                    log.info("Sending Chat Action packet for: {}", ActionType.SINGLE.getUsername());

                    singleClient.send(new ServerboundChatPacket(
                            message, // Message to be sent.
                            Instant.now().toEpochMilli(), // Message timestamp.
                            0L, // message salt
                            null, // No signature.
                            0, // Offset
                            new BitSet() // Acknowledged messages.
                    ));
                }
                break;
        }
    }

    public static void disconnect(String reason, ActionType actionType) {

        switch (actionType) {
            case ALL:
                log.info("Disconnecting all bots from server with reason: {}", reason);

                for (Map.Entry<String, Session> entry : activeBots.entrySet()) {
                    Session client = entry.getValue();
                    client.disconnect(reason);
                }
                break;

            case RANDOM:
                Map.Entry<String, Session> randomEntry = getRandomEntry();

                String username = randomEntry.getKey();
                Session client = randomEntry.getValue();

                log.info("Disconnecting {} from server with reason: {}", username, reason);

                client.disconnect(reason);
                break;

            case SINGLE:
                Session singleClient = activeBots.get(ActionType.SINGLE.getUsername());
                if (singleClient != null) {

                    log.info("Disconnecting {} from server with reason: {}", ActionType.SINGLE.getUsername(), reason);

                    singleClient.disconnect(reason);
                }

                break;
        }
    }

    private static Map.Entry<String, Session> getRandomEntry() {
        Set<Map.Entry<String, Session>> entrySet = activeBots.entrySet();
        Map.Entry<String, Session> randomEntry = entrySet.stream().skip(new Random().nextInt(entrySet.size())).findFirst().get();

        return randomEntry;
    }
}
