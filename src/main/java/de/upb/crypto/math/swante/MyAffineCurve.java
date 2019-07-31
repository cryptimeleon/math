package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.structures.ec.AffineEllipticCurvePoint;
import de.upb.crypto.math.swante.util.MyShortFormWeierstrassCurveParameters;

/**
 * class representing an affine curve
 */
public class MyAffineCurve extends MyShortFormWeierstrassCurve {
    public MyAffineCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    public AffineEllipticCurvePoint getNeutralElement() {
        return new AffineEllipticCurvePoint(this);
    }
    
    @Override
    public AffineEllipticCurvePoint getElement(FieldElement x, FieldElement y) {
        return new AffineEllipticCurvePoint(this, x, y);
    }
    
    @Override
    public String toString() {
        return "AffineCurve";
    }
}
