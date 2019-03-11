package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Optional;

// y^2 = x^3 + ax + b
public class MyShortFormWeierstrassCurve implements WeierstrassCurve {
    
    private Zp field;
    private final FieldElement a;
    private final FieldElement b;
    
    public MyShortFormWeierstrassCurve(Zp field, FieldElement a, FieldElement b) {
        this.field = field;
        this.a = a;
        this.b = b;
    }
    
    @Override
    public FieldElement getA6() {
        return b;
    }
    
    @Override
    public FieldElement getA4() {
        return a;
    }
    
    @Override
    public FieldElement getA3() {
        return field.getZeroElement();
    }
    
    @Override
    public FieldElement getA2() {
        return field.getZeroElement();
    }
    
    @Override
    public FieldElement getA1() {
        return field.getZeroElement();
    }
    
    @Override
    public EllipticCurvePoint getElement(FieldElement x, FieldElement y) {
        return null;
    }
    
    @Override
    public Field getFieldOfDefinition() {
        return field;
    }
    
    @Override
    public GroupElement getNeutralElement() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return null;
    }
    
    @Override
    public GroupElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return null;
    }
    
    @Override
    public GroupElement getElement(Representation repr) {
        return null;
    }
    
    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.empty();
    }
    
    @Override
    public int estimateCostOfInvert() {
        return 100;
    }
    
    @Override
    public Representation getRepresentation() {
        return null;
    }
}
