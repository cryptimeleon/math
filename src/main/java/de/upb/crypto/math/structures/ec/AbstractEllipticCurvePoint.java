package de.upb.crypto.math.structures.ec;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.swante.MyGlobals;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigInteger;

public abstract class AbstractEllipticCurvePoint implements EllipticCurvePoint {
    protected FieldElement x, y, z;
    
    protected WeierstrassCurve structure;
    
    public AbstractEllipticCurvePoint(WeierstrassCurve curve, FieldElement x, FieldElement y, FieldElement z) {
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * mixed addition (projective/jabobian + affine)
     * @param Q, must be normalized
     * @return sum of this + Q
     */
    public AbstractEllipticCurvePoint addAssumingZ2IsOne(AbstractEllipticCurvePoint Q) {
        throw new NotImplementedException();
    }
    
    @Override
    public AbstractEllipticCurvePoint prepareForPow(BigInteger exponent) {
        if (MyGlobals.useCurvePointNormalizationPowOptimization &&
                (exponent == null || exponent.bitLength() > MyGlobals.curvePointNormalizationOptimizationThreshold) &&
                !isNormalized()) {
            return normalize();
        }
        return this;
    }
    
    /**
     * apply frobenius function to this instance, in-place
     * @param p prime order of finite field
     */
    public void applyFrobenius(BigInteger p) {
        x = x.pow(p);
        y = y.pow(p);
    }
    
    public abstract AbstractEllipticCurvePoint add(AbstractEllipticCurvePoint Q);
    
    @Override
    public abstract AbstractEllipticCurvePoint inv();
    
    @Override
    public abstract AbstractEllipticCurvePoint normalize();
    
    @Override
    public AbstractEllipticCurvePoint op(Element e) throws IllegalArgumentException {
        AbstractEllipticCurvePoint Q = (AbstractEllipticCurvePoint) e;
        return this.add(Q);
    }
    
    public Field getFieldOfDefinition() {
        return structure.getFieldOfDefinition();
    }
    
    public FieldElement getX() {
        return x;
    }
    
    public FieldElement getY() {
        return y;
    }
    
    public FieldElement getZ() {
        return z;
    }
    
    public WeierstrassCurve getStructure() {
        return structure;
    }
    
    @Override
    public Representation getRepresentation() {
        /*
         * normalize point to save memory for z-coordinates
         */
        AbstractEllipticCurvePoint normalized = (AbstractEllipticCurvePoint) this.normalize();
        ObjectRepresentation r = new ObjectRepresentation();
        r.put("x", normalized.getX().getRepresentation());
        r.put("y", normalized.getY().getRepresentation());
        r.put("z", normalized.getZ().getRepresentation()); //basically in this to represent the neutral element
        return r;
    }
    
    public String toString() {
        return "(" + x.toString() + ":" + y.toString() + ":" + z.toString() + ")";
    }
    
    @Override
    public int hashCode() {
        if (isNormalized())
            return getX().hashCode();
        else
            return ((AbstractEllipticCurvePoint) normalize()).getX().hashCode(); //todo do something more efficient
    }
    
    @Override
    public boolean isNeutralElement() {
        return z.isZero();
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
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        throw new UnsupportedOperationException();
    }
}
