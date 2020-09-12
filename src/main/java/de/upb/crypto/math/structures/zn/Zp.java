package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.random.interfaces.RandomGeneratorSupplier;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.helpers.FiniteFieldTools;

import java.math.BigInteger;

/**
 * A special case for the ring Zn, where n is prime.
 * (Making the ring a field).
 * <p>
 * This completely reuses the Zn implementation.
 */
public class Zp extends Zn implements Field {

    /**
     * Construct the field Zp.
     *
     * @param p a prime number (is checked probabilistically)
     * @throws IllegalArgumentException if p is not prime
     */
    public Zp(BigInteger p) {
        super(p);
        if (!p.isProbablePrime(100))
            throw new IllegalArgumentException(p + " is not prime.");
    }

    public Zp(Representation repr) {
        super(repr);
        if (!n.isProbablePrime(100))
            throw new IllegalArgumentException(n + " is not prime");
    }

    @Override
    public BigInteger sizeUnitGroup() {
        return n.subtract(BigInteger.ONE);
    }

    @Override
    public boolean hasPrimeSize() throws UnsupportedOperationException {
        return true;
    }

    @Override
    public ZpElement getUniformlyRandomUnit() throws UnsupportedOperationException {
        return createZnElement(RandomGeneratorSupplier.getRnd().getRandomElement(n.subtract(BigInteger.ONE)).add(BigInteger.ONE));
    }

    @Override
    public ZpElement getUniformlyRandomNonzeroElement() {
        return (ZpElement) super.getUniformlyRandomNonzeroElement();
    }

    @Override
    public ZpElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return (ZpElement) super.getUniformlyRandomElement();
    }

    @Override
    public ZpElement getOneElement() {
        return (ZpElement) super.getOneElement();
    }

    @Override
    public ZpElement getZeroElement() {
        return (ZpElement) super.getZeroElement();
    }

    @Override
    public ZpElement getPrimitiveElement() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ZpElement getElement(Representation repr) {
        return (ZpElement) super.getElement(repr);
    }

    @Override
    public ZpElement getElement(long i) {
        return (ZpElement) super.getElement(i);
    }

    /**
     * @see ZnElement
     */
    public class ZpElement extends ZnElement implements FieldElement {
        public ZpElement(BigInteger v) {
            super(v);
        }

        @Override
        public Zp getStructure() {
            return Zp.this;
        }

        @Override
        public ZpElement add(Element e) {
            return (ZpElement) super.add(e);
        }
    
        @Override
        public ZpElement sub(Element e) {
            return (ZpElement) super.sub(e);
        }

        @Override
        public ZpElement neg() {
            return (ZpElement) super.neg();
        }

        @Override
        public ZpElement mul(Element e) {
            return (ZpElement) super.mul(e);
        }

        @Override
        public ZpElement mul(BigInteger k) {
            return (ZpElement) super.mul(k);
        }

        @Override
        public ZpElement mul(long k) {
            return (ZpElement) super.mul(k);
        }

        @Override
        public ZpElement inv() throws UnsupportedOperationException {
            return (ZpElement) super.inv();
        }

        @Override
        public ZpElement pow(BigInteger k) {
            return (ZpElement) super.pow(k);
        }

        @Override
        public ZpElement pow(long k) {
            return (ZpElement) super.pow(k);
        }

        /**
         * Returns true if there is a y\in Zp such that y^2 = this
         */
        public boolean isSquare() {
            return FiniteFieldTools.isSquare(this);
        }

        /**
         * Computes a square root of this element if it exists.
         *
         * @return an element x with x^2 = this
         * @throws ArithmeticException if element is not a quadratic residue
         */
        public ZpElement sqrt() throws ArithmeticException {
            if (this.isZero())
                return this;

            //see Lemma 11.22 of "Elliptic and Hyperelliptic Curve Cryptography"
            BigInteger p = n;
            ZpElement result;
            int pMod8 = p.mod(BigInteger.valueOf(8)).intValue();
            if (1 != pMod8) { //for p!=1 mod 8 a deterministic algorithm exists
                if (3 == (pMod8 % 4)) {
                    //for p=3 mod 4, a^((p+1)/4) is root of a
                    result = this.pow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4)));
                } else {
                    //for p=5 mod 8, and a^((p-1)/4)= 1, a^((p+3)/8) is root of a
                    //for p=5 mod 8, and a^((p-1)/4)=-1, 2a (4a)^((p-5)/8) is root of a
                    if (5 == pMod8) {
                        ZpElement t;
                        //a^((p+3)/8)
                        result = this.pow(p.add(BigInteger.valueOf(3)).divide(BigInteger.valueOf(8)));

                        //a^((p-5)/8)
                        t = this.pow(p.subtract(BigInteger.valueOf(5)).divide(BigInteger.valueOf(8)));

                        //(p-1)/4 = (p+3)/8 + (p-5)/8
                        if (result.mul(t).neg().isOne()) {
                            //4^((p-5)/8)
                            result = createZnElement(BigInteger.valueOf(4))
                                    .pow(p.subtract(BigInteger.valueOf(5)).divide(BigInteger.valueOf(8)));

                            //(4a)^((p-5)/8)
                            result = result.mul(t);

                            //a(4a)^((p-5)/8)
                            result = result.mul(this);

                            //2a(4a)^((p-5)/8)
                            result = result.add(result);

                        }
                    } else { //p = 7 mod 8
                        result = getOneElement(); //TODO this doesn't seem right.
                    }
                }
            } else {
                //if p=1 mod 8 , we need probabilistic algorithm
                if (isSquare())
                    result = (ZpElement) FiniteFieldTools.sqrt(this);
                else
                    result = getZeroElement(); //just to ensure we fail in the next step.
            }

            //check if result is correct.
            if (result.square().equals(this)) {
                return result;
            } else {
                throw new ArithmeticException("Input has to be quadratic residue.");
            }
        }
    }

    @Override
    public ZpElement createZnElement(BigInteger v) {
        return createZnElementUnsafe(v.mod(n));
    }

    @Override
    protected ZpElement createZnElementUnsafe(BigInteger vBetween0andN) {
        return new ZpElement(vBetween0andN);
    }

    @Override
    public ZpElement getElement(BigInteger i) {
        return createZnElement(i);
    }

    @Override
    public ZpElement injectiveValueOf(byte[] bytes) throws IllegalArgumentException {
        return (ZpElement) super.injectiveValueOf(bytes);
    }

    /**
     * Create the Zn element "representant mod modulus"
     * (convenience method)
     *
     * @param representant the integer representant of the element
     * @param modulus      the ring size
     */
    public static ZpElement valueOf(BigInteger representant, BigInteger modulus) {
        return new Zp(modulus).new ZpElement(representant);
    }

    /**
     * Create the Zn element "representant mod modulus"
     * (convenience method)
     *
     * @param representant the integer representant of the element
     * @param modulus      the ring size
     */
    public static ZpElement valueOf(long representant, BigInteger modulus) {
        return valueOf(BigInteger.valueOf(representant), modulus);
    }

    /**
     * Create the Zn element "representant mod modulus"
     * (convenience method)
     *
     * @param representant the integer representant of the element
     * @param modulus      the ring size
     */
    public static ZpElement valueOf(long representant, long modulus) {
        return valueOf(representant, BigInteger.valueOf(modulus));
    }

    @Override
    public ZpElement valueOf(long representant) {
        return valueOf(BigInteger.valueOf(representant));
    }

    @Override
    public ZpElement valueOf(BigInteger representant) {
        return createZnElement(representant);
    }
}
