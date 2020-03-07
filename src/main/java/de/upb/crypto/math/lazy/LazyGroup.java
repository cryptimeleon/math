package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

/**
 * A group where operations are deferred until they are needed.
 * This allows for automatic optimization of computations.
 * <p>
 * Generally, this can be used to estimate the potential of optimizing
 * equations in your scheme. For well-optimized implementations, using
 * the LazyGroup should probably be slightly slower than using the group
 * itself.
 * <p>
 * The optimizations done by this group are:
 * - Don't compute the same values multiple times.
 * - Compute everything using PowProductExpressions and PairingProductExpressions.
 * - Try to evaluate group elements concurrently (this may not help depending on the concrete code).
 */
public class LazyGroup implements Group {
    @Represented
    protected Group baseGroup;

    /**
     * Iff this is the target group of some pairing, this is not null.
     */
    @Represented
    protected LazyPairing associatedPairing = null;

    private ExecutorService executor;
    private HashMap<LazyGroupElementIdentityEqualsWrapper, Long> uncomputedRoots = new HashMap<>();
    private Timer timer;

    /**
     * cache of already instantiated elements as a map of a LazyGroupElement (wrapped to make sure they're
     * compared on the expression level (i.e. based on leaf nodes)) to itself (wrapped).
     * It may be tempting to replace this with a Set, but sets don't allow you to get the actual value/object from
     * the set (only check if one is present)
     */
    protected WeakHashMap<LazyGroupElementWrapper, LazyGroupElementIdentityEqualsWrapper> cache = new WeakHashMap<>();

    @Represented
    protected Boolean enableExpressionDeduplication;
    @Represented
    protected Boolean enableConcurrentEvaluation;

    /**
     * Wrapper that ensures that elements are compared based on their corresponding expressions
     */
    private static class LazyGroupElementWrapper {
        private LazyGroupElement e;

        public LazyGroupElementWrapper(LazyGroupElement e) {
            this.e = e;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            return e.equalsOnExpressionLevel(((LazyGroupElementWrapper) obj).e);
        }

        @Override
        public int hashCode() {
            return e.hashCodeOnExpressionLevel();
        }
    }

    /**
     * Wrapper that ensures that elements are compared based on identity for efficiency.
     */
    private static class LazyGroupElementIdentityEqualsWrapper {
        private LazyGroupElement e;

        public LazyGroupElementIdentityEqualsWrapper(LazyGroupElement e) {
            this.e = e;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            return e == ((LazyGroupElementIdentityEqualsWrapper) obj).e;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    protected BiFunction<BigInteger, BigInteger, BigInteger> bigIntAddAndReduce = (a, b) -> a.add(b).mod(size());
    protected BiFunction<BigInteger, BigInteger, BigInteger> bigIntMulAndReduce = (a, b) -> a.multiply(b).mod(size());

    /**
     * Root elements this old are speculatively computed in parallel. In milliseconds
     */
    private static final long rootComputeOffset = 100;

    /**
     * Instantiates this lazy group using the supplied base group.
     *
     * @param group the group that is wrapped by this LazyGroup.
     */
    public LazyGroup(Group group) {
        this(group, true, false);
    }

    /**
     * Constructor for the target group of a pairing.
     */
    protected LazyGroup(Group group, LazyPairing pairing) {
        this(group, pairing, true, false);
    }

    /**
     * Instantiates this lazy group using the supplied base group.
     *
     * @param group                         the group that is wrapped by this LazyGroup.
     * @param enableExpressionDeduplication if set to true, the lazy group will try to find expressions that are
     *                                      calculated multiple times and cache their results.
     * @param enableConcurrentEvaluation    if set to true, the lazy group will continuously evaluate expressions in
     *                                      the background even before they are accessed.
     */
    public LazyGroup(Group group, boolean enableExpressionDeduplication, boolean enableConcurrentEvaluation) {
        this.baseGroup = group;
        this.enableExpressionDeduplication = enableExpressionDeduplication;
        this.enableConcurrentEvaluation = enableConcurrentEvaluation;
        init();
    }

    /**
     * Constructor for the target group of a pairing.
     */
    protected LazyGroup(Group group, LazyPairing pairing, boolean enableExpressionDeduplication,
                        boolean enableConcurrentEvaluation) {
        this.baseGroup = group;
        this.associatedPairing = pairing;
        this.enableExpressionDeduplication = enableExpressionDeduplication;
        this.enableConcurrentEvaluation = enableConcurrentEvaluation;
        init();
    }

    public LazyGroup(Representation repr) {
        new ReprUtil(this).deserialize(repr);
        init();
    }

    /**
     * Stuff that happens in all constructors
     */
    private void init() {
        if (baseGroup.size() == null)
            throw new RuntimeException("LazyGroup only works for finite groups");

        this.executor = Executors.newWorkStealingPool();
        if (enableConcurrentEvaluation) {
            this.timer = new Timer(true);
            timer.scheduleAtFixedRate(new TimerTask() { //regularly precompute old uncomputed root LazyGroupElements.
                @Override
                public void run() {
                    synchronized (uncomputedRoots) {
                        long now = System.currentTimeMillis();
                        for (Map.Entry<LazyGroupElementIdentityEqualsWrapper, Long> e : uncomputedRoots.entrySet()) {
                            if (e.getValue() > now + rootComputeOffset)
                                precompute(e.getKey().e);
                        }
                    }
                }
            }, rootComputeOffset, rootComputeOffset);
        }
    }

    /**
     * Outputs a cached element that's computed like the given element
     * (on the expression level).
     * Returns cached value if existing. Otherwise, caches and returns the supplied value.
     */
    @Nullable
    protected LazyGroupElement cached(LazyGroupElement value) {
        if (!enableExpressionDeduplication)
            return value;

        LazyGroupElementWrapper wrapped = new LazyGroupElementWrapper(value);
        LazyGroupElementIdentityEqualsWrapper result = cache.get(wrapped);
        if (result == null) {
            cache.put(wrapped, new LazyGroupElementIdentityEqualsWrapper(value));
            return value;
        }
        return result.e;
    }

    /**
     * Queues the element to be computed concurrently
     */
    protected void precompute(LazyGroupElement elem) {
        unregisterUncomputedRoot(elem);
        executor.submit(elem::evaluate);
    }

    protected void unregisterUncomputedRoot(LazyGroupElement e) {
        if (!enableConcurrentEvaluation)
            return;
        synchronized (uncomputedRoots) {
            uncomputedRoots.remove(new LazyGroupElementIdentityEqualsWrapper(e));
        }
    }

    protected void registerUncomputedRoot(LazyGroupElement e) {
        if (!enableConcurrentEvaluation)
            return;
        synchronized (uncomputedRoots) {
            uncomputedRoots.put(new LazyGroupElementIdentityEqualsWrapper(e), System.currentTimeMillis());
        }
    }

    @Override
    public GroupElement getNeutralElement() {
        return new LeafGroupElement(this, baseGroup.getNeutralElement());
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return baseGroup.size();
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new LeafGroupElement(this, baseGroup.getUniformlyRandomElement());
    }

    @Override
    public GroupElement getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        return new LeafGroupElement(this, baseGroup.getUniformlyRandomNonNeutral());
    }

    @Override
    public GroupElement getElement(Representation repr) {
        return new LeafGroupElement(this, baseGroup.getElement(repr));
    }

    @Override
    public GroupElement getGenerator() throws UnsupportedOperationException {
        return new LeafGroupElement(this, baseGroup.getGenerator());
    }

    @Override
    public GroupElement evaluate(PowProductExpression expr) throws IllegalArgumentException {
        return new BatchOpGroupElement(this, expr);
    }

    @Override
    public int estimateCostOfInvert() {
        return baseGroup.estimateCostOfInvert();
    }

    @Override
    public boolean isCommutative() {
        return baseGroup.isCommutative();
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return baseGroup.getUniqueByteLength();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LazyGroup))
            return false;
        return baseGroup.equals(((LazyGroup) obj).baseGroup);
    }

    @Override
    public int hashCode() {
        return baseGroup.hashCode();
    }

    @Override
    public String toString() {
        return "lazy " + baseGroup.toString();
    }
}
