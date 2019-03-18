package de.upb.crypto.math.swante;

import java.math.BigInteger;

import static de.upb.crypto.math.swante.misc.pln;

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
    
    default boolean isNormalized() {return true;}
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

interface WeierstrassCurve<G extends EllipticCurvePoint<G>, F extends FieldElement<F>> extends EllipticCurve<G,F> {
    F getA6();
    
    F getA4();
    
    F getA3();
    
    F getA2();
    
    F getA1();
    
    G getElement(F x, F y);
    
    default boolean isShortForm() {
        return getA3().isZero() && getA2().isZero() && getA1().isZero();
    }
    
}

abstract class AbstractEllipticCurvePoint<G extends AbstractEllipticCurvePoint<G,F>, F extends FieldElement<F>> implements EllipticCurvePoint<G> {
    protected F x, y, z;
    
    protected WeierstrassCurve<G, F> structure;
    
    public AbstractEllipticCurvePoint(WeierstrassCurve<G, F> curve, F x, F y, F z) {
        this.structure = curve;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Field<F> getFieldOfDefinition() {
        return structure.getFieldOfDefinition();
    }
    
    public F getX() {
        return x;
    }
    
    public F getY() {
        return y;
    }
    
    public F getZ() {
        return z;
    }
    
    public WeierstrassCurve<G, F> getStructure() {
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


class A implements GroupElement<A> {
    
    public final int a;
    
    public A(int a) {
        this.a = a;
    }
    
    @Override
    public Group<A> getStructure() {
        return new B();
    }
    
    @Override
    public A inv() {
        return new A(-a);
    }
    
    @Override
    public A op(A other) {
        return new A(a + other.a);
    }
    
    @Override
    public String toString() {
        return "A(" + a + ")";
    }
}

class B implements Group<A> {
    
    @Override
    public A getNeutralElement() {
        return new A(0);
    }
    
    @Override
    public A getUniformlyRandomElement() {
        return null;
    }
}

public class Experiments {
    public static void main(String[] args) {
        A a = new A(3);
        pln(a.pow(4));
    }
}