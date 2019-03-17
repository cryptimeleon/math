package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.structures.ec.MyJacobiEllipticCurvePoint;
import de.upb.crypto.math.structures.ec.MyProjectiveEllipticCurvePoint;

public class MyJacobiCurve extends MyShortFormWeierstrassCurve {
    
    public MyJacobiCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    public MyJacobiEllipticCurvePoint getNeutralElement() {
        return new MyJacobiEllipticCurvePoint(this);
    }
    
    @Override
    public MyJacobiEllipticCurvePoint getElement(FieldElement x, FieldElement y) {
        return new MyJacobiEllipticCurvePoint(this, x, y, getFieldOfDefinition().getOneElement());
    }
    
    @Override
    public String toString() {
        return "JacobiCurve";
    }
}
