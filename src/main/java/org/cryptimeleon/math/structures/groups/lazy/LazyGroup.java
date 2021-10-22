package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.groups.GroupImpl;
import org.cryptimeleon.math.structures.groups.exp.*;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zp;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * A {@link GroupImpl} wrapper implementing deferred (lazy) evaluation for abelian groups with known finite order.
 * <p>
 * Allows for additional optimizations using information about the operations being applied.
 * Specifically, multi-exponentiation techniques can be applied to significantly speed up computations.
 * <p>
 * For more information, see the <a href="https://upbcuk.github.io/docs/lazy-eval.html">documentation</a>.
 */
public class LazyGroup implements Group {
    static final ExecutorService executor = ForkJoinPool.commonPool();  //using the commonPool because it automatically terminates with the JVM.
    // Alternative: Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); //but in that case, you'd have to handle shutdown gracefully yourself, probably via Runtime.getRuntime().addShutdownHook
    // I'm guessing the newFixedThreadPool may perform better
    // than the workStealingPool because this generally observes the order of tasks thrown at it (which generally the user will choose "correctly", i.e. smaller
    // expressions first, dependent expressions later). Everything still works with a workStealingPool, but the fear is that at some point, a thread steals
    // some computation that depends on lots of other LazyGroupElements that are currently in the process of being evaluated
    // and then possibly has to block a long time to wait for the other threads to finish while it could have computed something useful.
    // Honestly, we should try this out in a proper performance test.

    int exponentiationWindowSize = 4;
    int precomputationWindowSize = 8;
    @Represented
    GroupImpl impl;
    BigInteger size;
    boolean isPrimeOrder;
    Zn zn;
    GroupElement generator;
    MultiExpAlgorithm selectedMultiExpAlgorithm;
    ExpAlgorithm selectedExpAlgorithm;

    public LazyGroup(GroupImpl impl) {
        this(impl, 4, 8);
    }

    public LazyGroup(GroupImpl impl, int exponentiationWindowSize, int precomputationWindowSize) {
        this.impl = impl;
        this.exponentiationWindowSize = exponentiationWindowSize;
        this.precomputationWindowSize = precomputationWindowSize;
        init();
    }

    private void init() {
        size = impl.size();
        if (size == null || !impl.isCommutative()) {
            throw new IllegalArgumentException("Need commutative cyclic group of finite known order.");
        }
        generator = wrap(impl.getGenerator());
        isPrimeOrder = size.isProbablePrime(100);
        zn = isPrimeOrder ? new Zp(size) : new Zn(size);
        if (impl.estimateCostInvPerOp() >= ExponentiationAlgorithms.WNAF_INVERSION_COST_THRESHOLD) {
            selectedMultiExpAlgorithm = MultiExpAlgorithm.WNAF;
            selectedExpAlgorithm = ExpAlgorithm.WNAF;
        } else {
            selectedMultiExpAlgorithm = MultiExpAlgorithm.SLIDING;
            selectedExpAlgorithm = ExpAlgorithm.SLIDING;
        }
    }

    public LazyGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        init();
    }

    public LazyGroup(Representation repr, int exponentiationWindowSize, int precomputationWindowSize) {
        ReprUtil.deserialize(this, repr);
        this.exponentiationWindowSize = exponentiationWindowSize;
        this.precomputationWindowSize = precomputationWindowSize;
        init();
    }

    public LazyGroupElement wrap(GroupElementImpl impl) {
        return new ConstLazyGroupElement(this, impl);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new NeutralLazyGroupElement(this);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return size;
    }

    @Override
    public boolean hasPrimeSize() {
        return isPrimeOrder;
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new RandomGroupElement(this);
    }

    @Override
    public GroupElement getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        return new RandomNonNeutralGroupElement(this);
    }

    @Override
    public GroupElement restoreElement(Representation repr) {
        return wrap(impl.restoreElement(repr));
    }

    @Override
    public GroupElement getGenerator() throws UnsupportedOperationException {
        return generator;
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return impl.getUniqueByteLength();
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public Zn getZn() {
        return zn;
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LazyGroup lazyGroup = (LazyGroup) o;
        return impl.equals(lazyGroup.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }

    @Override
    public String toString() {
        return "Lazy "+impl.toString();
    }

    public GroupElementImpl compute(Multiexponentiation multiexp) {
        if (multiexp.isEmpty())
            return impl.getNeutralElement();
        if (impl.implementsOwnMultiExp())
            return impl.multiexp(multiexp);
        // use generic if group does not implement own algorithm
        switch (selectedMultiExpAlgorithm) {
            case SLIDING:
                return ExponentiationAlgorithms.interleavingSlidingWindowMultiExp(
                        multiexp,
                        Math.max(
                                exponentiationWindowSize,
                                multiexp.computeMinPrecomputedWindowSize(MultiExpAlgorithm.SLIDING)
                        )
                );
            case WNAF:
                return ExponentiationAlgorithms.interleavingWnafMultiExp(
                        multiexp,
                        Math.max(
                                exponentiationWindowSize,
                                multiexp.computeMinPrecomputedWindowSize(MultiExpAlgorithm.WNAF)
                        )
                );
            default:
                throw new IllegalStateException("Unsupported MultiExpAlgorithm " + selectedMultiExpAlgorithm);
        }
        //TODO some multiexponentiation algorithms may be able to handle different windows sizes for each base.
        // Generally, using the minimum for window size is "safe", but not necessarily clever performance-wise. Example: \prod h_i^x_i * (g^a)^b. The latter has no precomputation at all (even if g may have it), so ...
    }

    public GroupElementImpl compute(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        if (impl.implementsOwnExp())
            return impl.exp(base, exponent, precomputation);
        // use generic if group does not implement own algorithm
        switch (selectedExpAlgorithm) {
            case SLIDING:
                return ExponentiationAlgorithms.slidingWindowExp(
                        base, exponent, precomputation, exponentiationWindowSize
                );
            case WNAF:
                return ExponentiationAlgorithms.wnafExp(base, exponent, precomputation, exponentiationWindowSize);
            default:
                throw new IllegalStateException("Unsupported ExpAlgorithm " + selectedExpAlgorithm);
        }
    }

    /**
     * Returns the window size used for the non-cached precomputations computed during the exponentiation algorithm.
     */
    public int getExponentiationWindowSize() {
        return exponentiationWindowSize;
    }

    /**
     * Allows setting the window size used for used for the non-cached precomputations computed during the
     * exponentiation algorithm.
     * <p>
     * A larger window size leads to an exponential increase in the number of precomputations done during
     * exponentiation. As the precomputations affected by this variable are only temporarily stored during execution
     * of the exponentiation algorithm, we do not recommend setting this too high as the cost of computing the
     * whole window quickly exceeds its performance benefits during the actual exponentiation.
     * <p>
     * If you want to change the number of cached precomputations, use {@link #setPrecomputationWindowSize(int)}.
     */
    public void setExponentiationWindowSize(int exponentiationWindowSize) {
        this.exponentiationWindowSize = exponentiationWindowSize;
    }

    /**
     * Returns the window size used for the precomputations.
     */
    public int getPrecomputationWindowSize() {
        return precomputationWindowSize;
    }

    /**
     * Allows setting the window size used for the cached precomputations.
     * <p>
     * A larger window size leads to an exponential increase in the number of cached precomputations done but
     * can also improve the performance of later exponentiations.
     */
    public void setPrecomputationWindowSize(int precomputationWindowSize) {
        this.precomputationWindowSize = precomputationWindowSize;
    }

    public MultiExpAlgorithm getSelectedMultiExpAlgorithm() {
        return selectedMultiExpAlgorithm;
    }

    public void setSelectedMultiExpAlgorithm(MultiExpAlgorithm selectedMultiExpAlgorithm) {
        this.selectedMultiExpAlgorithm = selectedMultiExpAlgorithm;
    }

    public ExpAlgorithm getSelectedExpAlgorithm() {
        return selectedExpAlgorithm;
    }

    public void setSelectedExpAlgorithm(ExpAlgorithm selectedExpAlgorithm) {
        this.selectedExpAlgorithm = selectedExpAlgorithm;
    }

    public GroupImpl getImpl() {
        return impl;
    }
}
