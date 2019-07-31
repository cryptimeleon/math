package de.upb.crypto.math.structures.ec;


import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.swante.util.MyGlobals;

/**
 * a point on a short form weierstrass curve, in projective coordinates
 * representing the affine point (x/z, y/z)
 */
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
    public AbstractEllipticCurvePoint addAssumingZ2IsOne(AbstractEllipticCurvePoint Q) {
        FieldElement t0 = Q.y.mul(z);
        FieldElement u = t0.sub(y);
        FieldElement t1 = Q.x.mul(z);
        FieldElement v = t1.sub(x);
        if (v.isZero()) {
            if (u.isZero()) {
                return this.square();
            }
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement uu = u.square();
        FieldElement vv = v.square();
        FieldElement vvv = v.mul(vv);
        FieldElement R = vv.mul(x);
        FieldElement two = structure.getFieldOfDefinition().getElement(2);
        FieldElement t2 = R.mul(two);
        FieldElement t3 = uu.mul(z);
        FieldElement t4 = t3.sub(vvv);
        FieldElement A = t4.sub(t2);
        FieldElement X3 = v.mul(A);
        FieldElement t5 = R.sub(A);
        FieldElement t6 = vvv.mul(y);
        FieldElement t7 = u.mul(t5);
        FieldElement Y3 = t7.sub(t6);
        FieldElement Z3 = vvv.mul(z);
        return new MyProjectiveEllipticCurvePoint(structure, X3, Y3, Z3);
    }
    
    @Override
    public AbstractEllipticCurvePoint add(AbstractEllipticCurvePoint Q) throws IllegalArgumentException {
        if (Q == this) {
            return this.square();
        }
        if (Q.isNeutralElement()) {
            return this;
        }
        if (this.isNeutralElement()) {
            return Q;
        }
        if (MyGlobals.useCurvePointNormalizationPowOptimization && Q.isNormalized()) {
            return addAssumingZ2IsOne(Q);
        }
        FieldElement x1z2 = x.mul(Q.z);
        FieldElement v = Q.x.mul(z).sub(x1z2);
        FieldElement y1z2 = y.mul(Q.z);
        FieldElement u = Q.y.mul(z).sub(y1z2);
        if (v.isZero()) {
            if (u.isZero()) {
                return this.square();
            }
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement uu = u.square();
        FieldElement vv = v.square();
        FieldElement vvv = v.mul(vv);
        FieldElement r = vv.mul(x1z2);
        FieldElement z1z2 = z.mul(Q.z);
        FieldElement two = structure.getFieldOfDefinition().getElement(2);
        FieldElement a = uu.mul(z1z2).sub(vvv).sub(r.mul(two));
        FieldElement rx = v.mul(a);
        FieldElement ry = u.mul(r.sub(a)).sub(vvv.mul(y1z2));
        FieldElement rz = vvv.mul(z1z2);
        return new MyProjectiveEllipticCurvePoint(structure, rx, ry, rz);
    }
    
    // returns this+this
    @Override
    public AbstractEllipticCurvePoint square() {
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
        
        // the equality check is a bit more tricky here, compared to affine
        // coordinates, since Z has to be taken into account
        if (!this.x.mul(p.z).equals(p.x.mul(z)))
            return false;
        
        return this.y.mul(p.z).equals(p.y.mul(z));
    }
    
}

