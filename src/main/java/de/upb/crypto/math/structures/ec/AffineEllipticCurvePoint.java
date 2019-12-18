package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;


public class AffineEllipticCurvePoint extends EllipticCurvePoint {

    public AffineEllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y,
                                    FieldElement z) {
        super(curve, x, y, z);
    }

    public AffineEllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public AffineEllipticCurvePoint(WeierstrassCurve curve) {
        super(curve);
    }

    @Override
    public EllipticCurvePoint normalize() {
        return this;
    }

    @Override
    public boolean isNormalized() {
        return true;
    }

    @Override
    public FieldElement[] computeLine(EllipticCurvePoint Q) {
        AffineEllipticCurvePoint P = (AffineEllipticCurvePoint) Q;

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
    public GroupElement op(Element e) {
        EllipticCurvePoint P = (EllipticCurvePoint) e;

        return this.add(P, this.computeLine(P));
    }

    @Override
    public AffineEllipticCurvePoint add(EllipticCurvePoint P, FieldElement[] line) {
        AffineEllipticCurvePoint Q = (AffineEllipticCurvePoint) P;

        if (Q.isNeutralElement()) {
            return this;
        }

        if (this.isNeutralElement()) {
            return Q;
        }

        /*vertical line*/
        if (line[0].isZero()) {
            return (AffineEllipticCurvePoint) this.getStructure().getNeutralElement();
        }


        FieldElement nu;

        if (this.getStructure().isShortForm()) {
            nu = this.getStructure().getFieldOfDefinition().getZeroElement();
        } else {
            nu = this.calculateNu(Q);
        }

        FieldElement lambda = line[1];

        // x = L^2 + a1*L - a2 - xP - xQ
        FieldElement x = lambda.mul(lambda)
                .add(lambda.mul(this.getStructure().getA1()))
                .sub(this.getStructure().getA2()).sub(this.x).sub(Q.x);


        FieldElement y;
        if (this.getStructure().isShortForm()) {
            // y = L(x_1 - x ) - y_1
            y = this.x.sub(x).mul(lambda).sub(this.y);
        } else {
            // y = - (L + a1)*x - v - a3
            y = lambda.add(this.getStructure().getA1()).neg().mul(x)
                    .sub(nu).sub(this.getStructure().getA3());
        }

        return (AffineEllipticCurvePoint) this.getStructure().getElement(x, y);
    }

    @Override
    public GroupElement inv() {
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

        return this.getStructure().getElement(this.x, y);
    }

    private FieldElement calculateLambda(AffineEllipticCurvePoint P) {
        FieldElement enumerator, denominator;
        if (this.x.equals(P.x)) {
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
            enumerator = P.y.sub(this.y);
            denominator = P.x.sub(this.x);
        }
        return enumerator.div(denominator);
    }

    private FieldElement calculateNu(AffineEllipticCurvePoint P) {
        FieldElement enumerator, denominator;
        FieldElement x = this.x;
        FieldElement y = this.y;
        if (this.x.equals(P.x)) {
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
            enumerator = y.mul(P.x).sub(P.y.mul(x));
            denominator = P.x.sub(x);
        }
        return enumerator.div(denominator);
    }
}
