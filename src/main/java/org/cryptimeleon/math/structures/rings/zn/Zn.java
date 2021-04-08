package org.cryptimeleon.math.structures.rings.zn;

import org.cryptimeleon.math.expressions.exponent.ExponentConstantExpr;
import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.serialization.BigIntegerRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.Element;
import org.cryptimeleon.math.structures.rings.Ring;
import org.cryptimeleon.math.structures.rings.RingElement;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * The ring of integers modulo n.
 */
public class Zn implements Ring {
    /**
     * The neutral element of this ring's unit group (the one element).
     */
    protected final ZnElement ONE;

    /**
     * The neutral element of this ring's additive group (the zero element).
     */
    protected final ZnElement ZERO;

    /**
     * The modulus.
     */
    protected final BigInteger n;

    /**
     * Whether the modulus {@code n} is prime.
     */
    protected Boolean nIsPrime = null;

    /**
     * Maximum value (over all elements elem) of {@code elem.getInteger().toByteArray().length;}.
     * The value is exactly the number of bytes needed to represent n.
     */
    protected final int maxByteLength;

    /**
     * Constructs the ring.
     *
     * @param n number of elements in the ring
     */
    public Zn(BigInteger n) {
        if (n.signum() <= 0)
            throw new IllegalArgumentException("n must be positive");

        this.n = n;
        this.ONE = createZnElement(BigInteger.ONE);
        this.ZERO = createZnElement(BigInteger.ZERO);
        // All elements' integers are smaller than n; hence, their byte representation is not larger.
        maxByteLength = n.toByteArray().length;
    }

    /**
     * Constructs the ring from a {@code Representation}.
     */
    public Zn(Representation repr) {
        this(((BigIntegerRepresentation) repr).get());
    }

    @Override
    public BigInteger size() {
        return n;
    }

    @Override
    public boolean hasPrimeSize() throws UnsupportedOperationException {
        if (nIsPrime == null)
            nIsPrime = n.isProbablePrime(80);
        return nIsPrime;
    }

    @Override
    public BigInteger sizeUnitGroup() {
        throw new UnsupportedOperationException(); // the euler totient function is potentially hard to evaluate
    }

    @Override
    public ZnElement getZeroElement() {
        return ZERO;
    }

    @Override
    public ZnElement getOneElement() {
        return ONE;
    }

    @Override
    public ZnElement getUniformlyRandomElement() throws UnsupportedOperationException {
        return createZnElement(RandomGenerator.getRandomNumber(n));
    }

    @Override
    public ZnElement getUniformlyRandomUnit() throws UnsupportedOperationException {
        return (ZnElement) Ring.super.getUniformlyRandomUnit();
    }

    @Override
    public ZnElement getUniformlyRandomNonzeroElement() {
        return createZnElement(RandomGenerator.getRandomNonZeroNumber(n));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        return obj instanceof Zn && n.equals(((Zn) obj).n);
    }

    @Override
    public int hashCode() {
        return n.hashCode();
    }

    /**
     * Returns the length of the representation of the largest element in this ring in terms of number of bytes.
     */
    public final int upperBoundForUniqueRepresentation() {
        // Every Element will be smaller than n, hence n should have the largest representation
        return this.n.toByteArray().length;
    }

    /**
     * An equivalence class of integers with the same remainder when divided by n.
     */
    public class ZnElement implements RingElement, UniqueByteRepresentable {
        /**
         * The unique integer {@code v} such that {@code 0 <= v < n} and {@code v} projects
         * to the represented element in {@code Zn}.
         */
        protected final BigInteger v;

        /**
         * Construct a new {@code ZnElement} initialized as {@code [v] mod n} (must reduce {@code v} before calling!).
         */
        protected ZnElement(BigInteger v) {
            this.v = v;
            if (v.compareTo(n) >= 0 || v.signum() < 0)
                throw new RuntimeException("The given integer is not in Zn");
        }

        /**
         * Create the zero element.
         */
        protected ZnElement() {
            this.v = BigInteger.ZERO;
        }

        @Override
        public Zn getStructure() {
            return Zn.this;
        }

        @Override
        public ZnElement add(Element e) {
            checkSameModulus(e);
            BigInteger result = v.add(((ZnElement) e).v);
            if (result.compareTo(n) >= 0)
                result = result.subtract(n);
            return createZnElementUnsafe(result);
        }

        @Override
        public ZnElement neg() {
            return v.equals(BigInteger.ZERO) ? this : createZnElementUnsafe(n.subtract(v));
        }
    
        @Override
        public ZnElement sub(Element e) {
            checkSameModulus(e);
            BigInteger result = v.subtract(((ZnElement)e).v);
            if (result.signum() == -1)
                result = result.add(n);
            return createZnElementUnsafe(result);
        }

        @Override
        public ZnElement mul(Element e) {
            checkSameModulus(e);
            return createZnElementUnsafe(v.multiply(((ZnElement) e).v).mod(n));
        }

        @Override
        public ZnElement mul(BigInteger k) {
            return createZnElementUnsafe(v.multiply(k).mod(n));
        }

        @Override
        public ZnElement mul(long k) {
            return mul(BigInteger.valueOf(k));
        }

        @Override
        public ZnElement pow(BigInteger k) {
            return createZnElementUnsafe(v.modPow(k, n));
        }

        @Override
        public ZnElement pow(long k) {
            return pow(BigInteger.valueOf(k));
        }

        @Override
        public ZnElement inv() throws UnsupportedOperationException {
            try {
                return createZnElementUnsafe(v.modInverse(n));
            } catch (ArithmeticException e) {
                throw new UnsupportedOperationException("This element (" + v + ") is not invertible modulo " + n);
            }
        }

        @Override
        public boolean divides(RingElement e) throws UnsupportedOperationException {
            // this divides e over Zn iff gcd(this, n) divides e over the integers (http://shoup.net/ntb/ntb-v2.pdf, Theorem 2.5 (i))
            return v.gcd(n).remainder(((ZnElement) e).v).equals(BigInteger.ZERO);
        }

        @Override
        public ZnElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigInteger getRank() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        protected void checkSameModulus(Element e) {
            if (!(e instanceof ZnElement) || !getStructure().equals(e.getStructure()))
                throw new IllegalArgumentException("Cannot compute operations between "+getStructure()+" and "+e.getStructure());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            // (RH): written using instanceof to make ZnElement and ZpElement equals compatible
            // if we were to overwrite equals in Zp this would need to be reconsidered
            if (!(obj instanceof ZnElement))
                return false;
            ZnElement e = (ZnElement) obj;
            return Objects.equals(getStructure(), e.getStructure())
                    && Objects.equals(v, e.v);
        }

        @Override
        public int hashCode() {
            return v.hashCode();
        }

        @Override
        public Representation getRepresentation() {
            return new BigIntegerRepresentation(v);
        }

        /**
         * Returns the unique integer representative for this element in \([0,n)\).
         */
        public BigInteger getInteger() {
            return v;
        }

        @Override
        public String toString() {
            return v.toString();
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            BigInteger reduced = this.v.mod(Zn.this.n);
            byte[] tmp = reduced.toByteArray();

            byte[] result = new byte[maxByteLength];//implicitly set to 0

            //Produce array of fixed length with leading zeros.
            //Because BigInteger is Big-Endian and v >= 0, leading zeros do not change the value
            System.arraycopy(tmp, 0, result, maxByteLength - tmp.length, tmp.length);

            accumulator.append(result);
            return accumulator;
        }

        public ExponentConstantExpr asExponentExpression() {
            return new ExponentConstantExpr(this);
        }

        @Override
        public BigInteger asInteger() throws UnsupportedOperationException {
            return v;
        }
    }

    @Override
    public Representation getRepresentation() {
        return new BigIntegerRepresentation(n);
    }

    @Override
    public ZnElement restoreElement(Representation repr) {
        return createZnElement(((BigIntegerRepresentation) repr).get());
    }

    /**
     * Create the Zn element "representative mod modulus" (convenience method)
     *
     * @param representative the integer representative of the element
     * @param modulus        the ring size
     */
    public static ZnElement valueOf(BigInteger representative, BigInteger modulus) {
        return new Zn(modulus).valueOf(representative);
    }

    /**
     * Create the Zn element "representative mod modulus"
     *
     * @param representative the integer representative of the element
     * @param modulus      the ring size
     */
    public static ZnElement valueOf(long representative, BigInteger modulus) {
        return valueOf(BigInteger.valueOf(representative), modulus);
    }

    /**
     * Create the element "representative mod modulus"
     *
     * @param representative the integer representative of the element
     * @param modulus      the ring size
     */
    public static ZnElement valueOf(long representative, long modulus) {
        return valueOf(representative, BigInteger.valueOf(modulus));
    }

    /**
     * Creates the corresponding element mod n.
     *
     * @param representative the integer representative of the element
     */
    public ZnElement valueOf(long representative) {
        return valueOf(BigInteger.valueOf(representative));
    }

    /**
     * Creates the corresponding element mod n.
     *
     * @param representative the integer representative of the element
     */
    public ZnElement valueOf(BigInteger representative) {
        return createZnElement(representative);
    }

    /**
     * For all {@code k < floor((n.bitLength()-1)/8)}, this is an injective map {@code byte^k -> Zn}.
     * <p>
     * Note that there may be collisions between {@code injectiveValueOf(bytes1)} and {@code injectiveValueOf(bytes2)}
     * if {@code bytes1.length != bytes2.length}.
     *
     * @param bytes the bytes to map injectively into Zn.
     * @return the resulting Zn element
     * @throws IllegalArgumentException if the byte array is too long
     */
    public ZnElement injectiveValueOf(byte[] bytes) throws IllegalArgumentException {
        // any integer represented by a BITstring of length at most n.bitLength()-1 is is strictly smaller than n.
        // That's what we can handle.
        if (bytes.length > (n.bitLength() - 1) / 8)
            throw new IllegalArgumentException("Too many bytes to map injectively to Zn " +
                    "(allowed are byte arrays of length " + (n.bitLength() - 1) / 8 + ")");

        // Normalize to make the most significant byte 0 (includes the sign bit).
        // This ensures that the resulting BigInteger number is nonnegative.
        byte[] normalized = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, normalized, 1, bytes.length);

        BigInteger result = new BigInteger(normalized);
        if (result.compareTo(n) > 0 || result.signum() < 0)
            throw new RuntimeException("This should not happen");
        return createZnElement(result);
    }

    /**
     * Interprets given bytes as an integer and projects that number into Zn. For short byte[], this map is injective.
     */
    public ZnElement valueOf(byte[] bytes) {
        return createZnElement(new BigInteger(1, bytes));
    }

    @Override
    public BigInteger getCharacteristic() {
        return this.size();
    }

    /**
     * Creates an element from a {@code BigInteger} (formally, the projection of \(v\) from \(\mathbb{Z}\)
     * to \(\mathbb{Z}_n\)).
     * <p>
     * Implementation detail: This factory method allows the subclass {@link Zp} to use its own kind of elements
     * while reusing the {@code Zn} implementation.
     */
    public ZnElement createZnElement(BigInteger v) {
        return createZnElementUnsafe(v.mod(n));
    }

    /**
     * Instantiates a {@code ZnElement} without checking that the given representative is within the proper range.
     * @param vBetween0andN the representative of the element to instantiate.
     *                      Must be between 0 (inclusive) and n (exclusive).
     */
    protected ZnElement createZnElementUnsafe(BigInteger vBetween0andN) {
        return new ZnElement(vBetween0andN);
    }

    @Override
    public String toString() {
        return "Z_" + n.toString();
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.of(maxByteLength);
    }

    @Override
    public ZnElement getElement(BigInteger i) {
        return createZnElement(i);
    }

    @Override
    public boolean isCommutative() {
        return true;
    }
}
