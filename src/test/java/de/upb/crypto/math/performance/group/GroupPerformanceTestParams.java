package de.upb.crypto.math.performance.group;

import de.upb.crypto.math.interfaces.structures.Group;

/**
 * Parameters used in the {@link GroupPerformanceTest}.
 * <p>
 * The default number of test repetitions is 10.
 */
public class GroupPerformanceTestParams {
    Group group1;
    Group group2;
    int testRepetitions = 10;

    public GroupPerformanceTestParams(Group group1, Group group2, int testRepetitions) {
        this.group1 = group1;
        this.group2 = group2;
        this.testRepetitions = testRepetitions;
    }

    public GroupPerformanceTestParams(Group group1, Group group2) {
        this.group1 = group1;
        this.group2 = group2;
    }
}
