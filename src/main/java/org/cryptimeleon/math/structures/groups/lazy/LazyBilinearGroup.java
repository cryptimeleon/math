package org.cryptimeleon.math.structures.groups.lazy;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroup;
import org.cryptimeleon.math.structures.groups.elliptic.BilinearGroupImpl;

import java.util.Objects;

/**
 * A {@link BilinearGroupImpl} wrapper implementing deferred (lazy) evaluation.
 * <p>
 * Allows for additional optimizations using information about the operations being applied.
 * <p>
 * For more information, see the <a href="https://upbcuk.github.io/docs/lazy-eval.html">documentation</a>.
 */
public class LazyBilinearGroup implements BilinearGroup {
    @Represented
    protected BilinearGroupImpl impl;

    LazyGroup g1, g2, gt;
    LazyGroupHomomorphism homG2toG1;
    LazyBilinearMap map;
    HashIntoLazyGroup hashIntoG1, hashIntoG2, hashIntoGt;

    public LazyBilinearGroup(BilinearGroupImpl impl) {
        this.impl = impl;
        instantiateBasicStuff();
    }

    public LazyBilinearGroup(Representation repr) {
        ReprUtil.deserialize(this, repr);
        instantiateBasicStuff();
    }

    protected void instantiateBasicStuff() {
        g1 = new LazyGroup(impl.getG1());
        g2 = new LazyGroup(impl.getG2());
        gt = new LazyGroup(impl.getGT());

        map = new LazyBilinearMap(g1, g2, gt, impl.getBilinearMap());
        try {
            homG2toG1 = new LazyGroupHomomorphism(g1, impl.getHomomorphismG2toG1());
        } catch (UnsupportedOperationException e) {
            homG2toG1 = null;
        }

        try {
            hashIntoG1 = new HashIntoLazyGroup(impl.getHashIntoG1(), g1);
        } catch (UnsupportedOperationException e) {
            hashIntoG1 = null;
        }

        try {
            hashIntoG2 = new HashIntoLazyGroup(impl.getHashIntoG2(), g2);
        } catch (UnsupportedOperationException e) {
            hashIntoG2 = null;
        }

        try {
            hashIntoGt = new HashIntoLazyGroup(impl.getHashIntoGT(), gt);
        } catch (UnsupportedOperationException e) {
            hashIntoGt = null;
        }
    }

    @Override
    public LazyGroup getG1() {
        return g1;
    }

    @Override
    public LazyGroup getG2() {
        return g2;
    }

    @Override
    public LazyGroup getGT() {
        return gt;
    }

    @Override
    public LazyBilinearMap getBilinearMap() {
        return map;
    }

    @Override
    public LazyGroupHomomorphism getHomomorphismG2toG1() throws UnsupportedOperationException {
        if (homG2toG1 == null)
            throw new UnsupportedOperationException("No homomorphism available");
        return homG2toG1;
    }

    @Override
    public HashIntoLazyGroup getHashIntoG1() throws UnsupportedOperationException {
        if (hashIntoG1 == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoG1;
    }

    @Override
    public HashIntoLazyGroup getHashIntoG2() throws UnsupportedOperationException {
        if (hashIntoG2 == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoG2;
    }

    @Override
    public HashIntoLazyGroup getHashIntoGT() throws UnsupportedOperationException {
        if (hashIntoGt == null)
            throw new UnsupportedOperationException("No hash available");
        return hashIntoGt;
    }

    @Override
    public Integer getSecurityLevel() {
        return impl.getSecurityLevel();
    }

    @Override
    public Type getPairingType() {
        return impl.getPairingType();
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LazyBilinearGroup that = (LazyBilinearGroup) o;
        return impl.equals(that.impl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impl);
    }

    @Override
    public String toString() {
        return "LazyBilinearGroup{" +
                "impl=" + impl +
                '}';
    }
}
