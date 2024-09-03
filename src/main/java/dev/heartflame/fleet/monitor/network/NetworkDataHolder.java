package dev.heartflame.fleet.monitor.network;

import dev.heartflame.fleet.data.RollingAvgData;

import java.math.BigDecimal;

public final class NetworkDataHolder {

    private final RollingAvgData rxBytesPerSecond;
    private final RollingAvgData txBytesPerSecond;
    private final RollingAvgData rxPacketsPerSecond;
    private final RollingAvgData txPacketsPerSecond;

    NetworkDataHolder(int dataWindow) {
        this.rxBytesPerSecond = new RollingAvgData(dataWindow);
        this.txBytesPerSecond = new RollingAvgData(dataWindow);
        this.rxPacketsPerSecond = new RollingAvgData(dataWindow);
        this.txPacketsPerSecond = new RollingAvgData(dataWindow);
    }

    void accept(NetworkInterfaceData data, TransferRateCalculator transferRateCalculator) {
        this.rxBytesPerSecond.add(transferRateCalculator.calculate(data.getRxBytes()));
        this.txBytesPerSecond.add(transferRateCalculator.calculate(data.getTxBytes()));
        this.rxPacketsPerSecond.add(transferRateCalculator.calculate(data.getRxPackets()));
        this.txPacketsPerSecond.add(transferRateCalculator.calculate(data.getTxPackets()));
    }

    interface TransferRateCalculator {
        BigDecimal calculate(long value);
    }

    public RollingAvgData byteRate(NetworkInterfaceData.Direction direction) {
        switch (direction) {
            case RECEIVE:
                return rxBytesPerSecond();
            case TRANSMIT:
                return txBytesPerSecond();
            default:
                throw new AssertionError();
        }
    }

    public RollingAvgData packetRate(NetworkInterfaceData.Direction direction) {
        switch (direction) {
            case RECEIVE:
                return rxPacketsPerSecond();
            case TRANSMIT:
                return txPacketsPerSecond();
            default:
                throw new AssertionError();
        }
    }

    public RollingAvgData rxBytesPerSecond() {
        return this.rxBytesPerSecond;
    }

    public RollingAvgData rxPacketsPerSecond() {
        return this.rxPacketsPerSecond;
    }

    public RollingAvgData txBytesPerSecond() {
        return this.txBytesPerSecond;
    }

    public RollingAvgData txPacketsPerSecond() {
        return this.txPacketsPerSecond;
    }
}
