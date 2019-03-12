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
    
    @Override
    public MyAffineEllipticCurvePoint createNewPoint(FieldElement x, FieldElement y) {
        return new MyAffineEllipticCurvePoint(curve, (Zp.ZpElement)x, (Zp.ZpElement)y);
    }
    
    @Override
    public MyAffineEllipticCurvePoint normalize() {
        return this;
    }
    
    
    @Override
    public Group getStructure() {
        return null;
    }
    
    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
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
            return curve.getNeutralElement();
        }
        FieldElement s = y.sub(Q.y).div(x.sub(Q.x));
        FieldElement rx = s.square().sub(x).sub(Q.x);
        FieldElement ry = s.mul(x.sub(rx)).sub(y);
        return createNewPoint(rx, ry);
    }
    
    // returns this+this
    private GroupElement times2() {
        if (this.isNeutralElement() || y.isZero()) {
            return curve.getNeutralElement();
        }
        FieldElement s = x.mul(x);
        FieldElement tmp = y.add(y);
        s = s.add(s).add(s).add(curve.a).div(tmp);
        FieldElement rx = s.square().sub(x.add(x));
        FieldElement ry = s.mul(x.sub(rx)).sub(y);
        return createNewPoint(rx, ry);
    }
    
    @Override
    public MyAbstractEllipticCurvePoint getPointAtInfinity() {
        return null;
    }
    
    @Override
    public MyAffineEllipticCurvePoint invert() {
        if (this.isNeutralElement())
            return this;
        
        Zp.ZpElement newY = y.neg();
        return new MyAffineEllipticCurvePoint(curve, x, newY);
    }
    
    
    @Override
    public MyAbstractEllipticCurvePoint add(MyAbstractEllipticCurvePoint Q) {
        throw new UnsupportedOperationException();
    }
    

    
    @Override
    public String toString() {
        return "(" + x.toString() + "," + y.toString() + ")";
    }
    
    @Override
    public boolean isNormalized() {
        return true;
    }
    
    @Override
    public Field getFieldOfDefinition() {
        return null;
    }
    
    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Representation getRepresentation() {
        return null;
    }
}

