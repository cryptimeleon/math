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
        E operand = (E)this;
        
        E result = getStructure().getNeutralElement();
        for (int i = k.bitLength() - 1; i >= 0; i--) {
            result = result.op(result);
            if (k.testBit(i))
                result = result.op(operand);
        }
        return result;
    }
    
    default GroupElement pow(long k) {
        return pow(BigInteger.valueOf(k));
    }
    
    default boolean isNeutralElement() {
        return this.equals(getStructure().getNeutralElement());
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