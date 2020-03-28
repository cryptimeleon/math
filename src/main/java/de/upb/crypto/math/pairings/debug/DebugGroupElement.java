package de.upb.crypto.math.pairings.debug;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.AbstractGroupElement;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;

/**
 * @see DebugGroup
 */
public class DebugGroupElement extends AbstractGroupElement implements GroupElement {
    protected Zn.ZnElement elem;
    protected DebugGroup group;

    public DebugGroupElement(DebugGroup group, Zn.ZnElement elem) {
        this.elem = elem;
        this.group = group;
    }

    @Override
    public Group getStructure() {
        return group;
    }

    @Override
    public GroupElement inv() {
        //DebugGroupLogger.log(group.name, "inv");
        return group.wrap(elem.neg());
    }

    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        if (!(e instanceof DebugGroupElement) || !((DebugGroupElement) e).group.equals(group))
            throw new IllegalArgumentException("Incompatible groups. LHS: "
                    + group.name + "("
                    + group.size()
                    + "), RHS: "
                    + (e instanceof DebugGroupElement ? ((DebugGroupElement) e).group.name + "(" + ((DebugGroupElement) e).group.size() + ")"
                    : e == null ? "null" : e.getStructure())
            );
        //DebugGroupLogger.log(group.name, "op");

        return group.wrap(elem.add(((DebugGroupElement) e).elem));
    }

    @Override
    public GroupElement pow(BigInteger k) {
        DebugGroupLogger.log(group.name, "pow");
        return group.wrap(elem.mul(group.zn.createZnElement(k)));
    }

    @Override
    public GroupElement pow(Zn.ZnElement k) {
        if (!k.getStructure().size().mod(group.size()).equals(BigInteger.ZERO))
            System.err.println("Warning: used element of Z_" + k.getStructure().size() + "  in the exponent of a group of size " + group.size());

        return pow(k.getInteger());
    }

    @Override
    public boolean isNeutralElement() {
        return elem.isZero();
    }

    @Override
    public String toString() {
        return group.name + "--" + elem.toString();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return elem.updateAccumulator(accumulator);
    }

    @Override
    public Representation getRepresentation() {
        return elem.getRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebugGroupElement that = (DebugGroupElement) o;
        return Objects.equals(elem, that.elem) &&
                Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elem);
    }
}
