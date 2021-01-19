package de.upb.crypto.math.pairings.counting;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.serialization.annotations.Represented;
import de.upb.crypto.math.structures.groups.exp.MultiExpTerm;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.groups.exp.SmallExponentPrecomputation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Zn-based group that supports counting group operations, inversions, squarings and exponentiations as well as
 * number of terms in each multi-exponentiation.
 */
public class CountingGroupImpl implements GroupImpl {

    /**
     * Name of this group. Group elements between {@code CountingGroupElementImpl} instances only allow for group
     * operations if the groups' names match.
     */
    @Represented
    protected String name;

    /**
     * The Zn underlying this group. Realizes the actual group operations.
     */
    @Represented
    protected Zn zn;

    /**
     * Whether to count exponentiations as a single unit. If set to true, group operations in those exponentiations
     * will not be counted.
     */
    @Represented
    protected Boolean enableExpCounting;

    /**
     * Whether to count multi-exponentiations as a single unit. If set to true, group operations in those
     * multi-exponentiations will not be counted.
     */
    @Represented
    protected Boolean enableMultiExpCounting;

    /**
     * The counted number of inversions.
     */
    protected long numInversions;

    /**
     * The counted number of operations. Squarings are not considered in the group operation counter.
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

    /**
     * Instantiates this group with the given name and group size and to not count (multi-)exponentiations
     * explicitly (instead only total group operations are counted).
     *
     * @param name a unique name for this group. group operations are only compatible between groups of the same name
     *             and n
     * @param n    the size of this group
     */
    public CountingGroupImpl(String name, BigInteger n) {
        this(name, n, false, false);
    }

    /**
     * Instantiates this group with the given name, group size, and counting configuration.
     *
     * @param name a unique name for this group. group operations are only compatible between groups of the same name
     *             and n
     * @param n    the size of this group
     * @param enableExpCounting if {@code true}, exponentiations in G1, G2 and GT are counted as a single unit
     *                          and group operations within exponentiations are not counted; otherwise the former is
     *                          not done and group operations within exponentiations are added to the total count
     * @param enableMultiExpCounting if {@code true}, number of terms in each multi-exponentiation is tracked and
     *                               group operations within multi-exponentiations are not counted; otherwise
     *                               the former is not done and group operations within multi-exponentiations
     *                               are added to the total count
     */
    public CountingGroupImpl(String name, BigInteger n, boolean enableExpCounting, boolean enableMultiExpCounting) {
        zn = new Zn(n);
        this.name = name;
        this.enableExpCounting = enableExpCounting;
        this.enableMultiExpCounting = enableMultiExpCounting;
        numInversions = 0;
        numOps = 0;
        numSquarings = 0;
        numExps = 0;
        multiExpTermNumbers = new LinkedList<>();
        numRetrievedRepresentations = 0;
    }

    public CountingGroupImpl(Representation repr) {
        new ReprUtil(this).deserialize(repr);
        numInversions = 0;
        numOps = 0;
        numSquarings = 0;
        numExps = 0;
        multiExpTermNumbers = new LinkedList<>();
        numRetrievedRepresentations = 0;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public GroupElementImpl getNeutralElement() {
        return wrap(zn.getZeroElement());
    }

    @Override
    public GroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        return wrap(zn.getUniformlyRandomElement());
    }

    @Override
    public GroupElementImpl getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        return wrap(zn.getUniformlyRandomNonzeroElement());
    }

    @Override
    public GroupElementImpl getElement(Representation repr) {
        return wrap(zn.getElement(repr));
    }

    @Override
    public GroupElementImpl getGenerator() throws UnsupportedOperationException {
        return wrap(zn.getOneElement());
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return zn.getUniqueByteLength();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        CountingGroupImpl that = (CountingGroupImpl) other;
        return Objects.equals(name, that.name)
                && Objects.equals(zn, that.zn)
                && Objects.equals(enableExpCounting, that.enableExpCounting)
                && Objects.equals(enableMultiExpCounting, that.enableMultiExpCounting);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return zn.size();
    }

    @Override
    public boolean hasPrimeSize() throws UnsupportedOperationException {
        return zn.hasPrimeSize();
    }

    @Override
    public boolean implementsOwnExp() {
        return enableExpCounting;
    }

    @Override
    public GroupElementImpl exp(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        return base.pow(exponent); // this method already counts the exponentiation
    }

    @Override
    public boolean implementsOwnMultiExp() {
        return enableMultiExpCounting;
    }

    @Override
    public GroupElementImpl multiexp(Multiexponentiation mexp) {
        // This method is only used if enableMultiExpCounting is set to true; hence, we count
        // the multi-exponentiation done.
        CountingGroupElementImpl result = (CountingGroupElementImpl) mexp.getConstantFactor().orElse(getNeutralElement());
        for (MultiExpTerm term : mexp.getTerms()) {
            // Use methods where we can disable counting since we only want to count the multi-exponentiation here
            result = (CountingGroupElementImpl) result
                    .op(((CountingGroupElementImpl) term.getBase()).pow(term.getExponent(),false), false);
        }
        addMultiExpBaseNumber(mexp.getTerms().size());
        return result;
    }

    /**
     * Wraps a {@code ZnElement} in a {@code CountingGroupElementImpl} belonging to this group.
     */
    public CountingGroupElementImpl wrap(Zn.ZnElement elem) {
        return new CountingGroupElementImpl(this, elem);
    }

    @Override
    public double estimateCostInvPerOp() {
        return 1.6;
    }

    protected void incrementNumOps() {
        ++numOps;
    }

    protected void incrementNumInversions() {
        ++numInversions;
    }

    protected void incrementNumSquarings() {
        ++numSquarings;
    }

    protected void incrementNumExps() {
        ++numExps;
    }

    /**
     * Tracks the fact that a multi-exponentiation with the given number of terms was done.
     * @param numTerms the number of terms (bases) in the multi-exponentiation
     */
    protected void addMultiExpBaseNumber(int numTerms) {
        if (numTerms > 1) {
            multiExpTermNumbers.add(numTerms);
        }
    }

    protected void incrementNumRetrievedRepresentations() {
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

    public long getNumExps() { return numExps; }

    public List<Integer> getMultiExpTermNumbers() {
        return multiExpTermNumbers;
    }

    public long getNumRetrievedRepresentations() {
        return numRetrievedRepresentations;
    }

    public void resetOpsCounter() {
        numOps = 0;
    }

    public void resetInvsCounter() {
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

    public void resetCounters() {
        resetOpsCounter();
        resetInvsCounter();
        resetSquaringsCounter();
        resetExpsCounter();
        resetMultiExpTermNumbers();
        resetRetrievedRepresentationsCounter();
    }
}
