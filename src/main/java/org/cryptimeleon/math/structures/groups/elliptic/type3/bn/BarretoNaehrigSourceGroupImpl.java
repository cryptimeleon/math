package org.cryptimeleon.math.structures.groups.elliptic.type3.bn;

import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.elliptic.PairingSourceGroupElement;
import org.cryptimeleon.math.structures.groups.elliptic.PairingSourceGroupImpl;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

import java.math.BigInteger;

/**
 * Abstract class underlying both G1 and G2.
 * <p>
 * This class implements a subgroup of \(E:y^2=x^3+b\).
 */
abstract class BarretoNaehrigSourceGroupImpl extends PairingSourceGroupImpl {
    public BarretoNaehrigSourceGroupImpl(BigInteger size, BigInteger cofactor, ExtensionFieldElement a6) {
        super(size, cofactor, a6.getStructure().getZeroElement(), a6);
    }

    public BarretoNaehrigSourceGroupImpl(Representation r) {
        super(r);
    }

    protected PairingSourceGroupElement getUniformlyRandomElementOblivious() throws UnsupportedOperationException {
        do {
            /* get random y-coordinate */
            FieldElement y = getFieldOfDefinition().getUniformlyRandomElement();

            /*
             * for every y coordinate that leads to a point, ie, where y^2 -b is a cubic residue, there are three x-coordinates. Select one uniformly at random.
             */
            int sel = RandomGenerator.getRandomNumber(BigInteger.valueOf(3)).intValue();

            try {
                /*
                 * try if we can get a point on E. This fails if and only if y^2-b is not a cubic residue.
                 */
                return mapToSubgroup(y, sel);
            } catch (IllegalArgumentException e) {

            }
        } while (true);
    }

    /**
     * Maps a given y coordinate to a point in this subgroup.
     * <p>
     * As opposed to {@link #mapToPoint(FieldElement, int)}, this function includes cofactor multiplication.
     *
     * @param y   y coordinate of point
     * @param sel selection of x coordinate
     */
    public PairingSourceGroupElement mapToSubgroup(FieldElement y, int sel) {
        /* this is required to be sure to map to the unique subgroup of size size() */
        if (!this.size().gcd(this.getCofactor()).equals(BigInteger.ONE)) {
            throw new IllegalArgumentException("Require cofactor coprime to order of subgroup.");
        }

        return cofactorMultiplication(this.decompressX(y, sel), y);
    }

    /**
     * Decompresses a point by mapping y coordinate of point (x,y) back to curve.
     * <p>
     * This function takes the y-coordinate of a point and maps it to a point on this curve.
     * It solves the Weierstrass equation for a matching x-coordinate.
     * For a given field F, for each element y in F, there exist either 0 or 3 solutions over F,
     * i.e. x-coordinates in F.
     * <ul>
     * <li>If y is not a y-coordinate of a point on this curve (no matching x exists over the field of definition),
     * an {@code IllegalArgumentException} is thrown.
     * <li>If 3 solutions exists, the returned solution is selected with the parameter {@code sel} mod 3.
     * </ul>
     * It is important to note that this function does not map the result to the subgroup represented by this class.
     * A cofactor multiplication is still required.
     * We do not include cofactor multiplication into this function to assert the following
     * contract:
     * <pre>
     *     mapToPoint(P.getY(),0).equals(P);
     * </pre>
     *
     * @param y y-coordinate of point
     * @param sel selector for x-coordinate
     * @return point (x,y) on curve
     * @throws IllegalArgumentException if the y-coordinate does not correspond to any curve point
     */
    public PairingSourceGroupElement mapToPoint(FieldElement y, int sel) {
        return this.getElement(decompressX(y, sel), y);
    }

    /**
     * Retrieves the x-coordinate from the compressed point and its corresponding y-coordinate.
     * <p>
     * For more information on how this is done, consult the documentation for {@link #mapToPoint(FieldElement, int)}.
     *
     * @param y   y-coordinate of point
     * @param sel selector for x-coordinate
     * @return x-coordinate of the decompressed point
     * @throws IllegalArgumentException if the y-coordinate does not correspond to any curve point
     */
    public FieldElement decompressX(FieldElement y, int sel) {
        /*
         *
         * For background see Lemma2.26 and Lemma 2.27 of Naehrigs PHD Thesis.
         */
        FieldElement x, tmp;

        /* tmp = y^2 */
        tmp = y.pow(BigInteger.valueOf(2));

        /* tmp = y^2-b */
        tmp = tmp.sub(getA6());

        // TODO: move cube root computation to Field or FieldElement itself
        BigInteger e;
        switch (getFieldOfDefinition().size().mod(BigInteger.valueOf(9)).intValue()) {
            case 4:
                e = getFieldOfDefinition().size().multiply(BigInteger.valueOf(2)).add(BigInteger.ONE);
                break;
            case 7:
                e = getFieldOfDefinition().size().add(BigInteger.valueOf(2));
                break;
            default:
                throw new UnsupportedOperationException("The function mapToPoint is only implemented for fields " +
                        "with order mod 9 in {4,7}");
        }

        e = e.divide(BigInteger.valueOf(9));

        /* compute third root */
        x = tmp.pow(e);

        if (!x.pow(BigInteger.valueOf(3)).equals(tmp)) {
            throw new IllegalArgumentException("Argument " + y + "is not a cube in " + x.getStructure());
        }

        /* select one out of three possible third roots */
        x = x.mul(((ExtensionField) getFieldOfDefinition()).getCubeRoot().pow(BigInteger.valueOf(sel)));

        return x;
    }

    @Override
    public BarretoNaehrigSourceGroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        return (BarretoNaehrigSourceGroupElementImpl) this.getUniformlyRandomElementOblivious();
    }

    @Override
    public String toString() {
        return "Subgroup of F-rational points on E:x^3+b with b=" + this.getA6().toString();
    }

    @Override
    public boolean isShortForm() {
        return true;
    }

    @Override
    public boolean hasPrimeSize() throws UnsupportedOperationException {
        return true;
    }

    // workaround since `super.super.method()` does not work...
    protected PairingSourceGroupElement superGetUniformlyRandomElement() {
        return super.getUniformlyRandomElement();
    }
}
