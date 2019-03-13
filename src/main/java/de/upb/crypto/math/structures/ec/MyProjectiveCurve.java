package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.structures.zn.Zp;

public class MyProjectiveCurve extends MyShortFormWeierstrassCurve {
    public MyProjectiveCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    MyAbstractEllipticCurvePoint createPoint(Zp.ZpElement x, Zp.ZpElement y) {
        return new MyProjectiveEllipticCurvePoint(this, x, y);
    }
}
