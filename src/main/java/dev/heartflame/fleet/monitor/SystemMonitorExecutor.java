package dev.heartflame.fleet.monitor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.heartflame.fleet.bot.BotHandler;
import dev.heartflame.fleet.data.JSONParser;
import dev.heartflame.fleet.model.c2s.C2SStatisticsObject;
import dev.heartflame.fleet.monitor.network.NetworkMonitor;
import dev.heartflame.fleet.monitor.processor.CPUMonitor;
import dev.heartflame.fleet.monitor.ram.SystemMemoryMonitor;
import dev.heartflame.fleet.packet.senders.BotActions;
import dev.heartflame.fleet.util.ActionType;
import dev.heartflame.fleet.util.HLogger;
import dev.heartflame.fleet.util.RepeatingAction;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public enum SystemMonitorExecutor {;

    private static ObjectMapper mapper = new ObjectMapper();

    public static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = Executors.defaultThreadFactory().newThread(r);
        thread.setName("heartflame-monitoring-thread");
        thread.setDaemon(true);
        return thread;
    });

    public static CompletableFuture<C2SStatisticsObject> gatherData() {
        return CompletableFuture.supplyAsync(() -> {
            AtomicReference<Double> txBytes = new AtomicReference<>((double) 0); // Transmitted bytes per second.
            AtomicReference<Double> rxBytes = new AtomicReference<>((double) 0); // Received bytes per second.
            AtomicReference<Double> txPackets = new AtomicReference<>((double) 0); // Transmitted packets per second;
            AtomicReference<Double> rxPackets = new AtomicReference<>((double) 0); // Received packets per second.

            long usedMemory = SystemMemoryMonitor.getUsedMemory();
            long freeMemory = SystemMemoryMonitor.getAvailableMemory();
            long totalMemory = SystemMemoryMonitor.getTotalMemory();

            double usedCPU = CPUMonitor.processorLoad10sAvg();

            NetworkMonitor.networkAverages().forEach(((s, networkDataHolder) -> {
                txBytes.updateAndGet(v -> new Double((double) (v + networkDataHolder.txBytesPerSecond().mean())));
                rxBytes.updateAndGet(v -> new Double((double) (v + networkDataHolder.rxBytesPerSecond().mean())));
                txPackets.updateAndGet(v -> new Double((double) (v + networkDataHolder.txPacketsPerSecond().mean())));
                rxPackets.updateAndGet(v -> new Double((double) (v + networkDataHolder.rxPacketsPerSecond().mean())));
            }));

            int currentBotCount = BotHandler.activeBots.size();
            String json = tasksToJSON();
            return new C2SStatisticsObject(txBytes.get(), rxBytes.get(), txPackets.get(), rxPackets.get(), usedMemory, freeMemory, totalMemory, usedCPU, currentBotCount, (BotHandler.botCount - currentBotCount), JSONParser.getBotCount(), JSONParser.getJoinInterval(), json);
        });
    }

    public static String tasksToJSON() {
        try {
            ArrayNode array = mapper.createArrayNode();
            for (RepeatingAction action : BotActions.schedules) {
                ObjectNode obj = array.addObject();
                obj.put("label", String.format("%s-%s [%s]", action.getAction(), action.getActionType().toString(), action.getId()));
                obj.put("value", action.getId());
                obj.put("id", action.getId());
                obj.put("interval", action.getInterval());
                obj.put("getType", action.getActionType().toString());
            }

            String jsonArray = mapper.writeValueAsString(array);

            return jsonArray;
        } catch (IOException error) {
            HLogger.error("Error when trying to convert repeating tasks to JSON: " + error.getMessage());
            error.printStackTrace();

            return "[]";
        }
    }

    public static void init(int interval) {
        HLogger.info("Initialising Node Statistic Monitoring.");
        CPUMonitor.enableMonitoring();
        NetworkMonitor.enableMonitoring(interval);
    }



}
