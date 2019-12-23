package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Function;

/**
 * Implements a subgroup over the Weierstrass Curve.
 * More specifically: The set of Torsion points E[getSize()] = {P\in PairingGroup | P.pow(getSize()) = 1}
 * on an elliptic curve over fieldOfDefinition.
 *
 * @author Jan
 */
public abstract class PairingSourceGroup implements WeierstrassCurve {

    protected BigInteger size;
    protected BigInteger cofactor;
    protected EllipticCurvePoint generator;
    protected Field field;
    protected Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor;

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
     * @param cofactor the number c such that size * c = number of points on the WeierstrassCurve over fieldOfDefinition
     */
    private void create(BigInteger size, BigInteger cofactor, FieldElement a1, FieldElement a2,
                        FieldElement a3, FieldElement a4, FieldElement a6,
                        Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        this.size = size;
        this.cofactor = cofactor;
        this.field = a1.getStructure();
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.a6 = a6;
        this.ecpCoordConstructor = ecpCoordConstructor;
    }

    public PairingSourceGroup(BigInteger size, BigInteger cofactor, FieldElement a1, FieldElement a2,
                              FieldElement a3, FieldElement a4, FieldElement a6,
                              Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        create(size, cofactor, a1, a2, a3, a4, a6, ecpCoordConstructor);
    }

    public PairingSourceGroup(BigInteger size, BigInteger cofactor, FieldElement a4,
                              FieldElement a6, Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        create(size, cofactor, a4.getStructure().getZeroElement(), a4.getStructure().getZeroElement(),
                a4.getStructure().getZeroElement(), a4, a6, ecpCoordConstructor);
    }


    public PairingSourceGroup(Representation repr, Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        ObjectRepresentation or = (ObjectRepresentation) repr;
        this.size = ((BigIntegerRepresentation) or.get("size")).get();
        this.cofactor = ((BigIntegerRepresentation) or.get("cofactor")).get();
        this.field = (Field) ((RepresentableRepresentation) or.get("field")).recreateRepresentable();
        this.a1 = this.field.getElement(or.get("a1"));
        this.a2 = this.field.getElement(or.get("a2"));
        this.a3 = this.field.getElement(or.get("a3"));
        this.a4 = this.field.getElement(or.get("a4"));
        this.a6 = this.field.getElement(or.get("a6"));
        this.ecpCoordConstructor = ecpCoordConstructor;

        this.setGenerator(this.getElement(or.get("generator")));
    }

    @Override
    public EllipticCurvePoint getGenerator() {
        return this.generator;
    }

    public void setGenerator(EllipticCurvePoint generator) {
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

    @Override
    public Function<WeierstrassCurve, AbstractECPCoordinate> getEcpCoordConstructor() {
        return this.ecpCoordConstructor;
    }

    /**
     * Tests if (x,y) is on curve that defines this group. Does not check subgroup membership.
     *
     * @param x - x-coordinate of point that shall be checked
     * @param y - y-coordinate of point that shall be checked
     * @return true if p fulfills equation of this group
     */
    public boolean isOnCurve(FieldElement x, FieldElement y) {
        // FieldElement x,y;
        //
        // x = p.getX();
        // y = p.getY();

        /*
         * check y^2+a_1 xy + a_3 y = x^3+a_2 x^2 + a_4 x + a_6
         *
         * rewritten as
         *
         * ((a_1 x + a_3)y + y)y = x ( x ( x+a_2 )+a_4)+a_6
         */
        return x.mul(getA1()).add(getA3()).mul(y).add(y).mul(y).equals(x.add(getA2()).mul(x).add(getA4()).mul(x).add(getA6()));
    }

    /**
     * Tests if (x,y) is a member of this (sub)group.
     * <p>
     * This function first checks of (x,y) defines a point on the curve that defines this group. Then a subgroup membership test is performed by multiplication either with the group order or with the cofactor. If both are large, this is an expensive
     * operation.
     * <p>
     * For cryptographic protocols where x and y are inputs to the algorithm, a subgroup membership test is mandatory to avoid small subgroup attacks, twist attacks,...
     *
     * @param x - x-coordinate of point to be checked
     * @param y - y-coordinate of point to be checked
     * @return true if (x,y) is on curve
     */
    public boolean isMember(FieldElement x, FieldElement y) {
        //Ensure there is only one subgroup of size this.size()
        if (!this.size().gcd(this.getCofactor()).equals(BigInteger.ONE)) {
            throw new IllegalArgumentException("Require cofactor coprime to order of subgroup.");
        }

        //Check if point is on curve
        if (!isOnCurve(x, y))
            return false;

        //Check subgroup membership
        return this.getElement(x, y).pow(this.size()).isNeutralElement();
    }

    public Field getFieldOfDefinition() {
        return field;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((cofactor == null) ? 0 : cofactor.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) { //TODO
        if (!(o instanceof PairingSourceGroup)) {
            return false;
        }

        PairingSourceGroup sg = (PairingSourceGroup) o;

        if (!sg.getA6().equals(getA6())) {
            return false;
        }

        if (!sg.size().equals(size())) {
            return false;
        }

        if (!sg.getCofactor().equals(getCofactor())) {
            return false;
        }

        return true;
    }


    @Override
    public EllipticCurvePoint getUniformlyRandomElement() throws UnsupportedOperationException {
        Zp zp = new Zp(this.size());
        return (EllipticCurvePoint) this.getGenerator().pow(zp.getUniformlyRandomElement().getInteger());
    }

    @Override
    public EllipticCurvePoint getElement(Representation repr) {
        ObjectRepresentation or = (ObjectRepresentation) repr;
        FieldElement x = getFieldOfDefinition().getElement(or.get("x"));
        FieldElement y = getFieldOfDefinition().getElement(or.get("y"));
        FieldElement z = getFieldOfDefinition().getElement(or.get("z"));
        if (z.isZero())
            return (EllipticCurvePoint) getNeutralElement();

        return getElement(x, y);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        //reserve space for x,y,z coordinate
        return getFieldOfDefinition().getUniqueByteLength().map(k -> k * 3);
    }


    public abstract EllipticCurvePoint getElement(FieldElement x, FieldElement y);

    @Override
    public int estimateCostOfInvert() {
        return 25;
    }

    /**
     * Maps a point (x,y) on the curve into the subgroup represented by this object.
     * Note that pow() on a EllipticCurvePoint does not work if pow() depends on
     * the group size (which it may, e.g., to first reduce the exponent mod size()).
     *
     * @param x first coordinate of the point to map
     * @param y second coordinate of the point to map
     * @return a point in this subgroup
     */
    protected EllipticCurvePoint cofactorMultiplication(FieldElement x, FieldElement y) {
        EllipticCurvePoint elem = getElement(x, y);

        GroupElement result = getNeutralElement();
        for (int i = cofactor.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (cofactor.testBit(i))
                result = result.op(elem);
        }
        return (EllipticCurvePoint) result;
    }
}
