package dev.heartflame.fleet.data;

public interface DoubleAvgData {

    double mean();
    double max();
    double min();

    default double median() { return percentile(0.50d); }
    default double percentile95th() { return percentile(0.95d); }

    double percentile(double percentile);
}
