package de.upb.crypto.math.performance.group;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;

/**
 * Parameters used in the {@link GroupImplPerformanceTest}.
 * <p>
 * The default number of test repetitions is 10.
 */
public class GroupPerformanceTestParams {
    GroupImpl group1;
    GroupImpl group2;
    int testRepetitions = 10;

    public GroupPerformanceTestParams(GroupImpl group1, GroupImpl group2, int testRepetitions) {
        this.group1 = group1;
        this.group2 = group2;
        this.testRepetitions = testRepetitions;
    }

    public GroupPerformanceTestParams(GroupImpl group1, GroupImpl group2) {
        this.group1 = group1;
        this.group2 = group2;
    }
}
