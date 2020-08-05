package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.pairings.debug.DebugGroupElementImpl;
import de.upb.crypto.math.pairings.debug.DebugGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.v2.ReprUtil;
import de.upb.crypto.math.serialization.annotations.v2.Represented;
import de.upb.crypto.math.structures.groups.exp.ExponentiationAlgorithms;
import de.upb.crypto.math.structures.groups.exp.Multiexponentiation;
import de.upb.crypto.math.structures.groups.exp.SmallExponentPrecomputation;
import de.upb.crypto.math.structures.zn.Zn;
import de.upb.crypto.math.structures.zn.Zp;
import sun.security.ssl.Debug;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A group optimized for groups with somewhat expensive operations (and, particularly, exponentiations).
 * Assumes abelian cyclic group of known finite order.
 */
public class LazyGroup implements Group {
    static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    int exponentiationWindowSize = 4;
    int precomputationWindowSize = 8;
    Boolean containsDebugGroup;
    @Represented
    GroupImpl impl;
    BigInteger size;
    boolean isPrimeOrder;
    Zn zn;
    GroupElement generator;

    public LazyGroup(GroupImpl impl) {
        this(impl, 4, 8);
    }

    public LazyGroup(GroupImpl impl, int exponentiationWindowSize, int precomputationWindowSize) {
        this.impl = impl;
        this.exponentiationWindowSize = exponentiationWindowSize;
        this.precomputationWindowSize = precomputationWindowSize;
        containsDebugGroup = impl instanceof DebugGroupImpl;
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
    }

    public LazyGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        containsDebugGroup = impl instanceof DebugGroupImpl;
        init();
    }

    public LazyGroup(Representation repr, int exponentiationWindowSize, int precomputationWindowSize) {
        ReprUtil.deserialize(this, repr);
        containsDebugGroup = impl instanceof DebugGroupImpl;
        this.exponentiationWindowSize = exponentiationWindowSize;
        this.precomputationWindowSize = precomputationWindowSize;
        init();
    }

    protected LazyGroupElement wrap(GroupElementImpl impl) {
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
    public GroupElement getElement(Representation repr) {
        return wrap(impl.getElement(repr));
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
        GroupElementImpl result = ExponentiationAlgorithms.interleavingWnafMultiExp(multiexp, Math.max(exponentiationWindowSize, multiexp.getMinPrecomputedWindowSize()));
        if (containsDebugGroup) {
            ((DebugGroupImpl) multiexp.getTerms().get(0).getBase().getStructure()).addMultiExp(multiexp.getTerms().size());
        }
        return result;
        //TODO some multiexponentiation algorithms may be able to handle different windows sizes for each base.
        // Generally, using the minimum for window size is "safe", but not necessarily clever performance-wise. Example: \prof h_i^x_i * (g^a)^b. The latter has no precomputation at all (even if g may have it), so ...
    }

    public GroupElementImpl compute(GroupElementImpl base, BigInteger exponent, SmallExponentPrecomputation precomputation) {
        GroupElementImpl result = ExponentiationAlgorithms.wnafExp(base, exponent, precomputation, exponentiationWindowSize);
        if (containsDebugGroup) {
            ((DebugGroupImpl) base.getStructure()).incrementNumExps();
        }
        return result;
    }

    public int getExponentiationWindowSize() {
        return exponentiationWindowSize;
    }

    public void setExponentiationWindowSize(int exponentiationWindowSize) {
        this.exponentiationWindowSize = exponentiationWindowSize;
    }

    public int getPrecomputationWindowSize() {
        return precomputationWindowSize;
    }

    public void setPrecomputationWindowSize(int precomputationWindowSize) {
        this.precomputationWindowSize = precomputationWindowSize;
    }
}
