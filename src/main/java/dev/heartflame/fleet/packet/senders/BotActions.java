package dev.heartflame.fleet.packet.senders;

import dev.heartflame.fleet.model.s2c.S2CActionObject;
import dev.heartflame.fleet.util.ActionType;
import dev.heartflame.fleet.util.HLogger;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;


import javax.swing.*;
import java.time.Instant;
import java.util.*;

import static dev.heartflame.fleet.bot.BotHandler.activeBots;

public class BotActions {

    public static void parse(S2CActionObject object) {
        ActionType actionType = toActionType(object);
        switch (object.getAction()) {

            case "CHAT":
                chat(object.getPayload(), actionType);
                break;

            case "COMMAND":
                command(object.getPayload(), actionType);
                break;

            case "DISCONNECT":
                disconnect(object.getPayload(), actionType);
                break;

        }
    }

    public static ActionType toActionType(S2CActionObject object) {
        switch (object.getBot()) {

            case "ALL":
                return ActionType.ALL;

            case "RANDOM":
                return ActionType.RANDOM;

            default:
                ActionType ac = ActionType.SINGLE;
                ac.setUsername(object.getBot());
                return ac;
        }
    }
    

    public static void chat(String message, ActionType actionType) {

        switch (actionType) {
            case ALL:
                HLogger.info("Sending Chat Action packet for all bots.");

                for (Map.Entry<String, Session> entry : activeBots.entrySet()) {
                    Session client = entry.getValue();

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

                HLogger.info(String.format("Sending Chat Action packet for random bot [%s].", username));
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
                Session singleClient = activeBots.get(actionType.getUsername());
                if (singleClient != null) {

                    HLogger.info(String.format("Sending Chat Action packet for bot [%s].", actionType.getUsername()));

                    singleClient.send(new ServerboundChatPacket(
                            message, // Message to be sent.
                            Instant.now().toEpochMilli(), // Message timestamp.
                            0L, // message salt
                            null, // No signature.
                            0, // Offset
                            new BitSet() // Acknowledged messages.
                    ));
                } else {
                    HLogger.error(String.format("Attempted to send Chat Action packet [%s] for unknown bot [%s]!", message, actionType.getUsername()));
                }
                break;
        }
    }

    public static void disconnect(String reason, ActionType actionType) {

        switch (actionType) {
            case ALL:
                HLogger.info(String.format("Disconnecting all bots from server with reason [%s].", reason));

                for (Map.Entry<String, Session> entry : activeBots.entrySet()) {
                    Session client = entry.getValue();
                    client.disconnect(reason);
                }
                break;

            case RANDOM:
                Map.Entry<String, Session> randomEntry = getRandomEntry();

                String username = randomEntry.getKey();
                Session client = randomEntry.getValue();

                HLogger.info(String.format("Disconnecting random bot [%s] from server with reason [%s].", username, reason));

                client.disconnect(reason);
                break;

            case SINGLE:
                Session singleClient = activeBots.get(actionType.getUsername());
                if (singleClient != null) {

                    HLogger.info(String.format("Disconnecting bot [%s] from server with reason [%s].", actionType.getUsername(), reason));

                    singleClient.disconnect(reason);
                } else {
                    HLogger.error(String.format("Attempted to disconnect unknown bot [%s]!", actionType.getUsername()));
                }

                break;
        }
    }

    public static void command(String command, ActionType actionType) {
        switch (actionType) {
            case ALL:
                HLogger.info(String.format("Running command [%s] for all bots.", command));

                for (Map.Entry<String, Session> entry : activeBots.entrySet()) {
                    Session client = entry.getValue();

                    client.send(new ServerboundChatCommandPacket(command));
                }
                break;

            case RANDOM:
                Map.Entry<String, Session> randomEntry = getRandomEntry();
                Session client = randomEntry.getValue();

                HLogger.info(String.format("Executing command [%s] on random bot [%s].", command, randomEntry.getKey()));
                client.send(new ServerboundChatCommandPacket(command));

                break;

            case SINGLE:
                Session singleClient = activeBots.get(actionType.getUsername());
                if (singleClient != null) {

                    HLogger.info(String.format("Executing command [%s] on bot [%s].", command, actionType.getUsername()));
                    singleClient.send(new ServerboundChatCommandPacket(command));

                } else {
                    HLogger.error(String.format("Attempted to run command [%s] for unknown bot [%s]!", command, actionType.getUsername()));
                }

                break;

        }
    }

    private static Map.Entry<String, Session> getRandomEntry() {
        Set<Map.Entry<String, Session>> entrySet = activeBots.entrySet();

        return entrySet.stream().skip(new Random().nextInt(entrySet.size())).findFirst().get();
    }
}
