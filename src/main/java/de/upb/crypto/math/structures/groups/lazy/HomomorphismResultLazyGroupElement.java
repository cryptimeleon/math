package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

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
