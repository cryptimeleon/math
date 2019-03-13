package de.upb.crypto.math.structures.ec;


import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;

// a point on a short form weierstrass curve, in affince coordinates
public class MyAffineEllipticCurvePoint extends MyAbstractEllipticCurvePoint {
    
    public MyAffineEllipticCurvePoint(MyShortFormWeierstrassCurve curve,
                                      Zp.ZpElement x, Zp.ZpElement y) {
        super(curve, x, y, curve.field.getOneElement());
    }
    
    // returns point at infinity (neutral point)
    public MyAffineEllipticCurvePoint(MyShortFormWeierstrassCurve curve) {
        super(curve, curve.field.getZeroElement(),
                curve.field.getOneElement(), curve.field.getZeroElement());
    }
    
    @Override
    public MyAffineEllipticCurvePoint createNewPoint(FieldElement x, FieldElement y) {
        return new MyAffineEllipticCurvePoint(curve, (Zp.ZpElement)x, (Zp.ZpElement)y);
    }
    
    @Override
    public MyAffineEllipticCurvePoint normalize() {
        return this;
    }
    
    
    @Override
    public MyAffineEllipticCurvePoint op(Element e) throws IllegalArgumentException {
        MyAffineEllipticCurvePoint Q = (MyAffineEllipticCurvePoint) e;
        if (Q.isNeutralElement()) {
            return this;
        }
        if (this.isNeutralElement()) {
            return Q;
        }
        if (x.equals(Q.x)) {
            if (y.equals(Q.y)) {
                return this.times2();
            }
            return getPointAtInfinity();
        }
        FieldElement s = y.sub(Q.y).div(x.sub(Q.x));
        FieldElement rx = s.square().sub(x).sub(Q.x);
        FieldElement ry = s.mul(x.sub(rx)).sub(y);
        return createNewPoint(rx, ry);
    }
    
    // returns this+this
    private MyAffineEllipticCurvePoint times2() {
        if (this.isNeutralElement() || y.isZero()) {
            return getPointAtInfinity();
        }
        FieldElement s = x.square().mul(curve.three).add(curve.a).div(y.mul(curve.two));
        FieldElement rx = s.square().sub(x.mul(curve.two));
        FieldElement ry = s.mul(x.sub(rx)).sub(y);
        return createNewPoint(rx, ry);
    }
    
    @Override
    public MyAffineEllipticCurvePoint getPointAtInfinity() {
        return new MyAffineEllipticCurvePoint(curve);
    }
    
    @Override
    public MyAffineEllipticCurvePoint invert() {
        if (this.isNeutralElement())
            return this;
        
        Zp.ZpElement newY = y.neg();
        return new MyAffineEllipticCurvePoint(curve, x, newY);
    }
    
    
    @Override
    public MyAffineEllipticCurvePoint add(MyAbstractEllipticCurvePoint Q) {
        return this.op(Q);
    }
    

    
    @Override
    public String toString() {
        return "(" + x.toString() + "," + y.toString() + ")";
    }
    
    @Override
    public boolean isNormalized() {
        return true;
    }
    
}

