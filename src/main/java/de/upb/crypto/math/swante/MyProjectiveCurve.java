package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.structures.ec.MyProjectiveEllipticCurvePoint;
import de.upb.crypto.math.swante.util.MyShortFormWeierstrassCurveParameters;

/**
 * class representing a curve with points in projective coordinates
 */
public class MyProjectiveCurve extends MyShortFormWeierstrassCurve {
    public MyProjectiveCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint getNeutralElement() {
        return new MyProjectiveEllipticCurvePoint(this);
    }
    
    @Override
    public MyProjectiveEllipticCurvePoint getElement(FieldElement x, FieldElement y) {
        return new MyProjectiveEllipticCurvePoint(this, x, y, getFieldOfDefinition().getOneElement());
    }
    
    @Override
    public String toString() {
        return "ProjectiveCurve";
    }
    
}
