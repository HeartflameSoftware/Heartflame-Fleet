package dev.heartflame.fleet.model.c2s;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class C2SStatisticsObject {

    private double txBytes; // Transmitted bytes per second.
    private double rxBytes; // Received bytes per second.
    private double txPackets; // Transmitted packets per second;
    private double rxPackets; // Received packets per second.

    private long usedMemory;
    private long freeMemory;
    private long totalMemory;

    private double usedCPU; // Percentage of CPU usage.

    private int online; // Amount of online bots
    private int remaining; // Total bots (including scheduled to join) of that node.

}
