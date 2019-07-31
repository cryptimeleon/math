package de.upb.crypto.math.structures.ec;


import de.upb.crypto.math.interfaces.structures.*;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.swante.util.MyGlobals;

/**
 * a point on a short form weierstrass curve, in Jacobian coordinates
 * representing the affine point (x/z^2, y/z^3)
 */
public class MyJacobiEllipticCurvePoint extends AbstractEllipticCurvePoint {
    
    public MyJacobiEllipticCurvePoint(WeierstrassCurve curve,
                                          FieldElement x, FieldElement y) {
        super(curve, x, y, curve.getFieldOfDefinition().getOneElement());
    }
    
    public MyJacobiEllipticCurvePoint(WeierstrassCurve curve,
                                          FieldElement x, FieldElement y, FieldElement z) {
        super(curve, x, y, z);
    }
    
    // returns point at infinity (neutral point)
    public MyJacobiEllipticCurvePoint(WeierstrassCurve curve) {
        super(curve, curve.getFieldOfDefinition().getZeroElement(),
                curve.getFieldOfDefinition().getOneElement(), curve.getFieldOfDefinition().getZeroElement());
    }
    
    public AffineEllipticCurvePoint toAffinePoint() {
        if (isNeutralElement()) {
            return new AffineEllipticCurvePoint(structure); // neutral element
        }
        FieldElement div = z.inv();
        FieldElement div2 = div.square();
        FieldElement div3 = div2.mul(div);
        return new AffineEllipticCurvePoint(structure, x.mul(div2), y.mul(div3));
    }
    
    @Override
    public MyJacobiEllipticCurvePoint normalize() {
        if (isNeutralElement()) {
            return this;
        }
        FieldElement div = z.inv();
        FieldElement div2 = div.square();
        FieldElement div3 = div2.mul(div);
        return new MyJacobiEllipticCurvePoint(structure, x.mul(div2), y.mul(div3), structure.getFieldOfDefinition().getOneElement());
    }
    
    @Override
    public AbstractEllipticCurvePoint addAssumingZ2IsOne(AbstractEllipticCurvePoint Q) {
        FieldElement Z1Z1 = z.square();
        FieldElement U2 = Q.x.mul(Z1Z1);
        FieldElement t0 = z.mul(Z1Z1);
        FieldElement S2 = Q.y.mul(t0);
        FieldElement H = U2.sub(x);
        FieldElement t1 = S2.sub(y);
        if (H.isZero()) {
            if (t1.isZero()) {
                return this.square();
            }
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement HH = H.square();
        Field field = structure.getFieldOfDefinition();
        FieldElement two = field.getElement(2);
        FieldElement four = field.getElement(4);
        FieldElement I = HH.mul(four);
        FieldElement J = H.mul(I);
        FieldElement r = t1.mul(two);
        FieldElement V = x.mul(I);
        FieldElement t2 = r.square();
        FieldElement t3 = V.mul(two);
        FieldElement t4 = t2.sub(J);
        FieldElement X3 = t4.sub(t3);
        FieldElement t5 = V.sub(X3);
        FieldElement t6 = y.mul(J);
        FieldElement t7 = t6.mul(two);
        FieldElement t8 = r.mul(t5);
        FieldElement Y3 = t8.sub(t7);
        FieldElement t9 = z.add(H);
        FieldElement t10 = t9.square();
        FieldElement t11 = t10.sub(Z1Z1);
        FieldElement Z3 = t11.sub(HH);
        return new MyJacobiEllipticCurvePoint(structure, X3, Y3, Z3);
    }
    
    
    @Override
    public AbstractEllipticCurvePoint add(AbstractEllipticCurvePoint Q) throws IllegalArgumentException {
        if (Q == this) {
            return square();
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
        FieldElement z1Sq = z.square();
        FieldElement z2Sq = Q.z.square();
        FieldElement U1 = x.mul(z2Sq);
        FieldElement U2 = Q.x.mul(z1Sq);
        FieldElement S1 = y.mul(Q.z).mul(z2Sq);
        FieldElement S2 = Q.y.mul(z).mul(z1Sq);
        FieldElement H = U2.sub(U1);
        FieldElement sDiff = S2.sub(S1);
        if (H.isZero()) {
            if (sDiff.isZero()) {
                return this.square();
            }
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        Field field = structure.getFieldOfDefinition();
        FieldElement two = field.getElement(2);
        FieldElement hDoubled = H.mul(two);
        FieldElement I = hDoubled.square();
        FieldElement J = H.mul(I);
        FieldElement r = sDiff.mul(two);
        FieldElement V = U1.mul(I);
        FieldElement rx = r.square().sub(J).sub(V.mul(two));
        FieldElement ry = r.mul(V.sub(rx)).sub(S1.mul(J).mul(two));
        FieldElement z1PlusZ2 = z.add(Q.z);
        FieldElement rz = (z1PlusZ2.square().sub(z1Sq).sub(z2Sq)).mul(H);
        return new MyJacobiEllipticCurvePoint(structure, rx, ry, rz); // todo: return actual type of this instance somehow
    }
    
    // returns this+this
    @Override
    public AbstractEllipticCurvePoint square() {
        if (this.isNeutralElement() || y.isZero()) {
            return (AbstractEllipticCurvePoint)structure.getNeutralElement();
        }
        FieldElement xSq = x.square();
        FieldElement ySq = y.square();
        FieldElement yPow4 = ySq.square();
        FieldElement zSq = z.square();
        FieldElement x1PlusYy = x.add(ySq);
        Field field = structure.getFieldOfDefinition();
        FieldElement two = field.getElement(2);
        FieldElement S = x1PlusYy.square().sub(xSq).sub(yPow4).mul(two);
        FieldElement M = xSq.mul(field.getElement(3)).add(structure.getA4().mul(zSq.square()));
        FieldElement rx = M.square().sub(S.mul(two));
        FieldElement ry = M.mul(S.sub(rx)).sub(yPow4.mul(field.getElement(8)));
        FieldElement y1PlusZ1 = y.add(z);
        FieldElement rz = y1PlusZ1.square().sub(ySq).sub(zSq);
        return new MyJacobiEllipticCurvePoint(structure, rx, ry, rz);
    }
    
    @Override
    public MyJacobiEllipticCurvePoint inv() {
        if (this.isNeutralElement())
            return this;
        
        FieldElement newY = y.neg();
        return new MyJacobiEllipticCurvePoint(structure, x, newY, z);
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
        
        if (!(element instanceof MyJacobiEllipticCurvePoint))
            return false;
        
        MyJacobiEllipticCurvePoint p = (MyJacobiEllipticCurvePoint) element;
        if (this.isNeutralElement() && p.isNeutralElement())
            return true;
        
        if (this.isNeutralElement() || p.isNeutralElement())
            return false;
    
        // the equality check is a bit more tricky here, compared to affine
        // coordinates, since Z has to be taken into account
        if (!this.x.mul(p.z.square()).equals(p.x.mul(z.square())))
            return false;
        
        return this.y.mul(p.z.square().mul(p.z)).equals(p.y.mul(z.square().mul(z)));
    }
    
}

