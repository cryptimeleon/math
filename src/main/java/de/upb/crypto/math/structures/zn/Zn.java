package de.upb.crypto.math.structures.zn;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.swante.util.MyGlobals;
import de.upb.crypto.math.swante.util.MyUtil;

import java.math.BigInteger;
import java.util.Optional;

/**
 * The ring of integers modulo n
 */
public class Zn implements Ring {
    protected final ZnElement ONE;
    protected final ZnElement ZERO;
    protected final BigInteger n;

    /**
     * Maximum value (over all elements elem) of elem.getInteger().toByteArray().length;
     * The value is exactly the number of bytes needed to represent n.
     */
    protected final int maxByteLength;

    /**
     * Constructs the ring
     *
     * @param n number of elements in the ring
     */
    public Zn(BigInteger n) {
        if (n.signum() <= 0)
            throw new IllegalArgumentException("n must be positive");

        this.n = n;
        this.ONE = createZnElement(BigInteger.ONE);
        this.ZERO = createZnElement(BigInteger.ZERO);
        maxByteLength = n.toByteArray().length; //all elements' integers are smaller than n, hence their byte representation is not larger.
    }

    /**
     * Constructs the ring from a Representation.
     */
    public Zn(Representation repr) {
        this(((BigIntegerRepresentation) repr).get());
    }

    @Override
    public BigInteger size() {
        return n;
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
//        return createZnElement(RandomGeneratorSupplier.getRnd().getRandomElement(n));
        return createZnElement(MyUtil.randBig(n)); // TODO: switch back to old implementation
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Zn && n.equals(((Zn) obj).n);
    }

    @Override
    public int hashCode() {
        return n.hashCode();
    }

    public final int upperBoundForUniqueRepresentation() {
        // Every Element will be smaller than n, hence n should have the largest representation
        return this.n.toByteArray().length;
    }

    /**
     * An equivalence class of integers with the same remainder when divided by n
     */
    public class ZnElement implements RingElement, UniqueByteRepresentable {
        /**
         * The unique element v in Z such that 0 <= v < n and v projects to the represented element in Zn
         */
        protected final BigInteger v;

        /**
         * Construct a new ZnElement initialized as [v] mod n (no need to reduce v before calling)
         */
        protected ZnElement(BigInteger v) {
//            if (v.compareTo(n) < 0 && v.signum() >= 0) {
//                this.v = v;
//            } else {
                this.v = v.mod(n);
//            }
        }

        /**
         * Create the zero element
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
            return createZnElement(v.add(((ZnElement) e).v));
        }

        @Override
        public ZnElement neg() {
            return createZnElement(v.negate());
        }
    
        @Override
        public ZnElement sub(Element e) {
            return createZnElement(v.subtract(((ZnElement)e).v));
        }

        @Override
        public ZnElement mul(Element e) {
            return createZnElement(v.multiply(((ZnElement) e).v));
        }

        @Override
        public ZnElement inv() throws UnsupportedOperationException {
            try {
                return createZnElement(v.modInverse(n));
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

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ZnElement))
                return false;

            ZnElement e = (ZnElement) obj;
            return this.getStructure().equals(e.getStructure()) && v.equals(e.v);
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
         * Returns the unique integer representative for this element in [0,n).
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

    }

    @Override
    public Representation getRepresentation() {
        return new BigIntegerRepresentation(n);
    }

    @Override
    public ZnElement getElement(Representation repr) {
        return createZnElement(((BigIntegerRepresentation) repr).get());
    }

    /**
     * Create the Zn element "representant mod modulus" (convenience method)
     *
     * @param representant the integer representant of the element
     * @param modulus      the ring size
     */
    public static ZnElement valueOf(BigInteger representant, BigInteger modulus) {
        return new Zn(modulus).new ZnElement(representant);
    }

    /**
     * Create the Zn element "representant mod modulus" (convenience method)
     *
     * @param representant the integer representant of the element
     * @param modulus      the ring size
     */
    public static ZnElement valueOf(long representant, BigInteger modulus) {
        return valueOf(BigInteger.valueOf(representant), modulus);
    }

    /**
     * Create the Zn element "representant mod modulus" (convenience method)
     *
     * @param representant the integer representant of the element
     * @param modulus      the ring size
     */
    public static ZnElement valueOf(long representant, long modulus) {
        return valueOf(representant, BigInteger.valueOf(modulus));
    }

    /**
     * Creates the corresponding ZnElement
     *
     * @param representant
     * @return
     */
    public ZnElement valueOf(long representant) {
        return valueOf(BigInteger.valueOf(representant));
    }

    /**
     * Creates the corresponding ZnElement
     *
     * @param representant
     * @return
     */
    public ZnElement valueOf(BigInteger representant) {
        return createZnElement(representant);
    }

    /**
     * For all k < floor((n.bitLength()-1)/8),
     * this is an injective map byte^k -> Zn.
     * Note that there may be collisions between injectiveValueOf(bytes1) and injectiveValueOf(bytes2)
     * if bytes1.length != bytes2.length.
     *
     * @param bytes the bytes to map injectively into Zn.
     * @return a value of Zn
     * @throws IllegalArgumentException if the byte array is too long.
     */
    public ZnElement injectiveValueOf(byte[] bytes) throws IllegalArgumentException {
        if (bytes.length > (n.bitLength() - 1) / 8) //any integer represented by a BITstring of length at most n.bitLength()-1 is is strictly smaller than n. That's what we can handle.
            throw new IllegalArgumentException("Too many bytes to map injectively to Zn (allowed are byte arrays of length " + (n.bitLength() - 1) / 8 + ")");
        //Normalize to make the most significant byte 0 (includes the sign bit).
        //This ensures that the resulting BigInteger number is nonnegative.
        byte[] normalized = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, normalized, 1, bytes.length);

        BigInteger result = new BigInteger(normalized);
        if (result.compareTo(n) > 0 || result.signum() < 0)
            throw new RuntimeException("This should not happen");
        return createZnElement(result);
    }

    @Override
    public BigInteger getCharacteristic() {
        return this.size();
    }

    /**
     * Creates an element from a BigInteger (formally, the projection of v from Z to Zn). (Implementation detail: This factory method allows the subclass Zp to use its own kind of Elements while reusing the Zn implementation)
     */
    public ZnElement createZnElement(BigInteger v) {
        return new ZnElement(v);
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
