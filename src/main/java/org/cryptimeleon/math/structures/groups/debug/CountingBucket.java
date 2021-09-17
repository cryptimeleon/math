package org.cryptimeleon.math.structures.groups.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stores group operation data.
 * <p>
 * Operations are thread-safe, meaning that all increment and getter methods are implemented automically,
 * and the multi-exponentiation term number list is protected via a lock.
 */
public class CountingBucket {

    /**
     * The counted number of inversions.
     */
    final AtomicLong numInversions;

    /**
     * The counted number of operations.
     * Squarings are not considered in the group operation counter.
     */
    final AtomicLong numOps;

    /**
     * The counted number of squarings.
     */
    final AtomicLong numSquarings;

    /**
     * The counted number of exponentiations.
     */
    final AtomicLong numExps;

    /**
     * Number of retrieved representations for elements of this group.
     */
    final AtomicLong numRetrievedRepresentations;

    /**
     * Contains number of terms for each multi-exponentiation performed.
     */
    private final List<Integer> multiExpTermNumbers;

    private final Lock multiExpTermNumbersLock;

    public CountingBucket() {
        this.numInversions = new AtomicLong();
        this.numOps = new AtomicLong();
        this.numSquarings = new AtomicLong();
        this.numExps = new AtomicLong();
        this.numRetrievedRepresentations = new AtomicLong();
        this.multiExpTermNumbers = new ArrayList<>();
        this.multiExpTermNumbersLock = new ReentrantLock();
    }

    public void incrementNumOps() {
        numOps.incrementAndGet();
    }

    public void incrementNumInversions() {
        numInversions.incrementAndGet();
    }

    public void incrementNumSquarings() {
        numSquarings.incrementAndGet();
    }

    public void incrementNumExps() {
        numExps.incrementAndGet();
    }

    /**
     * Tracks the fact that a multi-exponentiation with the given number of terms was done.
     * @param numTerms the number of terms (bases) in the multi-exponentiation
     */
    public void addMultiExpBaseNumber(int numTerms) {
        multiExpTermNumbersLock.lock();
        try {
            if (numTerms > 1) {
                multiExpTermNumbers.add(numTerms);
            }
        } finally {
            multiExpTermNumbersLock.unlock();
        }
    }

    /**
     * Adds the given list of multi-exponentiation term numbers to this bucket.
     * @param newTerms the new terms to add to this bucket
     */
    public void addAllMultiExpBaseNumbers(List<Integer> newTerms) {
        multiExpTermNumbersLock.lock();
        try {
            multiExpTermNumbers.addAll(newTerms);
        } finally {
            multiExpTermNumbersLock.unlock();
        }
    }

    void incrementNumRetrievedRepresentations() {
        numRetrievedRepresentations.incrementAndGet();
    }

    public long getNumInversions() {
        return numInversions.get();
    }

    public long getNumOps() {
        return numOps.get();
    }

    public long getNumSquarings() {
        return numSquarings.get();
    }

    public long getNumExps() {
        return numExps.get();
    }

    public long getNumRetrievedRepresentations() {
        return numRetrievedRepresentations.get();
    }

    /**
     * Returns an immutable copy of the list storing the multi-exponentiation term numbers.
     * This list contains the number of exponentiations in each multi-exponentiation that has been calculated.
     */
    public List<Integer> getMultiExpTermNumbers() {
        return Collections.unmodifiableList(multiExpTermNumbers);
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

    protected void resetOpsCounter() {
        numOps.set(0);
    }

    protected void resetInversionsCounter() {
        numInversions.set(0);
    }

    protected void resetSquaringsCounter() {
        numSquarings.set(0);
    }

    protected void resetExpsCounter() { numExps.set(0); }

    protected void resetMultiExpTermNumbers() {
        multiExpTermNumbersLock.lock();
        try {
            multiExpTermNumbers.clear();
        } finally {
            multiExpTermNumbersLock.unlock();
        }
    }

    protected void resetRetrievedRepresentationsCounter() {
        numRetrievedRepresentations.set(0);
    }

    protected boolean isEmpty() {
        return numOps.get() == 0 && numInversions.get() == 0 && numSquarings.get() == 0 && numExps.get() == 0
                && getMultiExpTermNumbers().isEmpty() && numRetrievedRepresentations.get() == 0;
    }
}
