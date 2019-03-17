package de.upb.crypto.math.swante;

import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.ec.MyProjectiveEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;

public class MyProjectiveCurve extends MyShortFormWeierstrassCurve {
    public MyProjectiveCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    AbstractEllipticCurvePoint createPoint(Zp.ZpElement x, Zp.ZpElement y) {
        return new MyProjectiveEllipticCurvePoint(this, x, y);
    }
    
    @Override
    public String toString() {
        return "ProjectiveCurve";
    }
    
}
