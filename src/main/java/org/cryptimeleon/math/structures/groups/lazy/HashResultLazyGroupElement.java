package org.cryptimeleon.math.structures.groups.lazy;

/**
 * Represents the result of hashing a byte array to some structure.
 */
class HashResultLazyGroupElement extends LazyGroupElement {
    protected byte[] preimage;
    protected HashIntoLazyGroup hash;

    public HashResultLazyGroupElement(HashIntoLazyGroup hash, byte[] preimage) {
        super(hash.target);
        this.preimage = preimage;
        this.hash = hash;
    }

    @Override
    protected void computeConcreteValue() {
        setConcreteValue(hash.impl.hashIntoGroupImpl(preimage));
    }
}
