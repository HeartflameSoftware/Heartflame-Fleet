package dev.heartflame.fleet.monitor;

import dev.heartflame.fleet.bot.BotHandler;
import dev.heartflame.fleet.model.c2s.C2SStatisticsObject;
import dev.heartflame.fleet.monitor.network.NetworkMonitor;
import dev.heartflame.fleet.monitor.processor.CPUMonitor;
import dev.heartflame.fleet.monitor.ram.SystemMemoryMonitor;
import dev.heartflame.fleet.util.HLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public enum SystemMonitorExecutor {;
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
            return new C2SStatisticsObject(txBytes.get(), rxBytes.get(), txPackets.get(), rxPackets.get(), usedMemory, freeMemory, totalMemory, usedCPU, currentBotCount, (BotHandler.botCount - currentBotCount));
        });
    }

    public static void init(int interval) {
        HLogger.info("Initialising Node Statistic Monitoring.");
        CPUMonitor.enableMonitoring();
        NetworkMonitor.enableMonitoring(interval);
    }



}
