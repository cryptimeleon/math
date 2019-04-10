package de.upb.crypto.math.swante.util;

import java.util.ArrayList;

public class MyMetric {
    
    private ArrayList<Double> measurements = new ArrayList<>();
    private String metricName;
    
    public MyMetric() {
        this("<undefined metric name>");
    }
    
    public MyMetric(String metricName) {
        this.metricName = metricName;
    }
    
    public void add(double newMeasurement) {
        measurements.add(newMeasurement);
        measurements.sort(Double::compareTo);
    }
    
    
    public double computeAverage() {
        checkSizeNonZero();
        return sum() / size();
    }
    
    private void checkSizeNonZero() {
        if (size() == 0) {
            throw new RuntimeException("size cannot be zero");
        }
    }
    
    public double sum() {
        return measurements.stream().mapToDouble(it -> it).sum();
    }
    
    public double computeMedian() {
        checkSizeNonZero();
        int size = size();
        return 0.5 * (measurements.get((size - 1) / 2) + measurements.get(size / 2));
    }
    
    double computeVariance() {
        double mean = computeAverage();
        double variance = 0;
        for (double a : measurements) {
            variance += (a - mean) * (a - mean);
        }
        return variance / (size());
    }
    
    double computeStdDev() {
        return Math.sqrt(computeVariance());
    }
    
    @Override
    public String toString() {
        String res = "=== " + metricName + " ===\n";
        res += String.format("Size    : %d\n", size());
        res += String.format("Sum     : %.1f\n", sum());
        res += String.format("Average : %.1f\n", computeAverage());
        res += String.format("Std-Dev : %.1f\n", computeStdDev());
        res += String.format("Median  : %.1f", computeMedian());
        return res;
    }
    
    public int size() {
        return measurements.size();
    }
}
