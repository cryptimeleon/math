package de.upb.crypto.math.structures.groups.lazy;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

public class HashResultLazyGroupElement extends LazyGroupElement {
    protected byte[] preimage;
    protected LazyHashIntoStructure hash;

    public HashResultLazyGroupElement(LazyHashIntoStructure hash, byte[] preimage) {
        super(hash.target);
        this.preimage = preimage;
        this.hash = hash;
    }

    @Override
    protected GroupElementImpl computeConcreteValue() {
        return hash.impl.hashIntoGroupImpl(preimage);
    }
}
