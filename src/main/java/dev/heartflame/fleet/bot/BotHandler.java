package dev.heartflame.fleet.bot;

import dev.heartflame.fleet.model.s2c.S2CStressObject;
import dev.heartflame.fleet.util.HLogger;
import org.geysermc.mcprotocollib.network.Session;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BotHandler {

    public static Map<String, Session> activeBots = Collections.synchronizedMap(new ConcurrentHashMap<>());
    public static int botCount = 0;

    public static void initialiseBots(S2CStressObject object) {
        HLogger.warn(String.format("Initializing stress test for [%d] Bots @ [%s:%d] with interval [%d]ms.", object.getCount(), object.getIp(), object.getPort(), object.getInterval()));
        int start = (object.getId() - 1) * object.getCount() + 1;
        int end = object.getId() * object.getCount();

        botCount = object.getCount();

        for (int i = start; i <= end; i++) {

            int botID = i;
            String name = object.getNameFormat().replace("%id%", String.valueOf(botID));
            CompletableFuture.runAsync(() -> {
                BotSession session = new BotSession();
                Session client = session.newSession(object.getIp(), object.getPort(), name);

                activeBots.put(name, client);
            });

        }
        HLogger.debug("Successfully initialized stress test.");
    }
}
