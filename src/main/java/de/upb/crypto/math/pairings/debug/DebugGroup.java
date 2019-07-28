package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
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
public class DebugGroup implements Group {
    protected String name;
    protected Zn zn;

    /**
     * Instantiates the debug group (Zn,+)
     *
     * @param name a unique name for this group. Group operations only work between Groups with the same name (and same n)
     * @param n    the size of Zn
     */
    public DebugGroup(String name, BigInteger n) {
        zn = new Zn(n);
        this.name = name;
    }

    public DebugGroup(Representation repr) {
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
    public GroupElement getNeutralElement() {
        return wrap(zn.getZeroElement());
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        DebugGroupLogger.log(name, "chooseRnd");
        return wrap(zn.getUniformlyRandomElement());
    }

    @Override
    public GroupElement getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        DebugGroupLogger.log(name, "chooseRnd");
        Zn.ZnElement result;
        do {
            result = zn.getUniformlyRandomElement();
        } while (result.isZero());

        return wrap(result);
    }

    @Override
    public GroupElement getElement(Representation repr) {
        return wrap(zn.getElement(repr));
    }

    @Override
    public GroupElement getGenerator() throws UnsupportedOperationException {
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
        return obj instanceof DebugGroup && ((DebugGroup) obj).name.equals(this.name) && ((DebugGroup) obj).zn.equals(this.zn);
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
    public int estimateCostOfInvert() {
        return 100;
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return zn.size();
    }

    /**
     * Wraps an RingAdditiveGroupElement into a DebugGroupElement
     */
    protected DebugGroupElement wrap(Zn.ZnElement elem) {
        return new DebugGroupElement(this, elem);
    }
}
