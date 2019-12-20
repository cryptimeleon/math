package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;

import java.math.BigInteger;

public class ProjectiveECPCoordinate extends AbstractECPCoordinate {

    public ProjectiveECPCoordinate(WeierstrassCurve curve, FieldElement x, FieldElement y,
                                   FieldElement z) {
        super(curve, x, y, z);
    }

    public ProjectiveECPCoordinate(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public ProjectiveECPCoordinate(WeierstrassCurve curve) {
        super(curve);
    }

    @Override
    public AbstractECPCoordinate normalize() {
        if (this.isNormalized())
            return this;

        FieldElement inv_z = this.z.inv();
        return new ProjectiveECPCoordinate(
                this.structure,
                this.x.mul(inv_z),
                this.y.mul(inv_z)
        );
    }

    @Override
    public boolean isNormalized() {
        return z.equals(this.structure.getFieldOfDefinition().getOneElement());
    }


    public FieldElement[] computeLine(EllipticCurvePoint Q) {
        // TODO: How to do this?
        return new FieldElement[0];
    }


    public AbstractECPCoordinate add(EllipticCurvePoint P, FieldElement[] line) {
        ProjectiveECPCoordinate Q = (ProjectiveECPCoordinate) P;

        // TODO: Can line help here?

        return null;
    }

    public EllipticCurvePoint add(EllipticCurvePoint P) {
        ProjectiveECPCoordinate Q = (ProjectiveECPCoordinate) P;
        // If points are same, double instead (more efficient)
        if (this.representsSamePoint(Q)) {
            return this.ec_double();
        }
        // If either point is the neutral element at infinity, just return other one
        if (this.z.isZero())
            return Q;
        if (Q.z.isZero())
            return this;

        // TODO: Mixed operations, more efficient if one point is normalized already
        // Let this = (X_1, Y_1, Z_2) and Q = (X_1, Y_2, Z_2). Compute P + Q = (X_3, Y_3, Z_3).
        // Do addition without division by storing division in Z coordinate.
        // Takes 12 multiplications and 2 squarings.
        // Description of algorithm in Section 3.1.2 of Swante Scholz' master thesis.

        // W_X = X_1 * Z_2
        FieldElement W_X = this.x.mul(Q.z);
        // W_Y = Y_1 * Z_2
        FieldElement W_Y = this.y.mul(Q.z);
        // W_Z = Z_1 * Z_2
        FieldElement W_Z = this.z.mul(Q.z);
        // u = Y_2 * Z_1 - W_Y
        FieldElement u = Q.z.mul(this.z).sub(W_Y);
        // v = X_2 * Z_1 - W_X
        FieldElement v = Q.x.mul(this.z).sub(W_X);
        if (v.isZero()) {
            if (!u.isZero()) {
                // If v == 0 and u != 0, i.e. same x-coordinates but different y-coordinates,
                // we have a vertical line and can return neutral element
                return new ProjectiveECPCoordinate(this.structure);
            } else {
                // If v == 0 and u == 0, i.e. the points are the same, we need to do a doubling
                // and not an addition. This should not happen though as we already
                // test for equality beforehand.
                return this.ec_double();
            }
        }

        // u_2 = u^2
        FieldElement u_2 = u.square();
        // v_2 = v^2
        FieldElement v_2 = v.square();
        // v_3 = v_2 * v
        FieldElement v_3 = v_2.mul(v);
        // R = v_2 * W_X
        FieldElement R = v_2.mul(W_X);
        // A = u_2 * W_Z - v_3 - 2R
        FieldElement A = u_2.mul(W_Z).sub(v_3).sub(R.add(R));
        // X_3 = v * A
        FieldElement X_3 = v.mul(A);
        // Y_3 = u (R - A) - v_3 * W_Y
        FieldElement Y_3 = u.mul(R.sub(A)).sub(v_3.mul(W_Y));
        // Z_3 = v_3 * W_Z
        FieldElement Z_3 = v_3.mul(W_Z);

        return new ProjectiveECPCoordinate(this.structure, X_3, Y_3, Z_3);
    }

    @Override
    public GroupElement inv() {
        if (this.isNeutralElement())
            return this;

        FieldElement new_y = y;

        if (getStructure().isShortForm()) {
            new_y = new_y.neg();
        } else {
            // TODO: What to do here?
        }

        return new ProjectiveECPCoordinate(this.structure, x, new_y, z);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ProjectiveECPCoordinate))
            return false;
        ProjectiveECPCoordinate P = (ProjectiveECPCoordinate) other;
        if (this.isNeutralElement() && P.isNeutralElement())
            return true;
        else return x.equals(P.x) && y.equals(P.y) && z.equals(P.z);
    }

    // Cannot call method just double because its reserved
    private EllipticCurvePoint ec_double() {
        // If this point is neutral point or is self-inverse, just return neutral point
        if (this.z.isZero() || this.y.isZero()) {
            return new ProjectiveECPCoordinate(this.structure);
        }

        // Double the point this = (X_1, Y_1, Z_1). Takes 5 multiplications and 6 squarings.
        // Description of algorithm in section 3.1.4 of Swante Scholz' master thesis.

        // X_11 = X_1^2
        FieldElement X_11 = this.x.square();
        // Z_11 = Z_1^2
        FieldElement Z_11 = this.z.square();
        // w = a * Z_11 + 3 * X_11
        FieldElement three = this.structure.getFieldOfDefinition().getElement(BigInteger.valueOf(3));
        FieldElement w = this.structure.getA4().mul(Z_11).add(X_11.mul(three));
        // s = 2 * Y_1 * Z_1
        FieldElement s = this.y.add(this.y).mul(this.z);
        // s_2 = s^2
        FieldElement s_2 = s.square();
        // R = Y_1 * s
        FieldElement R = this.y.mul(s);
        // R_2 = R^2
        FieldElement R_2 = R.square();
        // B = (X_1 + R)^2 - X_11 - R_2
        FieldElement B = this.x.add(R).square().sub(X_11).sub(R_2);
        // h = w^2 - 2 * B
        FieldElement h = w.square().sub(B.add(B));
        // X_3 = h * s
        FieldElement X_3 = h.mul(s);
        // Y_3 = w * (B - h) - 2 * R_2
        FieldElement Y_3 = w.mul(B.sub(h)).sub(R_2.add(R_2));
        // Z_3 = s * s_2
        FieldElement Z_3 = s.mul(s_2);

        return new ProjectiveECPCoordinate(this.structure, X_3, Y_3, Z_3);
    }
}
