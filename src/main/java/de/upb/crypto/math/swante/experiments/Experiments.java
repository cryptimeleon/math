package de.upb.crypto.math.swante.experiments;

import de.upb.crypto.math.swante.MyShortFormWeierstrassCurveParameters;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.MyUtil.myAssert;

interface Element<E extends Element> {
    Structure<E> getStructure();
    
    @Override
    boolean equals(Object obj);
    
    @Override
    int hashCode();
}

interface Structure<E extends Element> {
    
    default BigInteger size() {
        throw new UnsupportedOperationException();
    }
    
    default E getUniformlyRandomElement() {
        throw new UnsupportedOperationException();
    }
    
}

interface Group<E extends GroupElement<E>> extends Structure<E> {
    E getNeutralElement();
    
    @Override
    E getUniformlyRandomElement();
    
    default E getUniformlyRandomNonNeutral() {
        E result;
        do {
            result = getUniformlyRandomElement();
        } while (result.isNeutralElement());
        
        return result;
    }
    
    default GroupElement getGenerator() {
        if (size().isProbablePrime(10000))
            return getUniformlyRandomNonNeutral();
        throw new UnsupportedOperationException("Can't compute generator for group: " + this);
    }
    
    default boolean isCommutative() {
        return true;
    }
    
    default int estimateCostOfInvert() {
        return 100;
    }
    
}

interface GroupElement<E extends GroupElement<E>> extends Element<E> {
    @Override
    Group<E> getStructure();
    
    E inv();
    
    E op(E e);
    
    default E pow(BigInteger k) { //default implementation: square&multiply algorithm
        if (k.signum() < 0)
            return pow(k.negate()).inv();
        E operand = (E) this;
        
        E result = getStructure().getNeutralElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (k.testBit(i))
                result = result.op(operand);
        }
        return result;
    }
    
    default E pow(long k) {
        return pow(BigInteger.valueOf(k));
    }
    
    default boolean isNeutralElement() {
        return this.equals(getStructure().getNeutralElement());
    }
}

interface RingElement<E extends RingElement<E>> extends Element<E> {
    @Override
    Ring<E> getStructure();
    
    E add(E e);
    
    E neg();
    
    default E sub(E e) {
        return add(e.neg());
    }
    
    E mul(E e);
    
    default E pow(BigInteger k) { //default implementation: square&multiply algorithm
        if (k.signum() < 0)
            return pow(k.negate()).inv();
        E result = getStructure().getOneElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.mul(result);
            if (k.testBit(i))
                result = this.mul(result);
        }
        return result;
    }
    
    E inv();
    
    default E div(E e) {
        return mul(e.inv());
    }
    
    default E square() {
        return this.mul((E) this);
    }
}


interface Ring<E extends RingElement<E>> extends Structure<E> {
    
    BigInteger sizeUnitGroup();
    
    E getZeroElement();
    
    E getOneElement();
    
    E getElement(BigInteger i);
    
    default E getElement(long i) {
        return getElement(BigInteger.valueOf(i));
    }
    
    boolean isCommutative();
}

interface FieldElement<E extends FieldElement<E>> extends RingElement<E> {
    
    @Override
    Field<E> getStructure();
    
    default boolean isZero() {
        return equals(getStructure().getZeroElement());
    }
    
}

interface Field<E extends FieldElement<E>> extends Ring<E> {
    
    @Override
    default boolean isCommutative() {
        return true;
    }
}

interface EllipticCurvePoint<G extends EllipticCurvePoint<G>> extends GroupElement<G> {
    G normalize();
    
    default boolean isNormalized() {
        return true;
    }
}

// G: usually the elliptic curve point type
// F; usually ZpElement
interface EllipticCurve<G extends GroupElement<G>,
        F extends FieldElement<F>> extends Group<G> {
    
    Field<F> getFieldOfDefinition();
    
    @Override
    default boolean isCommutative() {
        return true;
    }
}

class ZpElement implements FieldElement<ZpElement> {
    
    public final BigInteger value;
    public final Zp field;
    
    public ZpElement(Zp field, BigInteger value) {
        this.field = field;
        this.value = value.mod(field.p);
    }
    
    @Override
    public ZpElement add(ZpElement other) {
        return new ZpElement(field, value.add(other.value));
    }
    
    @Override
    public ZpElement neg() {
        return new ZpElement(field, value.negate());
    }
    
    @Override
    public ZpElement mul(ZpElement other) {
        return new ZpElement(field, value.multiply(other.value));
    }
    
    @Override
    public ZpElement inv() {
        return new ZpElement(field, new de.upb.crypto.math.structures.zn.Zp(field.p).new ZpElement(value).inv().getInteger());
    }
    
    @Override
    public Field<ZpElement> getStructure() {
        return field;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ZpElement))
            return false;
        
        ZpElement e = (ZpElement) obj;
        return value.equals(e.value);
    }
}

class Zp implements Field<ZpElement> {
    
    public final BigInteger p;
    
    public Zp(BigInteger p) {
        this.p = p;
    }
    
    @Override
    public BigInteger sizeUnitGroup() {
        return p;
    }
    
    @Override
    public ZpElement getZeroElement() {
        return new ZpElement(this, BigInteger.ZERO);
    }
    
    @Override
    public ZpElement getOneElement() {
        return new ZpElement(this, BigInteger.ONE);
    }
    
    @Override
    public ZpElement getElement(BigInteger i) {
        return new ZpElement(this, i);
    }
}

interface WeierstrassCurve<G extends EllipticCurvePoint<G>> extends EllipticCurve<G, ZpElement> {
    ZpElement getA6();
    
    ZpElement getA4();
    
    ZpElement getA3();
    
    ZpElement getA2();
    
    ZpElement getA1();
    
    G getElement(ZpElement x, ZpElement y);
    
    @Override
    Zp getFieldOfDefinition();
    
    default boolean isShortForm() {
        return getA3().isZero() && getA2().isZero() && getA1().isZero();
    }
    
}

abstract class AbstractEllipticCurvePoint<G extends AbstractEllipticCurvePoint<G>> implements EllipticCurvePoint<G> {
    protected final ZpElement x;
    protected final ZpElement y;
    protected final ZpElement z;
    
    protected final WeierstrassCurve<G> structure;
    
    public AbstractEllipticCurvePoint(WeierstrassCurve<G> curve, ZpElement x,
                                      ZpElement y, ZpElement z) {
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Zp getFieldOfDefinition() {
        return structure.getFieldOfDefinition();
    }
    
    public ZpElement getX() {
        return x;
    }
    
    public ZpElement getY() {
        return y;
    }
    
    public ZpElement getZ() {
        return z;
    }
    
    public WeierstrassCurve<G> getStructure() {
        return structure;
    }
    
    
    public String toString() {
        return "(" + x.toString() + ":" + y.toString() + ":" + z.toString() + ")";
    }
    
    @Override
    public int hashCode() {
        if (isNormalized())
            return getX().hashCode();
        else
            return normalize().getX().hashCode(); //todo do something more efficient
    }
    
    @Override
    public boolean isNeutralElement() {
        return z.isZero();
    }
    
}

class ExampleProjectiveEllipticCurvePoint extends AbstractEllipticCurvePoint<ExampleProjectiveEllipticCurvePoint> {
    
    public ExampleProjectiveEllipticCurvePoint(WeierstrassCurve<ExampleProjectiveEllipticCurvePoint> curve,
                                               ZpElement x, ZpElement y) {
        super(curve, x, y, curve.getFieldOfDefinition().getOneElement());
    }
    
    public ExampleProjectiveEllipticCurvePoint(WeierstrassCurve<ExampleProjectiveEllipticCurvePoint> curve,
                                               ZpElement x, ZpElement y, ZpElement z) {
        super(curve, x, y, z);
    }
    
    // returns point at infinity (neutral point)
    public ExampleProjectiveEllipticCurvePoint(WeierstrassCurve<ExampleProjectiveEllipticCurvePoint> curve) {
        super(curve, curve.getFieldOfDefinition().getZeroElement(),
                curve.getFieldOfDefinition().getOneElement(), curve.getFieldOfDefinition().getZeroElement());
    }
    
    
    @Override
    public ExampleProjectiveEllipticCurvePoint normalize() {
        if (isNeutralElement()) {
            return this;
        }
        ZpElement div = z.inv();
        return new ExampleProjectiveEllipticCurvePoint(structure, x.mul(div), y.mul(div), structure.getFieldOfDefinition().getOneElement());
    }
    
    
    @Override
    public ExampleProjectiveEllipticCurvePoint op(ExampleProjectiveEllipticCurvePoint Q) {
        if (Q.isNeutralElement()) {
            return this;
        }
        if (this.isNeutralElement()) {
            return Q;
        }
        ZpElement t0 = y.mul(Q.z);
        ZpElement t1 = Q.y.mul(z);
        ZpElement u0 = x.mul(Q.z);
        ZpElement u1 = Q.x.mul(z);
        if (u0.equals(u1)) {
            if (t0.equals(t1)) {
                return this.times2();
            }
            return structure.getNeutralElement();
        }
        ZpElement t = t0.sub(t1);
        ZpElement u = u0.sub(u1);
        ZpElement u2 = u.square();
        ZpElement v = z.mul(Q.z);
        ZpElement w = t.mul(t).mul(v).sub(u2.mul(u0.add(u1)));
        ZpElement u3 = u.mul(u2);
        ZpElement rx = u.mul(w);
        ZpElement ry = t.mul(u0.mul(u2).sub(w)).sub(t0.mul(u3));
        ZpElement rz = u3.mul(v);
        return new ExampleProjectiveEllipticCurvePoint(structure, rx, ry, rz); // todo: return actual type of this instance somehow
    }
    
    // returns this+this
    private ExampleProjectiveEllipticCurvePoint times2() {
        if (this.isNeutralElement() || y.isZero()) {
            return structure.getNeutralElement();
        }
        ZpElement t = x.square().mul(structure.getFieldOfDefinition().getElement(3)).add(z.square().mul(structure.getA4()));
        ZpElement two = structure.getFieldOfDefinition().getElement(2);
        ZpElement u = y.mul(z).mul(two);
        ZpElement v = u.mul(x).mul(y).mul(two);
        ZpElement w = t.square().sub(v.mul(two));
        ZpElement rx = u.mul(w);
        ZpElement u2 = u.square();
        ZpElement ry = t.mul(v.sub(w)).sub(u2.mul(y.square().mul(two)));
        ZpElement rz = u2.mul(u);
        return new ExampleProjectiveEllipticCurvePoint(structure, rx, ry, rz);
    }
    
    @Override
    public ExampleProjectiveEllipticCurvePoint inv() {
        if (this.isNeutralElement())
            return this;
        
        ZpElement newY = y.neg();
        return new ExampleProjectiveEllipticCurvePoint(structure, x, newY, z);
    }
    
    @Override
    public String toString() {
        return "(" + x.toString() + "," + y.toString() + "," + z.toString() + ")";
    }
    
    @Override
    public boolean isNormalized() {
        return z.value.equals(BigInteger.ONE);
    }
    
    @Override
    public boolean equals(Object element) {
        if (element == this)
            return true;
        
        if (!(element instanceof ExampleProjectiveEllipticCurvePoint))
            return false;
        
        ExampleProjectiveEllipticCurvePoint p = (ExampleProjectiveEllipticCurvePoint) element;
        if (this.isNeutralElement() && p.isNeutralElement())
            return true;
        
        if (this.isNeutralElement() || p.isNeutralElement())
            return false;
        
        if (!this.x.mul(p.z).equals(p.x.mul(z)))
            return false;
        
        return this.y.mul(p.z).equals(p.y.mul(z));
    }
    
}

abstract class ExampleShortFormWeierstrassCurve<G extends AbstractEllipticCurvePoint<G>> implements WeierstrassCurve<G> {
    
    public final Zp field;
    public final ZpElement a;
    public final ZpElement b;
    public final ZpElement n;
    public final ZpElement h;
    public final G generator;
    public final ZpElement two;
    public final ZpElement three;
    
    public ExampleShortFormWeierstrassCurve(MyShortFormWeierstrassCurveParameters parameters) {
        field = new Zp(parameters.p);
        two = new ZpElement(field, BigInteger.valueOf(2));
        three = new ZpElement(field, BigInteger.valueOf(3));
        a = new ZpElement(field, parameters.a);
        b = new ZpElement(field, parameters.b);
        n = new ZpElement(field, parameters.n);
        h = new ZpElement(field, parameters.h);
        generator = this.getElement(new ZpElement(field, parameters.gx), new ZpElement(field, parameters.gy));
    }
    
    @Override
    public abstract G getNeutralElement();
    
    @Override
    public abstract G getElement(ZpElement x, ZpElement y);
    
    @Override
    public G getGenerator() {
        return generator;
    }
    
    @Override
    public ZpElement getA6() {
        return b;
    }
    
    @Override
    public ZpElement getA4() {
        return a;
    }
    
    @Override
    public ZpElement getA3() {
        return field.getZeroElement();
    }
    
    @Override
    public ZpElement getA2() {
        return field.getZeroElement();
    }
    
    @Override
    public ZpElement getA1() {
        return field.getZeroElement();
    }
    
    @Override
    public Zp getFieldOfDefinition() {
        return field;
    }
    
    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return n.value;
    }
    
    @Override
    public G getUniformlyRandomElement() {
        return generator.pow(field.getUniformlyRandomElement().value);
    }
    
    
    public String parametersToString() {
        return String.format("p=%s\na=%s\nb=%s\nG.x=%s\nG.y=%s\nn=%s\nh=%s", field.size(), a, b, generator.getX(), generator.getY(), n, h);
    }
}

class ExampleProjectiveCurve extends ExampleShortFormWeierstrassCurve<ExampleProjectiveEllipticCurvePoint> {
    public ExampleProjectiveCurve(MyShortFormWeierstrassCurveParameters parameters) {
        super(parameters);
    }
    
    @Override
    public ExampleProjectiveEllipticCurvePoint getNeutralElement() {
        return new ExampleProjectiveEllipticCurvePoint(this);
    }
    
    @Override
    public ExampleProjectiveEllipticCurvePoint getElement(ZpElement x, ZpElement y) {
        return new ExampleProjectiveEllipticCurvePoint(this, x, y, getFieldOfDefinition().getOneElement());
    }
    
    @Override
    public String toString() {
        return "ProjectiveCurve";
    }
    
}

public class Experiments {
    public static void main(String[] args) {
        MyShortFormWeierstrassCurveParameters parameters = MyShortFormWeierstrassCurveParameters.createSecp256r1CurveParameters();
        ExampleProjectiveCurve curve = new ExampleProjectiveCurve(parameters);
        ExampleProjectiveEllipticCurvePoint g = curve.generator;
        ExampleProjectiveEllipticCurvePoint gg = g.op(g);
        myAssert(gg.normalize().getX().value.equals(new BigInteger("56515219790691171413109057904011688695424810155802929973526481321309856242040")));
        myAssert(g.pow(101).normalize().getX().value.equals(new BigInteger("93980847734016439027508041847036757272229093243964019053297849828346202436527")));
    }
}