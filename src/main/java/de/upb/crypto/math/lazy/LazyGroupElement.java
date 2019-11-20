package de.upb.crypto.math.lazy;

import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

public class LazyGroupElement implements GroupElement {
    protected LazyGroup group;

    /**
     * The symbolic value of this element
     */
    protected GroupElementExpression expr;

    protected LazyGroupElement(LazyGroup group) {
        this.group = group;
        this.expr = group.baseGroup.expr();
    }

    protected LazyGroupElement(LazyGroup group, GroupElementExpression expr) {
        this(group);
        this.expr = expr;
    }

    protected LazyGroupElement(LazyGroup group, GroupElement elem) {
        this(group, elem.expr());
    }

    @Override
    public LazyGroup getStructure() {
        return group;
    }

    @Override
    public LazyGroupElement inv() {
        return new LazyGroupElement(group, expr.inv());
    }

    @Override
    public LazyGroupElement op(Element e) throws IllegalArgumentException {
        return new LazyGroupElement(group, expr.op(((LazyGroupElement) e).expr));
    }

    @Override
    public LazyGroupElement pow(BigInteger k) {
        return new LazyGroupElement(group, expr.pow(k));
    }

    @Override
    public LazyGroupElement pow(Zn.ZnElement k) {
        return pow(k.getInteger());
    }

    @Override
    public boolean isNeutralElement() {
        return expr.evaluate().isNeutralElement();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return expr.evaluate().updateAccumulator(accumulator);
    }

    @Override
    public Representation getRepresentation() {
        return expr.evaluate().getRepresentation();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LazyGroupElement))
            return false;

        return this.op(((LazyGroupElement) obj).inv()).expr.evaluate().isNeutralElement();
    }

    @Override
    public int hashCode() {
        return expr.evaluate().hashCode();
    }

    /**
     * Returns true if the two elements are equal on an expression level,
     * i.e. the LazyGroupElement will definitely (symbolically) evaluate to
     * the same value (based on LeafGroupElements/PairingEvaluationElement, not on intermediate results).
     * This should be much more efficient than evaluating the element.
     */
    protected boolean equalsOnExpressionLevel(LazyGroupElement other) {
        return expr.equals(other.expr);
    }

    @Override
    public String toString() {
        return "[lazy element]";
    }
}
