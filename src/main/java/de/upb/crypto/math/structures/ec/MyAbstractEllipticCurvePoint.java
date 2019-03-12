package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.structures.zn.Zp;

// children should override invert and add. compute line is not used.
abstract public class MyAbstractEllipticCurvePoint implements EllipticCurvePoint {
    
    
    final MyShortFormWeierstrassCurve curve;
    final Zp.ZpElement x;
    final Zp.ZpElement y;
    final Zp.ZpElement z;
    
    public MyAbstractEllipticCurvePoint(MyShortFormWeierstrassCurve curve, Zp.ZpElement x, Zp.ZpElement y, Zp.ZpElement z) {
        
        this.curve = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public abstract MyAbstractEllipticCurvePoint createNewPoint(FieldElement x, FieldElement y);
    public abstract MyAbstractEllipticCurvePoint getPointAtInfinity();
    public abstract MyAbstractEllipticCurvePoint invert();
    public abstract MyAbstractEllipticCurvePoint add(MyAbstractEllipticCurvePoint Q);
    
    @Override
    public GroupElement op(Element e) throws IllegalArgumentException {
        MyAbstractEllipticCurvePoint Q = (MyAbstractEllipticCurvePoint) e;
        return this.add(Q);
    }
    
    @Override
    public FieldElement[] computeLine(EllipticCurvePoint Q) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public EllipticCurvePoint add(EllipticCurvePoint P, FieldElement[] line) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public GroupElement inv() {
        return this.invert();
    }
    
    @Override
    public boolean isNeutralElement() {
        return z.isZero();
    }
    
    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        throw new UnsupportedOperationException();
    }
}
