package dev.heartflame.fleet.bot;

import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.model.s2c.S2CStressObject;
import dev.heartflame.fleet.packet.senders.BotActions;
import dev.heartflame.fleet.util.HLogger;
import dev.heartflame.fleet.util.RepeatingAction;
import org.geysermc.mcprotocollib.network.Session;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BotHandler {

    public static Map<String, Session> activeBots = Collections.synchronizedMap(new ConcurrentHashMap<>());
    public static int botCount = 0;
    public static boolean cancel = false;
    public static boolean completed = false;
    public static S2CStressObject ongoingStress = null;

    public static void initialiseBots(S2CStressObject object) {
       CompletableFuture.runAsync(() -> {
           ongoingStress = object;
           if (object.isOverrideCount()) {
               botCount = object.getBotCount();
           } else {
               botCount = JSONParser.getBotCount();
           }

           int joinInterval = getInterval(ongoingStress);
           HLogger.warn(String.format("Initializing stress test for [%d] Bots @ [%s:%d] with interval [%d]ms.", botCount, ongoingStress.getIp(), ongoingStress.getPort(), joinInterval));

           for (int i = 1; i <= botCount; i++) {

               if (cancel) {
                   HLogger.warn("Detected cancel signal. Terminating stress test.");
                   cancel();
                   return;
               }

               String name = ongoingStress.getNameFormat().replace("%bot_id%", String.valueOf(i)).replace("%node_id%", String.valueOf(ongoingStress.getId()));
               CompletableFuture.runAsync(() -> {
                   BotSession session = new BotSession();
                   Session client = session.newSession(ongoingStress.getIp(), ongoingStress.getPort(), name);

                   activeBots.put(name, client);
               });

               try {
                   Thread.sleep(getInterval(ongoingStress));
               } catch (InterruptedException e) {
                   HLogger.warn("Encountered an exception when sleeping! [" + e.getMessage() + "]");
               }

           }
           completed = true;
           HLogger.debug("Successfully initialized stress test. All bots online.");
       });
    }

    public static void continueLoop(int newCount) {
        CompletableFuture.runAsync(() -> {
            for (int i = (botCount + 1); i <= newCount; i++) {
                if (cancel) {
                    HLogger.warn("Detected cancel signal. Terminating stress test.");
                    cancel();
                    return;
                }

                String name = ongoingStress.getNameFormat().replace("%bot_id%", String.valueOf(i)).replace("%node_id%", String.valueOf(ongoingStress.getId()));
                CompletableFuture.runAsync(() -> {
                    BotSession session = new BotSession();
                    Session client = session.newSession(ongoingStress.getIp(), ongoingStress.getPort(), name);

                    activeBots.put(name, client);
                });

                try {
                    Thread.sleep(getInterval(ongoingStress));
                } catch (InterruptedException e) {
                    HLogger.warn("Encountered an exception when sleeping! [" + e.getMessage() + "]");
                }
            }
        });
    }

    private static void cancel() {
        BotHandler.cancel = false; // Reset the cancel variable
        BotHandler.botCount = 0; // Reset the total bot count
        BotHandler.ongoingStress = null; // clear the ongoing stress object
        BotHandler.completed = false;
        BotHandler.activeBots.clear(); // empty the active bots map

        for (RepeatingAction task : BotActions.schedules) {
            HLogger.info("CANCELLING: " + task.getId());

            task.getTask().cancel(false);
            BotActions.schedules.remove(task);
        }
    }

    private static int getInterval(S2CStressObject object) {
        if (Files.exists(Paths.get(JSONParser.fetchDataStoragePath()))) {
            int inter = JSONParser.getJoinInterval();
            if (inter == -1) return object.getInterval();

            return inter;
        } else return object.getInterval();
    }
}
