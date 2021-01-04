package de.upb.crypto.math.structures.groups.lazy;

/**
 * Represents the result of hashing a byte array to some structure.
 */
public class HashResultLazyGroupElement extends LazyGroupElement {
    protected byte[] preimage;
    protected LazyHashIntoStructure hash;

    public HashResultLazyGroupElement(LazyHashIntoStructure hash, byte[] preimage) {
        super(hash.target);
        this.preimage = preimage;
        this.hash = hash;
    }

    @Override
    protected void computeConcreteValue() {
        setConcreteValue(hash.impl.hashIntoGroupImpl(preimage));
    }
}
