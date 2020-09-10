package de.upb.crypto.math.structures.ec;


import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;

public class AffineEllipticCurvePoint extends AbstractEllipticCurvePoint {


//	public AffineEllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y,FieldElement z) {
//		super(curve,
//				x,
//				y,
//				z
//				);
//	}

    public AffineEllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y) {
        super(curve,
                x,
                y,
                curve.getFieldOfDefinition().getOneElement()
        );
    }

    public AffineEllipticCurvePoint(WeierstrassCurve curve) {
        super(curve,
                curve.getFieldOfDefinition().getZeroElement(),
                curve.getFieldOfDefinition().getOneElement(),
                curve.getFieldOfDefinition().getZeroElement()
        );
    }

    @Override
    public AffineEllipticCurvePoint normalize() {
        return this;
    }


    @Override
    public GroupElementImpl inv() {
        if (this.isNeutralElement())
            return this;

        FieldElement y = this.getY();

        if (getStructure().isShortForm()) { //general formula collapses
            y = y.neg();
        } else { /* use general formula: -(x,y) = (x, -y - a_1*x - a3) */
            y = y
                    .add(this.getStructure().getA1().mul(this.getX()))
                    .add(this.getStructure().getA3())
                    .neg();
        }

        return this.getStructure().getElement(this.getX(), y);
    }

    private FieldElement calculateLambda(EllipticCurvePoint Q) {
        AffineEllipticCurvePoint P = (AffineEllipticCurvePoint) Q;
        FieldElement enumerator, denominator;
        if (this.getX().equals(P.getX())) {
            FieldElement x = this.getX();
            FieldElement y = this.getY();
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
            enumerator = P.getY().sub(this.getY());
            denominator = P.getX().sub(this.getX());
        }
        return enumerator.div(denominator);
    }

    private FieldElement calculateNu(AffineEllipticCurvePoint P) {
        FieldElement enumerator, denominator;
        FieldElement x = this.getX();
        FieldElement y = this.getY();
        if (this.getX().equals(P.getX())) {
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
            enumerator = y.mul(P.getX()).sub(P.getY().mul(x));
            denominator = P.getX().sub(x);
        }
        return enumerator.div(denominator);
    }


    @Override
    public AffineEllipticCurvePoint add(EllipticCurvePoint P, FieldElement[] line) {
        // Compute P (this) + Q (element)

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
                .sub(this.getStructure().getA2()).sub(this.getX()).sub(Q.getX());


        FieldElement y;
        if (this.getStructure().isShortForm()) {
            // y = L(x_1 - x ) - y_1
            y = this.getX().sub(x).mul(lambda).sub(this.getY());
        } else {
            // y = - (L + a1)*x - v - a3
            y = lambda.add(this.getStructure().getA1()).neg().mul(x)
                    .sub(nu).sub(this.getStructure().getA3());
        }

        return (AffineEllipticCurvePoint) this.getStructure().getElement(x, y);
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

    public String toString() {
        return isNeutralElement() ? "point at infinity" : "(" + x.toString() + "," + y.toString() + ")";
    }

    @Override
    public boolean equals(Object element) {
        if (element == this)
            return true;

        if (!(element instanceof AffineEllipticCurvePoint))
            return false;

        AffineEllipticCurvePoint p = (AffineEllipticCurvePoint) element;
        if (this.isNeutralElement() && p.isNeutralElement())
            return true;

        if (this.isNeutralElement() || p.isNeutralElement())
            return false;

        if (!this.getX().equals(p.getX()))
            return false;

        if (!this.getY().equals(p.getY()))
            return false;


        return true;
    }

    @Override
    public boolean isNormalized() {
        return true;
    }

//	@Override
//	public PairableReturnStruct addAndEvaluate(EllipticCurvePoint P, EllipticCurvePoint Q) {
//		AffineEllipticCurvePoint castedP = (AffineEllipticCurvePoint) P;
//
//		
//		AffineEllipticCurvePoint castedQ;
//		FieldElement vertical;
//		FieldElement line;
//		Pairable sum;
//		
//		
//		vertical = this.getFieldOfDefinition().getOneElement();
//		line = vertical;
//		
//		boolean computeLines = (null != Q);
//		
//		if (computeLines) {
//			castedQ = (AffineEllipticCurvePoint) Q;
//		} else {
//			castedQ = (AffineEllipticCurvePoint) this.getStructure().getNeutralElement();
//		}
//
//		
//
//	
//		// P = 0
//		if (P.isNeutralElement()) {
//			sum = this;
//			return new PairableReturnStruct(sum,line,vertical);
//		}
//
//		// this = 0
//		if (this.isNeutralElement()) {
//			sum =  (AffineEllipticCurvePoint) P;
//			return new PairableReturnStruct(sum,line,vertical);
//		}
//
//		// P = -Q
//		if (this.equals(castedP.inv())) {
//			sum = (AffineEllipticCurvePoint) this.getStructure().getNeutralElement();
//			if (computeLines) {
//				line = castedP.getX().sub(castedQ.getX());
//			}
//			return new PairableReturnStruct(sum,line,vertical);
//		}
//		// general case for P + Q with P != -Q and both != infinity
//
//
//		
//		/* lambda equals the slope of the line throuhg this and element */
//		FieldElement nu;
//		if (this.getStructure().isShortForm()) {
//			nu = this.getStructure().getFieldOfDefinition().getZeroElement();
//		} else {
//			nu = this.calculateNu(castedP);
//		}
//		
//		FieldElement lambda = this.calculateLambda(castedP);
//		
//		sum = (Pairable) this.add(castedP, lambda, nu);
//		
//
//		if (computeLines) {
//			/*
//			 * line through this and P at Q is y_Q-y_P - lambda_this,P(x_Q-x_P)
//			 */
//			line = castedQ.getY().sub(castedP.getY()).sub(
//					castedQ.getX().sub(castedP.getX()).mul(lambda));
//			
//			
//			/*
//			 * vertical throuth this+P at Q is xQ-x_this+P
//			 */
//			vertical = castedQ.getX().sub(((AffineEllipticCurvePoint) sum).getX()); 
//		}
//		
//		return new PairableReturnStruct(sum,line,vertical);
//
//
//	}

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        AffineEllipticCurvePoint normalized = normalize();
        if (!getStructure().getFieldOfDefinition().getUniqueByteLength().isPresent()) {
            accumulator.escapeAndSeparate(normalized.getX());
            accumulator.escapeAndSeparate(normalized.getY());
            accumulator.escapeAndSeparate(normalized.getZ());
        } else {
            accumulator.append(normalized.getX());
            accumulator.append(normalized.getY());
            accumulator.append(normalized.getZ());
        }
        return accumulator;
    }
}
