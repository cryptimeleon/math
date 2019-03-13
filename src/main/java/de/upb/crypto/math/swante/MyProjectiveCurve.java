package de.upb.crypto.math.swante;

import de.upb.crypto.math.structures.zn.Zp;

public class MyProjectiveCurve extends MyShortFormWeierstrassCurve {
    public MyProjectiveCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    MyAbstractEllipticCurvePoint createPoint(Zp.ZpElement x, Zp.ZpElement y) {
        return new MyProjectiveEllipticCurvePoint(this, x, y);
    }
    
    @Override
    public String toString() {
        return String.format("ProjectiveCurve with parameters:\n%s", super.parametersToString());
    }
    
}
