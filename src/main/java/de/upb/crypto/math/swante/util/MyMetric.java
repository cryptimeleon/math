package de.upb.crypto.math.swante.util;

import java.util.ArrayList;
import java.util.Collections;

public class MyMetric {
    
    private ArrayList<Double> measurements = new ArrayList<>();
    
    public void add(double newMeasurement) {
        measurements.add(newMeasurement);
    }
    
    
    public double computeAverage() {
        checkSizeNonZero();
        return sum() / measurements.size();
    }
    
    private void checkSizeNonZero() {
        if (measurements.size() == 0) {
            throw new RuntimeException("size cannot be zero");
        }
    }
    
    public double sum() {
        return measurements.stream().mapToDouble(it->it).sum();
    }
    
    public double computeMedian() {
        checkSizeNonZero();
        int size = measurements.size();
        return 0.5 * (measurements.get((size-1)/2)+measurements.get(size/2));
    }
}
