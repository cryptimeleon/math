package de.upb.crypto.math.swante;


import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.structures.zn.Zp;

// a point on a short form weierstrass curve, in affince coordinates
public class MyProjectiveEllipticCurvePoint extends MyAbstractEllipticCurvePoint {
    
    public MyProjectiveEllipticCurvePoint(MyShortFormWeierstrassCurve curve,
                                          Zp.ZpElement x, Zp.ZpElement y) {
        super(curve, x, y, curve.field.getOneElement());
    }
    
    public MyProjectiveEllipticCurvePoint(MyShortFormWeierstrassCurve curve,
                                      Zp.ZpElement x, Zp.ZpElement y, Zp.ZpElement z) {
        super(curve, x, y, z);
    }
    
    // returns point at infinity (neutral point)
    private MyProjectiveEllipticCurvePoint(MyShortFormWeierstrassCurve curve) {
        super(curve, curve.field.getZeroElement(),
                curve.field.getOneElement(), curve.field.getZeroElement());
    }
    
    public MyAffineEllipticCurvePoint toAffinePoint() {
        if (isNeutralElement()) {
            return new MyAffineEllipticCurvePoint(curve); // neutral element
        }
        Zp.ZpElement div = z.inv();
        return new MyAffineEllipticCurvePoint(curve, x.mul(div), y.mul(div));
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint createNewPoint(FieldElement x, FieldElement y) {
        return new MyProjectiveEllipticCurvePoint(curve, (Zp.ZpElement)x, (Zp.ZpElement)y);
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint normalize() {
        if (isNeutralElement()) {
            return this;
        }
        Zp.ZpElement div = z.inv();
        return new MyProjectiveEllipticCurvePoint(curve, x.mul(div), y.mul(div), curve.field.getOneElement());
    }
    
    
    @Override
    public MyProjectiveEllipticCurvePoint op(Element e) throws IllegalArgumentException {
        MyProjectiveEllipticCurvePoint q = (MyProjectiveEllipticCurvePoint) e;
        if (q.isNeutralElement()) {
            return this;
        }
        if (this.isNeutralElement()) {
            return q;
        }
        Zp.ZpElement t0 = y.mul(q.z);
        Zp.ZpElement t1 = q.y.mul(z);
        Zp.ZpElement u0 = x.mul(q.z);
        Zp.ZpElement u1 = q.x.mul(z);
        if (u0.equals(u1)) {
            if (t0.equals(t1)) {
                return this.times2();
            }
            return getPointAtInfinity();
        }
        FieldElement t = t0.sub(t1);
        FieldElement u = u0.sub(u1);
        FieldElement u2 = u.square();
        Zp.ZpElement v = z.mul(q.z);
        FieldElement w = t.mul(t).mul(v).sub(u2.mul(u0.add(u1)));
        FieldElement u3 = u.mul(u2);
        Zp.ZpElement rx = ((Zp.ZpElement) u.mul(w));
        Zp.ZpElement ry = ((Zp.ZpElement) t.mul(u0.mul(u2).sub(w)).sub(t0.mul(u3)));
        Zp.ZpElement rz = ((Zp.ZpElement) u3.mul(v));
        return new MyProjectiveEllipticCurvePoint(curve, rx, ry, rz);
    }
    
    // returns this+this
    private MyProjectiveEllipticCurvePoint times2() {
        if (this.isNeutralElement() || y.isZero()) {
            return getPointAtInfinity();
        }
        FieldElement t = x.square().mul(curve.three).add(z.square().mul(curve.a));
        Zp.ZpElement u = y.mul(z).mul(curve.two);
        Zp.ZpElement v = u.mul(x).mul(y).mul(curve.two);
        FieldElement w = t.square().sub(v.mul(curve.two));
        Zp.ZpElement rx = u.mul(w);
        FieldElement u2 = u.square();
        Zp.ZpElement ry = (Zp.ZpElement) t.mul(v.sub(w)).sub(u2.mul(y.square().mul(curve.two)));
        Zp.ZpElement rz = (Zp.ZpElement) u2.mul(u);
        return new MyProjectiveEllipticCurvePoint(curve, rx, ry, rz);
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint getPointAtInfinity() {
        return new MyProjectiveEllipticCurvePoint(curve);
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint invert() {
        if (this.isNeutralElement())
            return this;
        
        Zp.ZpElement newY = y.neg();
        return new MyProjectiveEllipticCurvePoint(curve, x, newY, z);
    }
    
    
    @Override
    public MyProjectiveEllipticCurvePoint add(MyAbstractEllipticCurvePoint Q) {
        return this.op(Q);
    }
    
    
    
    @Override
    public String toString() {
        return "(" + x.toString() + "," + y.toString() + "," + z.toString() + ")";
    }
    
    @Override
    public boolean isNormalized() {
        return z.isOne();
    }
    
}

