package de.upb.crypto.math.swante;

import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;

public class MyAffineCurve extends MyShortFormWeierstrassCurve {
    public MyAffineCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    AbstractEllipticCurvePoint createPoint(Zp.ZpElement x, Zp.ZpElement y) {
        return new MyAffineEllipticCurvePoint(this, x, y);
    }
    
    @Override
    public String toString() {
        return String.format("AffineCurve with parameters:\n%s", super.parametersToString());
    }
}
