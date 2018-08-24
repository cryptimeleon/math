package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representable;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Optional;

/**
 * A multiplicative subgroup of an ExtensionField.
 * When initialized with (finite) ExtensionField f and prime number "size",
 * it represents the unique subgroup of f* of size "size".
 */
public abstract class PairingTargetGroup implements Group, Representable {

    protected ExtensionField fieldOfDefinition;
    protected BigInteger size;
    private PairingTargetGroupElement generator = null; //not part of this object's state. Only used for optimization.

    public ExtensionField getFieldOfDefinition() {
        return fieldOfDefinition;
    }

    /**
     * Construct target group as subgroup of field f of given size.
     * If size does not divide size of the multiplicative group of f, an exception is thrown.
     *
     * @param f    - field for embedding to new group
     * @param size - size of new group
     */
    public PairingTargetGroup(ExtensionField f, BigInteger size) {
        if (!f.size().subtract(BigInteger.ONE).mod(size).equals(BigInteger.ZERO)) {
            throw new IllegalArgumentException("Size of subgroup has to divide size of field");
        }
        if (!size.isProbablePrime(100)) {
            throw new IllegalArgumentException("Expect prime size");
        }
        this.fieldOfDefinition = f;
        this.size = size;
    }

    public BigInteger getCofactor() {
        return fieldOfDefinition.size().divide(this.size());
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return size;
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation r = new ObjectRepresentation();
        r.put("field", this.getFieldOfDefinition().getRepresentation());
        r.put("size", new BigIntegerRepresentation(this.size()));
        return r;

    }

    protected void init(Representation r) {


    }

    public PairingTargetGroup(Representation r) {
        this(new ExtensionField(r.obj().get("field")), r.obj().get("size").bigInt().get());
    }

    @Override
    public PairingTargetGroupElement getNeutralElement() {
        return getElement((ExtensionFieldElement) fieldOfDefinition.getOneElement());
    }

    @Override
    public PairingTargetGroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        /*due to large cofactor, exponentiation of generator is more efficient than cofactor
         * exponentiation,
         *  at least unless we use structure of cofactor*/
        return (PairingTargetGroupElement) this.getGenerator().pow(
                (new Zp(this.size()).getUniformlyRandomElement()));
    }

    @Override
    public PairingTargetGroupElement getGenerator() {
        if (generator == null) {
            FieldElement fe;
            do {
                fe = this.getFieldOfDefinition().getUniformlyRandomElement();
                fe = fe.pow(this.getFieldOfDefinition().size().subtract(BigInteger.ONE).divide(this.size()));


            } while (fe.isOne());

            this.generator = this.getElement((ExtensionFieldElement) fe);
        }
        return this.generator;
    }

    @Override
    public PairingTargetGroupElement getElement(Representation repr) {
        return getElement((ExtensionFieldElement) fieldOfDefinition.getElement(repr));
    }

    /**
     * Concrete implementations should wrap the extension field element into their subclass.
     */
    public abstract PairingTargetGroupElement getElement(ExtensionFieldElement fe);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldOfDefinition == null) ? 0 : fieldOfDefinition.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PairingTargetGroup)) {
            return false;
        }

        PairingTargetGroup sg = (PairingTargetGroup) o;

        if (!sg.getFieldOfDefinition().equals(getFieldOfDefinition())) {
            return false;
        }

        if (!sg.size().equals(size())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Multiplicative subgroup of " + this.getFieldOfDefinition() + " of size " + this.size();
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return this.fieldOfDefinition.getUniqueByteLength();
    }

    @Override
    public boolean isCommutative() {
        return true;
    }

    @Override
    public int estimateCostOfInvert() {
        return 200;
    }
}
