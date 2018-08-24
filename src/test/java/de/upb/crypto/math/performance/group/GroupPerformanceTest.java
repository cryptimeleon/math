package de.upb.crypto.math.performance.group;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This test intends to evaluate the performance of the implemented groups.
 */
@RunWith(value = Parameterized.class)
public class GroupPerformanceTest {
    private Group group1;
    private Group group2;
    private int testRepetitions;

    public GroupPerformanceTest(GroupPerformanceTestParams testParams) {
        this.group1 = testParams.group1;
        this.group2 = testParams.group2;
        this.testRepetitions = testParams.testRepetitions;
    }

    @Test
    public void evaluateExponentiationSpeedUp() {
        long group1ExpTime = 0;
        long group2ExpTime = 0;

        for (int i = 0; i < testRepetitions; i++) {
            GroupElement group1Base = group1.getUniformlyRandomElement();
            BigInteger group1Exponent = new Zp(group1.size()).getUniformlyRandomElement().getInteger();
            GroupElement group1Result = group1Base;

            GroupElement group2Base = group2.getUniformlyRandomElement();
            BigInteger group2Exponent = new Zp(group2.size()).getUniformlyRandomElement().getInteger();
            GroupElement group2Result = group2Base;

            long refTimeGroup1 = System.nanoTime();
            group1Result = group1Result.pow(group1Exponent);
            group1ExpTime += System.nanoTime() - refTimeGroup1;

            long refTimeGroup2 = System.nanoTime();
            group2Result = group2Result.pow(group2Exponent);
            group2ExpTime += System.nanoTime() - refTimeGroup2;
        }
        group1ExpTime /= testRepetitions;
        group2ExpTime /= testRepetitions;

        System.out.println("In " + testRepetitions + " runs exponentiation in " + group1.getClass().getSimpleName() + " (on avg) in time " + group1ExpTime / 1e6
                + "ms and in " + group2.getClass().getSimpleName() + " (on avg) in time " + group2ExpTime / 1e6 + ".\nSpeedup: " + ((double) group1ExpTime) / group2ExpTime);
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<GroupPerformanceTestParams> data() {
        ArrayList<GroupPerformanceTestParams> schemes = new ArrayList<>();

        return schemes;
    }
}
