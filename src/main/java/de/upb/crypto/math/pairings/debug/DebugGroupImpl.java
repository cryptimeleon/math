package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StringRepresentation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Optional;

/**
 * A group used for debugging purposes. Really fast, but
 * DLOG is trivial in this group.
 * <p>
 * Concretely, the group is (Zn, +).
 * This group does support a bilinear map, namely e(a,b) = a*b.
 */
public class DebugGroupImpl implements GroupImpl {
    protected String name;
    protected Zn zn;

    /**
     * Instantiates the debug group (Zn,+)
     *
     * @param name a unique name for this group. Group operations only work between Groups with the same name (and same n)
     * @param n    the size of Zn
     */
    public DebugGroupImpl(String name, BigInteger n) {
        zn = new Zn(n);
        this.name = name;
    }

    public DebugGroupImpl(Representation repr) {
        this.zn = new Zn(repr.obj().get("n").bigInt().get());
        this.name = repr.obj().get("name").str().get();
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation repr = new ObjectRepresentation();
        repr.put("name", new StringRepresentation(name));
        repr.put("n", new BigIntegerRepresentation(zn.size()));

        return repr;
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
    public boolean equals(Object obj) {
        return obj instanceof DebugGroupImpl && ((DebugGroupImpl) obj).name.equals(this.name) && ((DebugGroupImpl) obj).zn.equals(this.zn);
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

    /**
     * Wraps an RingAdditiveGroupElement into a DebugGroupElement
     */
    protected DebugGroupElementImpl wrap(Zn.ZnElement elem) {
        return new DebugGroupElementImpl(this, elem);
    }
}
