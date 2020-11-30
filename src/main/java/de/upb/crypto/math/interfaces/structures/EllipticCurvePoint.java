package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;

/**
 * A point on an elliptic curve.
 */
public interface EllipticCurvePoint extends GroupElementImpl {

    /**
     * Normalizes this elliptic curve point.
     * @return the normalized point
     */
    public EllipticCurvePoint normalize();

    /**
     * Checks whether this point is normalized.
     * @return true if this point is normalized, else false
     */
    boolean isNormalized();

    /**
     * Returns the base field over which the elliptic curve this point belongs to is defined.
     */
    public Field getFieldOfDefinition();


    /**
     * Computes a parameterization of the line through this and {@code Q}.
     * <p>
     * The result of this function should parameterize the line through this and Q.
     * For example for affine points, a line through P is parameterized by \(a_0, a_1\) with
     * \(a_0(y-yP)-a_1(x-xP)\).
     * For Jacobian points, a line through P is parameterized by
     * \(a_0, a_1\) with \(a_0(yZp^3-Yp)-a_1(xZp^2-Xp)\).
     * <p>
     * This function is useful to assess the line as an intermediate result of point addition
     * for a an efficient pairing computation.
     *
     * @param Q the second point on the line
     * @return parameterization of line through this and {@code Q}
     */
    public FieldElement[] computeLine(EllipticCurvePoint Q);

    /**
     * Add this to P with the help of line where line is the result of {@code this.computeLine(P)}.
     * <p>
     * The contract is that {@code this.op(P).equals(this.add(P,this.computeLine(P)))}.
     *
     * @param P the point to add
     * @param line the line to use for the addition
     * @return the resulting curve point
     */
    public EllipticCurvePoint add(EllipticCurvePoint P, FieldElement[] line);


    @Override
    public default GroupElementImpl op(GroupElementImpl e) throws IllegalArgumentException {
        EllipticCurvePoint P = (EllipticCurvePoint) e;

        return this.add(P, this.computeLine(P));
    }
}
