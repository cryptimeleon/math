package de.upb.crypto.math.lazy;

import de.upb.crypto.math.expressions.evaluator.NaiveGroupElementExpressionEvaluator;
import de.upb.crypto.math.expressions.group.GroupElementExpressionEvaluator;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.annotations.AnnotatedRepresentationUtil;
import de.upb.crypto.math.serialization.annotations.Represented;

import java.math.BigInteger;
import java.util.Optional;

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

    /**
     * Instantiates this lazy group using the supplied base group.
     *
     * @param group                         the group that is wrapped by this LazyGroup.
     */
    public LazyGroup(Group group) {
        this.baseGroup = group;
        init();
    }

    /**
     * Constructor for the target group of a pairing.
     */
    protected LazyGroup(Group group, LazyPairing pairing) {
        this.baseGroup = group;
        this.associatedPairing = pairing;
        init();
    }

    public LazyGroup(Representation repr) {
        AnnotatedRepresentationUtil.restoreAnnotatedRepresentation(repr, this);
        init();
    }

    /**
     * Stuff that happens in all constructors
     */
    private void init() {
        if (baseGroup.size() == null)
            throw new RuntimeException("LazyGroup only works for finite groups");
    }

    @Override
    public GroupElement getNeutralElement() {
        return new LazyGroupElement(this);
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return baseGroup.size();
    }

    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return new LazyGroupElement(this, baseGroup.getUniformlyRandomElement());
    }

    @Override
    public GroupElement getUniformlyRandomNonNeutral() throws UnsupportedOperationException {
        return new LazyGroupElement(this, baseGroup.getUniformlyRandomNonNeutral());
    }

    @Override
    public GroupElement getElement(Representation repr) {
        return new LazyGroupElement(this, baseGroup.getElement(repr));
    }

    @Override
    public GroupElement getGenerator() throws UnsupportedOperationException {
        return new LazyGroupElement(this, baseGroup.getGenerator());
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
    public GroupElementExpressionEvaluator getExpressionEvaluator() {
        return new NaiveGroupElementExpressionEvaluator();
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return baseGroup.getUniqueByteLength();
    }

    @Override
    public Representation getRepresentation() {
        return AnnotatedRepresentationUtil.putAnnotatedRepresentation(this);
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
