package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.structures.zn.Zp;

public class MyAffineCurve extends MyShortFormWeierstrassCurve {
    public MyAffineCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    MyAbstractEllipticCurvePoint createPoint(Zp.ZpElement x, Zp.ZpElement y) {
        return new MyAffineEllipticCurvePoint(this, x, y);
    }
}
