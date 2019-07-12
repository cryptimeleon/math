package de.upb.crypto.math.swante.usedinthesis;

import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.MyTestUtils;
import de.upb.crypto.math.swante.util.MyMetric;
import org.junit.Test;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.misc.pln;

public class MyBasicTests {
    
    int numSuperIterations = 5;
    int numWarmUpIterations = 2;
    int[] bitLengths = new int[]{32, 64, 128, 256, 512, 1024};
    int numValues = 1000 * 1000;
    
    @Test
    public void testFieldOperations() {
        pln("=========================");
        pln("Running field squaring performance tests");
        MyMetric metric = new MyMetric("basic field operation costs");
        for (int bitLength : bitLengths) {
            BigInteger p = MyTestUtils.createPrimeWithGivenBitLength(bitLength);
            Zp zp = new Zp(p);
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
            pln(metric);
        }
    }
    
}