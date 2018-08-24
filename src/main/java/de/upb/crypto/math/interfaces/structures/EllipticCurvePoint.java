package de.upb.crypto.math.interfaces.structures;

public interface EllipticCurvePoint extends GroupElement {
    public EllipticCurvePoint normalize();

    boolean isNormalized();

    public Field getFieldOfDefinition();


    /**
     * Computes a parameterization of the line through this and Q.
     * <p>
     * The result of this function should parameterize the line through this and Q.
     * For example for affine points, a line through P is parameterized by a_0,a_1 with
     * a_0(y-yP)-a_1(x-xP). For Jacobian points, a line through P is parameterized by
     * a_0,a_1 with a_0(yZp^3-Yp)-a_1(xZp^2-Xp).
     * <p>
     * This function is useful to assess the line as an intermediate result of point addition
     * for a an efficient pairing computation.
     *
     * @param Q - second point on the line
     * @return parameterization of line through this and Q
     */
    public FieldElement[] computeLine(EllipticCurvePoint Q);

    /**
     * Add this to P with the help of line where line is the resul of this.computeLine(P).
     * <p>
     * The contract is that this.op(P)=this.add(P,this.computeLine(P)).
     *
     * @param P
     * @param line
     * @return
     */
    public EllipticCurvePoint add(EllipticCurvePoint P, FieldElement[] line);


    @Override
    public default GroupElement op(Element e) throws IllegalArgumentException {
        EllipticCurvePoint P = (EllipticCurvePoint) e;

        return this.add(P, this.computeLine(P));
    }

}
