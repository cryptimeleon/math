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
    public AbstractEllipticCurvePoint add(AbstractEllipticCurvePoint Q) throws IllegalArgumentException {
        if (Q.isNeutralElement()) {
            return this;
        }
        if (this.isNeutralElement()) {
            return Q;
        }
        FieldElement t0 = y.mul(Q.z);
        FieldElement t1 = Q.y.mul(z);
        FieldElement u0 = x.mul(Q.z);
        FieldElement u1 = Q.x.mul(z);
        if (u0.equals(u1)) {
            if (t0.equals(t1)) {
                return this.times2();
            }
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement t = t0.sub(t1);
        FieldElement u = u0.sub(u1);
        FieldElement u2 = u.square();
        FieldElement v = z.mul(Q.z);
        FieldElement w = t.square().mul(v).sub(u2.mul(u0.add(u1)));
        FieldElement u3 = u.mul(u2);
        FieldElement rx = u.mul(w);
        FieldElement ry = t.mul(u0.mul(u2).sub(w)).sub(t0.mul(u3));
        FieldElement rz = u3.mul(v);
        return new MyProjectiveEllipticCurvePoint(structure, rx, ry, rz); // todo: return actual type of this instance somehow
    }
    
    // returns this+this
    private AbstractEllipticCurvePoint times2() {
        if (this.isNeutralElement() || y.isZero()) {
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement t = x.square().mul(structure.getFieldOfDefinition().getElement(3)).add(z.square().mul(structure.getA4()));
        FieldElement two = structure.getFieldOfDefinition().getElement(2);
        FieldElement u = y.mul(z).mul(two);
        FieldElement v = u.mul(x).mul(y).mul(two);
        FieldElement w = t.square().sub(v.mul(two));
        FieldElement rx = u.mul(w);
        FieldElement u2 = u.square();
        FieldElement ry = t.mul(v.sub(w)).sub(u2.mul(y.square().mul(two)));
        FieldElement rz = u2.mul(u);
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

