package dev.heartflame.fleet.model.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HardwareStatisticsModel {

    private double txBytes; // Transmitted bytes per second.
    private double rxBytes; // Received bytes per second.
    private double txPackets; // Transmitted packets per second;
    private double rxPackets; // Received packets per second.

    private long usedMemory;
    private long freeMemory;

    private double usedCPU; // Percentage of CPU usage.
}
