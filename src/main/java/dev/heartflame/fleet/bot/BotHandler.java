package dev.heartflame.fleet.bot;

import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.model.s2c.S2CStressObject;
import dev.heartflame.fleet.util.HLogger;
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
    public static S2CStressObject ongoingStress = null;

    public static void initialiseBots(S2CStressObject object) {
        ongoingStress = object;
        int joinInterval = getInterval(ongoingStress);
        HLogger.warn(String.format("Initializing stress test for [%d] Bots @ [%s:%d] with interval [%d]ms.", ongoingStress.getCount(), ongoingStress.getIp(), ongoingStress.getPort(), joinInterval));

        int start = (ongoingStress.getId() - 1) * ongoingStress.getCount() + 1;
        int end = ongoingStress.getId() * ongoingStress.getCount();

        botCount = ongoingStress.getCount();

        for (int i = start; i <= end; i++) {

            if (cancel) {
                HLogger.warn("Detected cancel signal. Terminating stress test.");
                cancel = false;
                ongoingStress = null;
                return;
            }

            int botID = i;
            String name = ongoingStress.getNameFormat().replace("%id%", String.valueOf(botID));
            CompletableFuture.runAsync(() -> {
                BotSession session = new BotSession();
                Session client = session.newSession(ongoingStress.getIp(), ongoingStress.getPort(), name);

                activeBots.put(name, client);
                try {
                    Thread.sleep(joinInterval);
                } catch (InterruptedException e) {
                    HLogger.warn("Encountered an exception when sleeping! [" + e.getMessage() + "]");
                }
            });

        }
        HLogger.debug("Successfully initialized stress test. All bots online.");
    }

    private static int getInterval(S2CStressObject object) {
        if (Files.exists(Paths.get(JSONParser.fetchDataStoragePath()))) {
            return JSONParser.getJoinInterval();
        } else return object.getInterval();
    }
}
