package de.upb.crypto.math.structures.groups.lazy;

/**
 * Represents the result of applying a group homomorphism to some group element.
 */
public class HomomorphismResultLazyGroupElement extends LazyGroupElement {
    protected LazyGroupElement preimage;
    protected LazyGroupHomomorphism homomorphism;

    public HomomorphismResultLazyGroupElement(LazyGroupElement preimage, LazyGroupHomomorphism homomorphism) {
        super(homomorphism.targetGroup);
        this.preimage = preimage;
        this.homomorphism = homomorphism;
    }

    @Override
    protected void computeConcreteValue() {
        setConcreteValue(homomorphism.impl.apply(preimage.getConcreteValue()));
    }
}
