package de.upb.crypto.math.structures.ec;


import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;

// a point on a short form weierstrass curve, in affince coordinates
public class MyAffineEllipticCurvePoint extends AbstractEllipticCurvePoint {
    
    private MyShortFormWeierstrassCurve curve;
    
    public MyAffineEllipticCurvePoint(MyShortFormWeierstrassCurve curve,
                                      FieldElement x, FieldElement y) {
        super(curve, x, y,
                curve.getFieldOfDefinition().getOneElement());
        this.curve = curve;
    }
    
    @Override
    public MyAffineEllipticCurvePoint normalize() {
        return this;
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
        return new MyAffineEllipticCurvePoint(curve, rx, ry);
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
        return new MyAffineEllipticCurvePoint(curve, rx, ry);
    }
    
    @Override
    public GroupElement inv() {
        if (this.isNeutralElement())
            return this;
        
        FieldElement newY = this.getY().neg();
        return this.getStructure().getElement(this.getX(), newY);
    }
    
    
    @Override
    public MyAffineEllipticCurvePoint add(EllipticCurvePoint P, FieldElement[] line) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public FieldElement[] computeLine(EllipticCurvePoint Q) {
        throw new UnsupportedOperationException();
    }
    
    public String toString() {
        return "(" + x.toString() + "," + y.toString() + ")";
    }
    
    @Override
    public boolean equals(Object element) {
        if (element == this)
            return true;
        
        if (!(element instanceof MyAffineEllipticCurvePoint))
            return false;
        
        MyAffineEllipticCurvePoint p = (MyAffineEllipticCurvePoint) element;
        if (this.isNeutralElement() && p.isNeutralElement())
            return true;
        
        if (this.isNeutralElement() || p.isNeutralElement())
            return false;
        
        if (!this.getX().equals(p.getX()))
            return false;
    
        return this.getY().equals(p.getY());
    }
    
    @Override
    public boolean isNormalized() {
        return true;
    }
    
    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        throw new UnsupportedOperationException();
    }
}

