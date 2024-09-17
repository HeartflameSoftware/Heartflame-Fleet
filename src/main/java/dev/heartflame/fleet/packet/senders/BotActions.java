package dev.heartflame.fleet.packet.senders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.heartflame.fleet.model.s2c.S2CActionObject;
import dev.heartflame.fleet.util.ActionType;
import dev.heartflame.fleet.util.HLogger;
import dev.heartflame.fleet.util.RepeatingAction;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static dev.heartflame.fleet.bot.BotHandler.activeBots;

public class BotActions {

    public static List<RepeatingAction> schedules = new ArrayList<>();

    public static void preparse(S2CActionObject object) {
        ActionType actionType = toActionType(object);

        if (object.isRepeating()) {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

            String interval = object.getInterval();
            if (interval.contains("-")) {
                String[] parts = interval.split("-");

                int min = Integer.parseInt(parts[0]);
                int max = Integer.parseInt(parts[1]);

                schedules.add(new RepeatingAction(UUID.randomUUID().toString().substring(0, 5), service, interval, actionType, object.getAction(), service.scheduleAtFixedRate(() -> {
                    try {
                        parse(object, actionType, true);
                        Thread.sleep(ThreadLocalRandom.current().nextInt(max - min + 1) + min);
                    } catch (InterruptedException e) {
                        HLogger.error("Error when scheduling a bot action: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, 0, min, TimeUnit.MILLISECONDS)));
            } else {
                schedules.add(new RepeatingAction(UUID.randomUUID().toString().substring(0, 5), service, interval, actionType, object.getAction(), service.scheduleAtFixedRate(() -> {
                    try {
                        parse(object, actionType, true);
                        Thread.sleep(Integer.parseInt(interval));
                    } catch (InterruptedException e) {
                        HLogger.error("Error when scheduling a bot action: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, 0, 0, TimeUnit.MILLISECONDS)));
            }
        } else {
            parse(object, actionType, false);
        }
    }

    public static void parse(S2CActionObject object, ActionType actionType, boolean silent) {
        switch (object.getAction()) {

            case "CHAT":
                chat(object.getPayload(), actionType, silent);
                break;

            case "COMMAND":
                command(object.getPayload(), actionType, silent);
                break;

            case "DISCONNECT":
                disconnect(object.getPayload(), actionType, silent);
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
    

    public static void chat(String message, ActionType actionType, boolean silent) {

        switch (actionType) {
            case ALL:
                if (!silent) HLogger.info("Sending Chat Action packet for all bots.");

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
                if (randomEntry == null) return;

                String username = randomEntry.getKey();
                Session client = randomEntry.getValue();

                if (!silent) HLogger.info(String.format("Sending Chat Action packet for random bot [%s].", username));
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

                    if (!silent) HLogger.info(String.format("Sending Chat Action packet for bot [%s].", actionType.getUsername()));

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

    public static void disconnect(String reason, ActionType actionType, boolean silent) {

        switch (actionType) {
            case ALL:
                if (!silent) HLogger.info(String.format("Disconnecting all bots from server with reason [%s].", reason));

                for (Map.Entry<String, Session> entry : activeBots.entrySet()) {
                    Session client = entry.getValue();
                    client.disconnect(reason);
                    activeBots.remove(entry.getKey());
                }
                break;

            case RANDOM:
                Map.Entry<String, Session> randomEntry = getRandomEntry();
                if (randomEntry == null) return;

                String username = randomEntry.getKey();
                Session client = randomEntry.getValue();

                if (!silent) HLogger.info(String.format("Disconnecting random bot [%s] from server with reason [%s].", username, reason));

                client.disconnect(reason);
                break;

            case SINGLE:
                Session singleClient = activeBots.get(actionType.getUsername());
                if (singleClient != null) {

                    if (!silent) HLogger.info(String.format("Disconnecting bot [%s] from server with reason [%s].", actionType.getUsername(), reason));

                    singleClient.disconnect(reason);
                } else {
                    HLogger.error(String.format("Attempted to disconnect unknown bot [%s]!", actionType.getUsername()));
                }

                break;
        }
    }

    public static void command(String command, ActionType actionType, boolean silent) {
        switch (actionType) {
            case ALL:
                if (!silent) HLogger.info(String.format("Running command [%s] for all bots.", command));

                for (Map.Entry<String, Session> entry : activeBots.entrySet()) {
                    Session client = entry.getValue();

                    client.send(new ServerboundChatCommandPacket(command));
                }
                break;

            case RANDOM:
                Map.Entry<String, Session> randomEntry = getRandomEntry();
                if (randomEntry == null) return;

                Session client = randomEntry.getValue();

                if (!silent) HLogger.info(String.format("Executing command [%s] on random bot [%s].", command, randomEntry.getKey()));
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
        if (activeBots.size() <= 0) {
            HLogger.error("Attempted to get random bot when no bots are online!");
            return null;
        }

        return entrySet.stream().skip(new Random().nextInt(entrySet.size())).findFirst().get();
    }

    public static void cancel(String str) {
        try {
            HLogger.info(str);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(str);

            JsonNode tasksNode = jsonNode.get("tasks");
            List<String> tasks = new ArrayList<>();

            if (tasksNode.isArray()) {
                for (JsonNode task : tasksNode) {
                    tasks.add(task.textValue());
                }
            } else {
                tasks.add(tasksNode.textValue());
            }

            for (String task : tasks) {

                HLogger.info("Processing task ID: " + task);

                for (RepeatingAction action : schedules) {
                    if (action.getId().equals(task)) {
                        HLogger.warn(String.format("Cancelling task with ID of [%s]", task));
                        action.getTask().cancel(true);
                        action.getService().shutdownNow();
                    }
                }
            }
        } catch (Exception e) {
            HLogger.error(String.format("An error occurred while cancelling tasks: %s", e));
        }
    }
}
