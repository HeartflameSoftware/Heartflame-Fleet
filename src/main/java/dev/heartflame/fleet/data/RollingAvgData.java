package dev.heartflame.fleet.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class RollingAvgData implements DoubleAvgData {

    private final Queue<BigDecimal> samples;
    private final int dataWindow;
    private BigDecimal total = BigDecimal.ZERO;

    public RollingAvgData(int dataWindow) {
        this.dataWindow = dataWindow;
        this.samples = new ArrayDeque<>(this.dataWindow + 1);
    }

    public int getSamples() {
        synchronized (this) { return this.samples.size(); }
    }

    public void add(BigDecimal amount) {
        synchronized (this) {
            this.total = this.total.add(amount);
            this.samples.add(amount);
            if (this.samples.size() > this.dataWindow) {
                this.total = this.total.subtract(this.samples.remove());
            }
        }
    }

    @Override
    public double mean() {
        synchronized (this) {
            if (this.samples.isEmpty()) {
                return 0;
            }

            BigDecimal div = BigDecimal.valueOf(this.samples.size());
            return this.total.divide(div, 30, RoundingMode.HALF_UP).doubleValue();
        }
    }

    @Override
    public double max() {
        synchronized (this) {
            BigDecimal max = null;
            for (BigDecimal sm : this.samples) {
                if (max == null || sm.compareTo(max) > 0) {
                    max = sm;
                }
            }

            return max == null ? 0 : max.doubleValue();
        }
    }

    @Override
    public double min() {
        synchronized (this) {
            BigDecimal min = null;
            for (BigDecimal sample : this.samples) {
                if (min == null || sample.compareTo(min) < 0) {
                    min = sample;
                }
            }
            return min == null ? 0 : min.doubleValue();
        }
    }

    @Override
    public double percentile(double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException("Invalid percentile " + percentile);
        }

        BigDecimal[] sortedSamples;
        synchronized (this) {
            if (this.samples.isEmpty()) {
                return 0;
            }
            sortedSamples = this.samples.toArray(new BigDecimal[0]);
        }
        Arrays.sort(sortedSamples);

        int rank = (int) Math.ceil(percentile * (sortedSamples.length - 1));
        return sortedSamples[rank].doubleValue();
    }
}
