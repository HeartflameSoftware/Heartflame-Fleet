package dev.heartflame.fleet.monitor;

import dev.heartflame.fleet.model.internal.HardwareStatisticsModel;
import dev.heartflame.fleet.monitor.network.NetworkMonitor;
import dev.heartflame.fleet.monitor.processor.CPUMonitor;
import dev.heartflame.fleet.monitor.ram.SystemMemoryMonitor;

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

    public static HardwareStatisticsModel gatherData() {
        AtomicReference<Double> txBytes = new AtomicReference<>((double) 0); // Transmitted bytes per second.
        AtomicReference<Double> rxBytes = new AtomicReference<>((double) 0); // Received bytes per second.
        AtomicReference<Double> txPackets = new AtomicReference<>((double) 0); // Transmitted packets per second;
        AtomicReference<Double> rxPackets = new AtomicReference<>((double) 0); // Received packets per second.

        long usedMemory = SystemMemoryMonitor.getUsedMemory();
        long freeMemory = SystemMemoryMonitor.getAvailableMemory();

        double usedCPU = CPUMonitor.processorLoad10sAvg();

        NetworkMonitor.networkAverages().forEach(((s, networkDataHolder) -> {
            txBytes.updateAndGet(v -> new Double((double) (v + networkDataHolder.txBytesPerSecond().mean())));
            rxBytes.updateAndGet(v -> new Double((double) (v + networkDataHolder.rxBytesPerSecond().mean())));
            txPackets.updateAndGet(v -> new Double((double) (v + networkDataHolder.txPacketsPerSecond().mean())));
            rxPackets.updateAndGet(v -> new Double((double) (v + networkDataHolder.rxPacketsPerSecond().mean())));
        }));

        return new HardwareStatisticsModel(txBytes.get(), rxBytes.get(), txPackets.get(), rxPackets.get(), usedMemory, freeMemory, usedCPU);
    }

    public static void init(int interval) {
        System.out.println("Initialising Node Statistic Monitoring.");
        CPUMonitor.enableMonitoring();
        NetworkMonitor.enableMonitoring(interval);
    }



}
