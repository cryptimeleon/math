package de.upb.crypto.math.swante.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MyMetricTests {
    
    @Test
    public void testMetric() {
        MyMetric a = new MyMetric();
        a.add(2.0);
        a.add(11.0);
        a.add(3.0);
        a.add(4.0);
        assertEquals(20.0, a.sum(), 0.00001);
        assertEquals(3.5, a.computeMedian(), 0.00001);
        assertEquals(5.0, a.computeAverage(), 0.00001);
        System.out.println(a);
    }
    
}