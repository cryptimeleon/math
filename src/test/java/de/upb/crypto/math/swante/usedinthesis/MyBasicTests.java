package de.upb.crypto.math.swante.usedinthesis;

import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.MyTestUtils;
import de.upb.crypto.math.swante.util.MyMetric;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;

import static de.upb.crypto.math.swante.misc.pln;

public class MyBasicTests {
    
    int numSuperIterations = 5;
    int numWarmUpIterations = 5;
    int numValues = 1000 * 1000;
    int minBitLengthPower = 4;
    int maxBitLengthPower = 8;
    
    @Test
    public void testFieldOperations() {
        pln("=========================");
        pln("Running field operation performance tests");
        for (int iMeta = 0; iMeta < 2; iMeta++) {
            ArrayList<Double> results = new ArrayList<Double>();
            pln("meta iteration " + iMeta);
            for (int bitLengthPower = minBitLengthPower; bitLengthPower <= maxBitLengthPower; bitLengthPower++) {
                int bitLength = 1 << bitLengthPower;
                pln("=========================");
                pln("Bit Length = " + bitLength);
                BigInteger p = MyTestUtils.createPrimeWithGivenBitLength(bitLength);
                Zp zp = new Zp(p);
                MyMetric metric = new MyMetric("basic field operation costs for bitLength=" + bitLength);
                for (int iter = -numWarmUpIterations; iter < numSuperIterations; iter++) {
                    Zp.ZpElement[] A = MyTestUtils.createRandomZpValues(zp, numValues);
                    Zp.ZpElement[] B = MyTestUtils.createRandomZpValues(zp, numValues);
                    double startMillis = System.nanoTime() / 1.0e6;
                    for (int i = 0; i < numValues; i++) {
                        A[i].add(B[i]);
//                    A[i].sub(B[i]);
//                    A[i].square();
//                    A[i].mul(B[i]);
//                    A[i].inv();
                    }
                    double elapsedMillis = System.nanoTime() / 1.0e6 - startMillis;
                    if (iter >= 0) {
                        metric.add(elapsedMillis);
                    }
                    pln(String.format("bitLength=%d, iteration=%d, #values=%d, time=%.1f ms", bitLength, iter, numValues, elapsedMillis));
                }
//                pln(metric);
                results.add(metric.computeMedian());
            }
            pln("=====================");
            pln("Final results");
            pln("=====================");
            pln("<bit length power>\t<elapsed millis>");
            for (int bitLengthPower = minBitLengthPower; bitLengthPower <= maxBitLengthPower; bitLengthPower++) {
                pln(bitLengthPower + "\t" + results.get(bitLengthPower - minBitLengthPower));
            }
        }
    }
    
}