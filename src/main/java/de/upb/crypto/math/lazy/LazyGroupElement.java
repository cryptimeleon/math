package de.upb.crypto.math.lazy;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.interfaces.structures.PowProductExpression;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LazyGroupElement implements GroupElement {
    protected LazyGroup group;

    /**
     * value of this element. May be null until it's calculated.
     */
    protected volatile GroupElement value = null;

    /**
     * Number of children of this group element
     */
    private AtomicInteger referenceCount = new AtomicInteger();

    /**
     * cached output of putProductBasedOnLeafs, containing the
     * expressions (over the base group) that compute this element
     */
    protected PowProductExpression productBasedOnLeafs;

    /**
     * cached output of putProductBasedOnLeafs, containing the
     * expressions (over the base group) that compute this element.
     * <p>
     * This is only set if group.associatedPairing != null
     */
    protected PairingProductExpression pairingProductBasedOnLeafs;

    protected LazyGroupElement(LazyGroup group) {
        this.group = group;
    }

    protected void registerReference() {
        int count = referenceCount.incrementAndGet();
        if (count == 2 && value == null)
            group.precompute(this);
    }

    @Override
    public LazyGroup getStructure() {
        return group;
    }

    @Override
    public LazyGroupElement inv() {
        return pow(BigInteger.valueOf(-1));
    }

    @Override
    public LazyGroupElement op(Element e) throws IllegalArgumentException {
        return group.cached(new OpGroupElement(group, this, (LazyGroupElement) e));
    }

    @Override
    public LazyGroupElement op(PowProductExpression expr) throws IllegalArgumentException {
        return group.cached(op(group.evaluate(expr)));
    }

    @Override
    public LazyGroupElement pow(BigInteger k) {
        return group.cached(new PowGroupElement(group, this, k));
    }

    @Override
    public LazyGroupElement pow(Zn.ZnElement k) {
        return pow(k.getInteger());
    }

    protected void evaluate() {
        if (value != null)
            return;
        synchronized (this) {
            if (value != null) //avoid double evaluation by waiting threads
                return;

            //Basic group elements
            PowProductExpression gexpr = emptyPowProductExpression();
            PairingProductExpression pexpr = emptyPairingProductExpression();
            putProduct(gexpr, pexpr);
            GroupElement val = gexpr.staticOptimization().evaluate();
            if (pexpr != null)
                val = val.op(pexpr.staticOptimization().evaluate());

            value = val;
        }
    }

    public GroupElement getValue() {
        evaluate();
        return value;
    }

    @Override
    public boolean isNeutralElement() {
        return getValue().isNeutralElement();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return getValue().updateAccumulator(accumulator);
    }

    @Override
    public Representation getRepresentation() {
        return getValue().getRepresentation();
    }

    /**
     * Puts a way to compute the lazy element as a product of exponentiations (cf. Group::evaluate)
     * into prod. The Elements in the map must be from the base group.
     *
     * @param prod        the expression over group elements
     * @param pairingProd the expression over pairing evaluations. Can be null if group.associatedPairing == null
     */
    protected abstract void putProduct(PowProductExpression prod, PairingProductExpression pairingProd);


    /**
     * Puts a way to compute the lazy element as a product of exponentiations (cf. Group::evaluate)
     * into prod and pairingProd.
     * In contrast to putProduct, this method does not stop recursing as soon as it hits a node
     * with value != null, but stops recursing only when it hits a leaf.
     *
     * @param prod        the expression over group elements
     * @param pairingProd the expression over pairing evaluations. Can be null if group.associatedPairing == null
     */
    protected abstract void putProductBasedOnLeafs(PowProductExpression prod, PairingProductExpression pairingProd);

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LazyGroupElement))
            return false;

        if (this.value != null)
            return value.equals(((LazyGroupElement) obj).getValue());
        if (((LazyGroupElement) obj).value != null)
            return ((LazyGroupElement) obj).value.equals(this.getValue());

        //If nothing is cached, the following may be better as it may allow for optimization on both sides of the
        // equality
        return this.op(((LazyGroupElement) obj).inv()).getValue().isNeutralElement();
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    /**
     * Returns true if the two elements are equal on an expression level,
     * i.e. the LazyGroupElement will definitely (symbolically) evaluate to
     * the same value (based on LeafGroupElements/PairingEvaluationElement, not on intermediate results).
     * This should be much more efficient than evaluating the element.
     */
    protected boolean equalsOnExpressionLevel(LazyGroupElement other) {
        computeProductsBasedOnLeafs();
        if (other.productBasedOnLeafs == null || group.associatedPairing != null && other.pairingProductBasedOnLeafs == null)
            return other.equalsOnExpressionLevel(this);

        return productBasedOnLeafs.equals(other.productBasedOnLeafs) && Objects
                .equals(pairingProductBasedOnLeafs, other.pairingProductBasedOnLeafs);
    }

    /**
     * Returns a hashCode consistent with equalsOnExpressionLevel
     */
    protected int hashCodeOnExpressionLevel() {
        computeProductsBasedOnLeafs();
        return productBasedOnLeafs.hashCode() + Objects.hashCode(pairingProductBasedOnLeafs);
    }

    @Override
    public String toString() {
        //evaluate(), then value.toString() would be somewhat awkward for debuggers.
        if (value != null) {
            return value.toString();
        }
        return "[lazy unevaluated]";
    }

    /**
     * Shortcut for subclass implementation
     */
    protected PairingProductExpression emptyPairingProductExpression() {
        return group.associatedPairing != null ?
                new PairingProductExpression(group.associatedPairing.baseBilinearMap) : null;
    }

    protected PowProductExpression emptyPowProductExpression() {
        return new PowProductExpression(group.baseGroup);
    }

    /**
     * Computes productBasedOnLeafs and pairingProductBasedOnLeafs
     */
    private void computeProductsBasedOnLeafs() {
        if (productBasedOnLeafs == null || group.associatedPairing != null && pairingProductBasedOnLeafs == null) {
            PowProductExpression expr = emptyPowProductExpression();
            PairingProductExpression pexpr = emptyPairingProductExpression();
            putProductBasedOnLeafs(expr, pexpr);
            productBasedOnLeafs = expr;
            pairingProductBasedOnLeafs = pexpr;
        }
    }
}
