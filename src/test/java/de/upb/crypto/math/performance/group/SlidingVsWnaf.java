package de.upb.crypto.math.performance.group;

import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.evaluator.OptGroupElementExpressionEvaluatorConfig;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.GroupPrecomputationsFactory;
import de.upb.crypto.math.pairings.mcl.MclGroup1;
import de.upb.crypto.math.performance.expressions.ExpressionGenerator;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public class SlidingVsWnaf {
    public static void main(String[] args) {
        int numRuns = 40;
        int numWarmups = 5;
        BilinearGroupFactory fac = new BilinearGroupFactory(60);
        fac.setRequirements(BilinearGroup.Type.TYPE_3);
        BilinearGroup bilGroup = fac.createBilinearGroup();
        //Group group = bilGroup.getG1();
        Group group = new MclGroup1();
        int numElems = 200;
        GroupElement[] bases = new GroupElement[numElems];
        Zn.ZnElement[] exponents = new Zn.ZnElement[numElems];
        for (int l = 0; l < 5; ++l) {
            for (int i = 0; i < bases.length; ++i) {
                bases[i] = group.getUniformlyRandomNonNeutral();
                exponents[i] = group.getUniformlyRandomExponent();
            }
            long[] mclTimes = new long[numRuns];
            long[] wnafTimes = new long[numRuns];
            long[] slidingTimes = new long[numRuns];
            for (int k = 0; k < numRuns + numWarmups; ++k) {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < bases.length; ++i) {
                    bases[i].pow(exponents[i]);
                }
                if (k >= numWarmups)
                    mclTimes[k-numWarmups] = System.currentTimeMillis() - startTime;

                startTime = System.currentTimeMillis();
                for (int i = 0; i < bases.length; ++i) {
                    bases[i].powWnaf(exponents[i].getInteger(), 4, false);
                }
                if (k >= numWarmups)
                    wnafTimes[k-numWarmups] = System.currentTimeMillis() - startTime;

                startTime = System.currentTimeMillis();
                for (int i = 0; i < bases.length; ++i) {
                    bases[i].powSlidingWindow(exponents[i].getInteger(), 4, false);
                }
                if (k >= numWarmups)
                    slidingTimes[k-numWarmups] = System.currentTimeMillis() - startTime;

            }
            System.out.println("------ " + l + " -----");
            System.out.println("Mcl avg: " + average(mclTimes));
            System.out.println("Wnaf avg: " + average(wnafTimes));
            System.out.println("Sliding avg: " + average(slidingTimes));

            System.out.println("Mcl min: " + minimum(mclTimes));
            System.out.println("Wnaf min: " + minimum(wnafTimes));
            System.out.println("Sliding min: " + minimum(slidingTimes));
        }
    }

    public static double average(long[] dataPoints) {
        double avg = 0;
        for (long dataPoint : dataPoints) {
            avg += dataPoint;
        }
        return avg / dataPoints.length;
    }

    public static long minimum(long[] dataPoints) {
        long min = dataPoints[0];
        for (long dataPoint : dataPoints) {
            if (dataPoint < min)
                min = dataPoint;
        }
        return min;
    }
}
