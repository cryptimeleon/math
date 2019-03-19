package de.upb.crypto.math.structures.ec;


import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;

// a point on a short form weierstrass curve, in projective coordinates
// representing the affine point (x/z, y/z)
public class MyProjectiveEllipticCurvePoint extends AbstractEllipticCurvePoint {
    
    public MyProjectiveEllipticCurvePoint(WeierstrassCurve curve,
                                          FieldElement x, FieldElement y) {
        super(curve, x, y, curve.getFieldOfDefinition().getOneElement());
    }
    
    public MyProjectiveEllipticCurvePoint(WeierstrassCurve curve,
                                          FieldElement x, FieldElement y, FieldElement z) {
        super(curve, x, y, z);
    }
    
    // returns point at infinity (neutral point)
    public MyProjectiveEllipticCurvePoint(WeierstrassCurve curve) {
        super(curve, curve.getFieldOfDefinition().getZeroElement(),
                curve.getFieldOfDefinition().getOneElement(), curve.getFieldOfDefinition().getZeroElement());
    }
    
    public AffineEllipticCurvePoint toAffinePoint() {
        if (isNeutralElement()) {
            return new AffineEllipticCurvePoint(structure); // neutral element
        }
        FieldElement div = z.inv();
        return new AffineEllipticCurvePoint(structure, x.mul(div), y.mul(div));
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint normalize() {
        if (isNeutralElement()) {
            return this;
        }
        FieldElement div = z.inv();
        return new MyProjectiveEllipticCurvePoint(structure, x.mul(div), y.mul(div), structure.getFieldOfDefinition().getOneElement());
    }
    
    
    @Override
    public AbstractEllipticCurvePoint add(AbstractEllipticCurvePoint q) throws IllegalArgumentException {
//        if (Q == this) {
//            return this.times2();
//        }
        if (q.isNeutralElement()) {
            return this;
        }
        if (this.isNeutralElement()) {
            return q;
        }
        FieldElement x1z2 = x.mul(q.z);
        FieldElement v = q.x.mul(z).sub(x1z2);
        FieldElement y1z2 = y.mul(q.z);
        FieldElement u = q.y.mul(z).sub(y1z2);
        if (v.isZero()) {
            if (u.isZero()) {
                return this.times2();
            }
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement uu = u.square();
        FieldElement vv = v.square();
        FieldElement vvv = v.mul(vv);
        FieldElement r = vv.mul(x1z2);
        FieldElement z1z2 = z.mul(q.z);
        FieldElement two = structure.getFieldOfDefinition().getElement(2);
        FieldElement a = uu.mul(z1z2).sub(vvv).sub(r.mul(two));
        FieldElement rx = v.mul(a);
        FieldElement ry = u.mul(r.sub(a)).sub(vvv.mul(y1z2));
        FieldElement rz = vvv.mul(z1z2);
        return new MyProjectiveEllipticCurvePoint(structure, rx, ry, rz); // todo: return actual type of this instance somehow
    }
    
    // returns this+this
    public AbstractEllipticCurvePoint times2() {
        if (this.isNeutralElement() || y.isZero()) {
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement two = structure.getFieldOfDefinition().getElement(2);
        FieldElement xx = x.square();
        FieldElement zz = z.square();
        FieldElement w = structure.getA4().mul(zz).add(xx.mul(structure.getFieldOfDefinition().getElement(3)));
        FieldElement s = y.mul(z).mul(two);
        FieldElement ss = s.square();
        FieldElement r = y.mul(s);
        FieldElement rr = r.square();
        FieldElement B = (x.add(r)).square().sub(xx).sub(rr);
        FieldElement h = w.square().sub(B.mul(two));
        FieldElement rx = h.mul(s);
        FieldElement ry = w.mul(B.sub(h)).sub(rr.mul(two));
        FieldElement rz = s.mul(ss);
        return new MyProjectiveEllipticCurvePoint(structure, rx, ry, rz);
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint inv() {
        if (this.isNeutralElement())
            return this;
        
        FieldElement newY = y.neg();
        return new MyProjectiveEllipticCurvePoint(structure, x, newY, z);
    }
    
    @Override
    public String toString() {
        return "(" + x.toString() + "," + y.toString() + "," + z.toString() + ")";
    }
    
    @Override
    public boolean isNormalized() {
        return z.isOne();
    }
    
    @Override
    public boolean equals(Object element) {
        if (element == this)
            return true;
        
        if (!(element instanceof MyProjectiveEllipticCurvePoint))
            return false;
        
        MyProjectiveEllipticCurvePoint p = (MyProjectiveEllipticCurvePoint) element;
        if (this.isNeutralElement() && p.isNeutralElement())
            return true;
        
        if (this.isNeutralElement() || p.isNeutralElement())
            return false;
        
        if (!this.x.mul(p.z).equals(p.x.mul(z)))
            return false;
        
        return this.y.mul(p.z).equals(p.y.mul(z));
    }
    
}

