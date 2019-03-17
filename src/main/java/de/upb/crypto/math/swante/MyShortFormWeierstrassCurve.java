package de.upb.crypto.math.swante;

import de.upb.crypto.math.interfaces.structures.EllipticCurvePoint;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.ec.AffineEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;

import java.math.BigInteger;
import java.util.Optional;

/**
 * curve in weierstrass short form: y^2 = x^3 + ax + b
 * with size n, and cofactor h, such that n*h = |E|
 * the actual type of the generator point determines the type of points
 * used for computations
 * @author Swante
 */

public abstract class MyShortFormWeierstrassCurve implements WeierstrassCurve {
    
    public final Zp field;
    public final Zp.ZpElement a;
    public final Zp.ZpElement b;
    public final Zp.ZpElement n;
    public final Zp.ZpElement h;
    public final AbstractEllipticCurvePoint generator;
    public final Zp.ZpElement two;
    public final Zp.ZpElement three;
    
    public MyShortFormWeierstrassCurve(MyShortFormWeierstrassCurveParameters parameters) {
        field = new Zp(parameters.p);
        two = field.new ZpElement(BigInteger.valueOf(2));
        three = field.new ZpElement(BigInteger.valueOf(3));
        a = field.new ZpElement(parameters.a);
        b = field.new ZpElement(parameters.b);
        n = field.new ZpElement(parameters.n);
        h = field.new ZpElement(parameters.h);
        generator = this.getElement(field.new ZpElement(parameters.gx), field.new ZpElement(parameters.gy));
    }
    
    @Override
    public abstract AbstractEllipticCurvePoint getNeutralElement();
    
    @Override
    public abstract AbstractEllipticCurvePoint getElement(FieldElement x, FieldElement y);
    
    @Override
    public AbstractEllipticCurvePoint getGenerator() {
        return generator;
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
    public Field getFieldOfDefinition() {
        return field;
    }
    
    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return n.getInteger();
    }
    
    @Override
    public AbstractEllipticCurvePoint getUniformlyRandomElement() throws UnsupportedOperationException {
        return (AbstractEllipticCurvePoint) this.getGenerator().pow(field.getUniformlyRandomElement().getInteger());
    }
    
    @Override
    public AbstractEllipticCurvePoint getElement(Representation repr) {
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
    
    
    protected String parametersToString() {
        return String.format("p=%s\na=%s\nb=%s\nG.x=%s\nG.y=%s\nn=%s\nh=%s", field.size(), a, b, generator.getX(), generator.getY(), n, h);
    }
}
