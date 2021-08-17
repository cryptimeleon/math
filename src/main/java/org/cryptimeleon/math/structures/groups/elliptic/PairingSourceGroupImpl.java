package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.serialization.BigIntegerRepresentation;
import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.rings.Field;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.zn.Zp;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * Implements a subgroup over the Weierstrass Curve.
 * More specifically: The set of Torsion points E[getSize()] = {P\in PairingGroup | P.pow(getSize()) = 1}
 * on an elliptic curve over fieldOfDefinition.
 */
public abstract class PairingSourceGroupImpl implements WeierstrassCurve {

    protected BigInteger size;
    protected BigInteger cofactor;
    protected PairingSourceGroupElement generator;
    protected Field field;

    private FieldElement a1, a2, a3, a4, a6;

    public BigInteger getSize() {
        return size;
    }

    public FieldElement getA1() {
        return a1;
    }

    public FieldElement getA2() {
        return a2;
    }

    public FieldElement getA3() {
        return a3;
    }

    public FieldElement getA4() {
        return a4;
    }

    public FieldElement getA6() {
        return a6;
    }

    /**
     * Instantiates the group
     *
     * @param size     the desired size of the group
     * @param cofactor the number c such that size * c = number of points on the WeierstrassCurve over
     *                 fieldOfDefinition
     */
    private void create(BigInteger size, BigInteger cofactor, FieldElement a1, FieldElement a2, FieldElement a3, FieldElement a4, FieldElement a6) {
        this.size = size;
        this.cofactor = cofactor;
        this.field = a1.getStructure();
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.a6 = a6;
    }

    public PairingSourceGroupImpl(BigInteger size, BigInteger cofactor, FieldElement a1, FieldElement a2, FieldElement a3, FieldElement a4, FieldElement a6) {
        create(size, cofactor, a1, a2, a3, a4, a6);
    }

    public PairingSourceGroupImpl(BigInteger size, BigInteger cofactor, FieldElement a4, FieldElement a6) {
        create(size, cofactor, a4.getStructure().getZeroElement(), a4.getStructure().getZeroElement(), a4.getStructure().getZeroElement(), a4, a6);
    }


    public PairingSourceGroupImpl(Representation repr) {
        ObjectRepresentation or = (ObjectRepresentation) repr;
        this.size = ((BigIntegerRepresentation) or.get("size")).get();
        this.cofactor = ((BigIntegerRepresentation) or.get("cofactor")).get();
        this.field = (Field) ((RepresentableRepresentation) or.get("field")).recreateRepresentable();
        this.a1 = this.field.restoreElement(or.get("a1"));
        this.a2 = this.field.restoreElement(or.get("a2"));
        this.a3 = this.field.restoreElement(or.get("a3"));
        this.a4 = this.field.restoreElement(or.get("a4"));
        this.a6 = this.field.restoreElement(or.get("a6"));

        this.setGenerator(this.restoreElement(or.get("generator")));
    }

    @Override
    public PairingSourceGroupElement getGenerator() {
        return this.generator;
    }

    public void setGenerator(PairingSourceGroupElement generator) {
        this.generator = generator;
    }

    /**
     * Returns cofactor of this subgroup.
     *
     * @return
     */
    public BigInteger getCofactor() {
        return cofactor;
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return size;
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation or = new ObjectRepresentation();
        or.put("size", new BigIntegerRepresentation(size));
        or.put("cofactor", new BigIntegerRepresentation(getCofactor()));
        or.put("generator", this.getGenerator().getRepresentation());
        or.put("field", new RepresentableRepresentation(field));
        or.put("a1", a1.getRepresentation());
        or.put("a2", a2.getRepresentation());
        or.put("a3", a3.getRepresentation());
        or.put("a4", a4.getRepresentation());
        or.put("a6", a6.getRepresentation());
        return or;
    }

    /**
     * Tests if (x,y) is a member of this (sub)group.
     * <p>
     * Does NOT check whether point is on the curve. This needs to be done separately before.
     * <p>
     * For cryptographic protocols where x and y are inputs to the algorithm,
     * a subgroup membership test is mandatory to avoid small subgroup attacks, twist attacks, etc.
     *
     * @param x x-coordinate of point to be checked
     * @param y y-coordinate of point to be checked
     * @return true if (x,y) is member of this (sub)group, false otherwise
     */
    public boolean isMember(FieldElement x, FieldElement y) {
        //Ensure there is only one subgroup of size this.size()
        if (!this.size().gcd(this.getCofactor()).equals(BigInteger.ONE)) {
            throw new IllegalArgumentException("Require cofactor coprime to order of subgroup.");
        }

        // Check subgroup membership by exponentiating with subgroup order
        // Need custom exponentiation since pow() could have special handling for size() parameter
        //   e.g. hardcoded 1 (since it may assume membership in group already)
        PairingSourceGroupElement elem = getElement(x, y);
        BigInteger size = this.size();
        GroupElementImpl result = getNeutralElement();
        for (int i = size.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (size.testBit(i))
                result = result.op(elem);
        }
        return result.isNeutralElement();
    }

    public Field getFieldOfDefinition() {
        return field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairingSourceGroupImpl that = (PairingSourceGroupImpl) o;
        return size.equals(that.size) &&
                cofactor.equals(that.cofactor) &&
                generator.equals(that.generator) &&
                field.equals(that.field) &&
                Objects.equals(a1, that.a1) &&
                Objects.equals(a2, that.a2) &&
                Objects.equals(a3, that.a3) &&
                Objects.equals(a4, that.a4) &&
                Objects.equals(a6, that.a6);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size);
    }

    @Override
    public PairingSourceGroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        Zp zp = new Zp(this.size());
        return (PairingSourceGroupElement) this.getGenerator().pow(zp.getUniformlyRandomElement().asInteger());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks that the deserialized point is on the curve and in this (sub)group.
     * If not, a {@link IllegalArgumentException} is thrown.
     *
     * @throws IllegalArgumentException if the deserialized point is either not on the curve or not member of this
     *                                  (sub)group.
     */
    @Override
    public PairingSourceGroupElement restoreElement(Representation repr) {
        ObjectRepresentation or = (ObjectRepresentation) repr;
        FieldElement x = getFieldOfDefinition().restoreElement(or.get("x"));
        FieldElement y = getFieldOfDefinition().restoreElement(or.get("y"));
        FieldElement z = getFieldOfDefinition().restoreElement(or.get("z"));
        if (z.isZero())
            return (PairingSourceGroupElement) getNeutralElement();
        // Check that point is on this curve
        if (!isOnCurve(x, y)) {
            throw new IllegalArgumentException("Point is not on the curve underlying this group");
        }
        // Check that point is in this group
        if (!isMember(x, y)) {
            throw new IllegalArgumentException("Element specified by given representation is not member of this group");
        }
        return getElement(x, y);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        //reserve space for x,y,z coordinate
        return getFieldOfDefinition().getUniqueByteLength().map(k -> k * 3);
    }


    public abstract PairingSourceGroupElement getElement(FieldElement x, FieldElement y);

    /**
     * Maps a point (x,y) on the curve into the subgroup represented by this object.
     * Note that pow() on a PairingSourceGroupElement does not work if pow() depends on
     * the group size (which it may, e.g., to first reduce the exponent mod size()).
     *
     * @param x first coordinate of the point to map
     * @param y second coordinate of the point to map
     * @return a point in this subgroup
     */
    protected PairingSourceGroupElement cofactorMultiplication(FieldElement x, FieldElement y) {
        PairingSourceGroupElement elem = getElement(x, y);

        GroupElementImpl result = getNeutralElement();
        for (int i = cofactor.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (cofactor.testBit(i))
                result = result.op(elem);
        }
        return (PairingSourceGroupElement) result;
    }
}
