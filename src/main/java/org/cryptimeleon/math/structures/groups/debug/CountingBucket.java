package org.cryptimeleon.math.structures.groups.debug;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores group operation data.
 */
class CountingBucket {

    /**
     * The counted number of inversions including (multi-)exponentiations.
     */
    protected long numInversionsTotal;

    /**
     * The counted number of inversions not including (multi-)exponentiations.
     */
    protected long numInversionsNoExpMultiExp;

    /**
     * The counted number of operations including (multi-)exponentiations.
     * Squarings are not considered in the group operation counter.
     */
    protected long numOpsTotal;

    /**
     * The counted number of operations not including (multi-)exponentiations.
     * Squarings are not considered in the group operation counter.
     */
    protected long numOpsNoExpMultiExp;

    /**
     * The counted number of squarings including (multi-)exponentiations.
     */
    protected long numSquaringsTotal;

    /**
     * The counted number of squarings not including (multi-)exponentiations.
     */
    protected long numSquaringsNoExpMultiExp;

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
        this.numInversionsTotal = 0;
        this.numInversionsNoExpMultiExp = 0;
        this.numOpsTotal = 0;
        this.numOpsNoExpMultiExp = 0;
        this.numSquaringsTotal = 0;
        this.numSquaringsNoExpMultiExp = 0;
        this.numExps = 0;
        this.numRetrievedRepresentations = 0;
        this.multiExpTermNumbers = new ArrayList<>();
    }

    public void incrementNumOpsTotal() {
        ++numOpsTotal;
    }

    public void incrementNumOpsNoExpMultiExp() {
        ++numOpsNoExpMultiExp;
    }

    public void incrementNumInversionsTotal() {
        ++numInversionsTotal;
    }

    public void incrementNumInversionsNoExpMultiExp() {
        ++numInversionsNoExpMultiExp;
    }

    public void incrementNumSquaringsTotal() {
        ++numSquaringsTotal;
    }

    public void incrementNumSquaringsNoExpMultiExp() {
        ++numSquaringsNoExpMultiExp;
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

    public long getNumInversionsTotal() {
        return numInversionsTotal;
    }

    public long getNumInversionsNoExpMultiExp() {
        return numInversionsNoExpMultiExp;
    }

    public long getNumOpsTotal() {
        return numOpsTotal;
    }

    public long getNumOpsNoExpMultiExp() {
        return numOpsNoExpMultiExp;
    }

    public long getNumSquaringsTotal() {
        return numSquaringsTotal;
    }

    public long getNumSquaringsNoExpMultiExp() {
        return numSquaringsNoExpMultiExp;
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

    public void resetOpsTotalCounter() {
        numOpsTotal = 0;
    }

    public void resetOpsNoExpMultiExpCounter() {
        numOpsNoExpMultiExp = 0;
    }

    public void resetInversionsTotalCounter() {
        numInversionsTotal = 0;
    }

    public void resetInversionsNoExpMultiExpCounter() {
        numInversionsNoExpMultiExp = 0;
    }

    public void resetSquaringsTotalCounter() {
        numSquaringsTotal = 0;
    }

    public void resetSquaringsNoExpMultiExpCounter() {
        numSquaringsNoExpMultiExp = 0;
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
        resetOpsTotalCounter();
        resetOpsNoExpMultiExpCounter();
        resetInversionsTotalCounter();
        resetInversionsNoExpMultiExpCounter();
        resetSquaringsTotalCounter();
        resetSquaringsNoExpMultiExpCounter();
        resetExpsCounter();
        resetMultiExpTermNumbers();
        resetRetrievedRepresentationsCounter();
    }
}
