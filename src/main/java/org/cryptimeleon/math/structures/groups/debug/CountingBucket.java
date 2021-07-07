package org.cryptimeleon.math.structures.groups.debug;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores group operation data.
 */
public class CountingBucket {

    /**
     * The counted number of inversions.
     */
    protected long numInversions;

    /**
     * The counted number of operations.
     * Squarings are not considered in the group operation counter.
     */
    protected long numOps;

    /**
     * The counted number of squarings.
     */
    protected long numSquarings;

    /**
     * The counted number of exponentiations.
     */
    protected long numExps;

    /**
     * Number of retrieved representations for elements of this group.
     */
    protected long numRetrievedRepresentations;

    /**
     * Contains number of terms for each multi-exponentiation performed.
     */
    protected List<Integer> multiExpTermNumbers;

    public CountingBucket() {
        this.numInversions = 0;
        this.numOps = 0;
        this.numSquarings = 0;
        this.numExps = 0;
        this.numRetrievedRepresentations = 0;
        this.multiExpTermNumbers = new ArrayList<>();
    }

    public void incrementNumOps() {
        ++numOps;
    }


    public void incrementNumInversions() {
        ++numInversions;
    }

    public void incrementNumSquarings() {
        ++numSquarings;
    }

    public void incrementNumExps() {
        ++numExps;
    }

    /**
     * Tracks the fact that a multi-exponentiation with the given number of terms was done.
     * @param numTerms the number of terms (bases) in the multi-exponentiation
     */
    public void addMultiExpBaseNumber(int numTerms) {
        if (numTerms > 1) {
            multiExpTermNumbers.add(numTerms);
        }
    }

    void incrementNumRetrievedRepresentations() {
        ++numRetrievedRepresentations;
    }

    public long getNumInversions() {
        return numInversions;
    }

    public long getNumOps() {
        return numOps;
    }

    public long getNumSquarings() {
        return numSquarings;
    }

    public long getNumExps() {
        return numExps;
    }

    public long getNumRetrievedRepresentations() {
        return numRetrievedRepresentations;
    }

    public List<Integer> getMultiExpTermNumbers() {
        return multiExpTermNumbers;
    }

    public void resetOpsCounter() {
        numOps = 0;
    }

    public void resetInversionsCounter() {
        numInversions = 0;
    }

    public void resetSquaringsCounter() {
        numSquarings = 0;
    }

    public void resetExpsCounter() { numExps = 0; }

    public void resetMultiExpTermNumbers() { multiExpTermNumbers = new LinkedList<>(); }

    public void resetRetrievedRepresentationsCounter() {
        numRetrievedRepresentations = 0;
    }

    /**
     * Resets all counters.
     */
    public void resetCounters() {
        resetOpsCounter();
        resetInversionsCounter();
        resetSquaringsCounter();
        resetExpsCounter();
        resetMultiExpTermNumbers();
        resetRetrievedRepresentationsCounter();
    }
}
