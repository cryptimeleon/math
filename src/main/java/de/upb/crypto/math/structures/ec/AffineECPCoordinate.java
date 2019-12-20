package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;


public class AffineECPCoordinate extends AbstractECPCoordinate {

    public AffineECPCoordinate(WeierstrassCurve curve, FieldElement x, FieldElement y,
                               FieldElement z) {
        super(curve, x, y, z);
    }

    public AffineECPCoordinate(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public AffineECPCoordinate(WeierstrassCurve curve) {
        super(curve);
    }

    @Override
    public AbstractECPCoordinate normalize() {
        return this;
    }

    @Override
    public boolean isNormalized() {
        return true;
    }

    @Override
    public FieldElement[] computeLine(AbstractECPCoordinate Q) {
        AffineECPCoordinate P = (AffineECPCoordinate) Q;

        if (this.equals(P.inv()) || this.isNeutralElement() || P.isNeutralElement()) {
            //line is given as 0*(y-y_P)+1*(x-x_P)
            return new FieldElement[]{this.getFieldOfDefinition().getZeroElement(),
                    this.getFieldOfDefinition().getOneElement()};
        } else {
            //line is given as 1*(y-y_P)-lambda*(x-x_P)
            return new FieldElement[]{this.getFieldOfDefinition().getOneElement(),
                    this.calculateLambda(P)};
        }
    }

    @Override
    public AbstractECPCoordinate add(AbstractECPCoordinate Q) {
        return this.add(Q, computeLine(Q));
    }

    @Override
    public AffineECPCoordinate add(AbstractECPCoordinate Q, FieldElement[] line) {
        AffineECPCoordinate P = (AffineECPCoordinate) Q;

        if (P.isNeutralElement()) {
            return this;
        }

        if (this.isNeutralElement()) {
            return P;
        }

        /*vertical line*/
        if (line[0].isZero()) {
            return new AffineECPCoordinate(this.getStructure());
        }


        FieldElement nu;

        if (this.getStructure().isShortForm()) {
            nu = this.getStructure().getFieldOfDefinition().getZeroElement();
        } else {
            nu = this.calculateNu(P);
        }

        FieldElement lambda = line[1];

        // x = L^2 + a1*L - a2 - xP - xQ
        FieldElement x = lambda.mul(lambda)
                .add(lambda.mul(this.getStructure().getA1()))
                .sub(this.getStructure().getA2()).sub(this.x).sub(P.x);


        FieldElement y;
        if (this.getStructure().isShortForm()) {
            // y = L(x_1 - x ) - y_1
            y = this.x.sub(x).mul(lambda).sub(this.y);
        } else {
            // y = - (L + a1)*x - v - a3
            y = lambda.add(this.getStructure().getA1()).neg().mul(x)
                    .sub(nu).sub(this.getStructure().getA3());
        }

        return new AffineECPCoordinate(this.getStructure(), x, y);
    }

    @Override
    public AbstractECPCoordinate inv() {
        if (this.isNeutralElement())
            return this;

        FieldElement y = this.y;

        if (getStructure().isShortForm()) { //general formula collapses
            y = y.neg();
        } else { /* use general formula: -(x,y) = (x, -y - a_1*x - a3) */
            y = y
                    .add(this.getStructure().getA1().mul(this.x))
                    .add(this.getStructure().getA3())
                    .neg();
        }

        return new AffineECPCoordinate(this.getStructure(), this.x, y);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AffineECPCoordinate))
            return false;
        AffineECPCoordinate P = (AffineECPCoordinate) other;
        if (this.isNeutralElement() && P.isNeutralElement())
            return true;
        else return x.equals(P.x) && y.equals(P.y) && z.equals(P.z);
    }

    private FieldElement calculateLambda(AffineECPCoordinate Q) {
        FieldElement enumerator, denominator;
        if (this.x.equals(Q.x)) {
            FieldElement x = this.x;
            FieldElement y = this.y;
            // Calculate numerator of lambda
            // L = 3x^2
            enumerator = x.mul(x);
            enumerator = enumerator.add(enumerator).add(enumerator);

            // L + 2*a2*x
            FieldElement tmp = this.getStructure().getA2().mul(x);
            tmp = tmp.add(tmp);
            enumerator = enumerator.add(tmp);

            // L + a4 - a1*y
            enumerator = enumerator.add(this.getStructure().getA4())
                    .sub(y.mul(this.getStructure().getA1()));

            // calculate denominator of enumerator
            // = 2y
            denominator = y.add(y);

            // + a1*x
            denominator = denominator.add(x.mul(this.getStructure().getA1()));
            // + a3
            denominator = denominator.add(this.getStructure().getA3());
        } else {
            enumerator = Q.y.sub(this.y);
            denominator = Q.x.sub(this.x);
        }
        return enumerator.div(denominator);
    }

    private FieldElement calculateNu(AffineECPCoordinate Q) {
        FieldElement enumerator, denominator;
        FieldElement x = this.x;
        FieldElement y = this.y;
        if (this.x.equals(Q.x)) {
            // calculate numerator of v
            // - x^3
            enumerator = x.mul(x).mul(x).neg();

            // + a4*x
            enumerator = enumerator.add(x.mul(this.getStructure().getA4()));

            // + 2*a6
            enumerator = enumerator.add(this.getStructure().getA6())
                    .add(this.getStructure().getA6());

            // - a3*y
            enumerator = enumerator.sub(y.mul(this.getStructure().getA3()));

            // calculate denominator of v

            // + 2y
            denominator = y.add(y);

            // + a1*x
            denominator = denominator.add(x.mul(this.getStructure().getA1()));

            // + a3
            denominator = denominator.add(this.getStructure().getA3());
        } else {
            enumerator = y.mul(Q.x).sub(Q.y.mul(x));
            denominator = Q.x.sub(x);
        }
        return enumerator.div(denominator);
    }
}
