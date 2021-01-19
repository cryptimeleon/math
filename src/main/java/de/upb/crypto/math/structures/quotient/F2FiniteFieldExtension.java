package de.upb.crypto.math.structures.quotient;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.polynomial.PolynomialRing.Polynomial;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.structures.zn.Zp.ZpElement;

import java.math.BigInteger;

/**
 * Class for storing polynomials defined over the galois field \(\mathbb{F}_2\).
 * <p>
 * Every polynomial has an encapsulated {@link BigInteger} representation
 * that is used for performing efficient multiplication and addition.
 */
public class F2FiniteFieldExtension extends FiniteFieldExtension {

    /**
     * Encodes the irreducible polynomial underlying this field extension as a {@code BigInteger}
     * for efficiency reasons.
     */
    protected BigInteger efficientIrreducible;

    /**
     * The irreducible polynomial over \(\mathbb{Z}_2\) underlying this field extension.
     */
    protected Polynomial irreducible;

    /**
     * The base ring of the polynomial ring, \(\mathbb{Z}_2\) in this case.
     */
    protected static final Zp baseRing = new Zp(BigInteger.valueOf(2));

    /**
     * The polynomial ring over which the irreducible polynomial underlying this field extension is defined.
     */
    protected static final PolynomialRing polyRing = new PolynomialRing(baseRing);

    /**
     * The neutral element of the unit group of this field, also known as the one element.
     */
    protected F2FiniteFieldElement ONE;

    /**
     * The neutral element of the additive subgroup of this field,  also known as the zero element.
     */
    protected F2FiniteFieldElement ZERO;

    public F2FiniteFieldExtension(Representation repr) {
        super(new Zp(BigInteger.valueOf(2)), polyRing.getElement(repr.obj().get("irreducible")));
        irreducible = polyRing.getElement(repr.obj().get("irreducible"));
        efficientIrreducible = repr.obj().get("efficientIrreducible").bigInt().get();
        ONE = new F2FiniteFieldElement(polyRing.new Polynomial(baseRing.createZnElement(BigInteger.ONE)));
        ZERO = new F2FiniteFieldElement(polyRing.new Polynomial(baseRing.createZnElement(BigInteger.ZERO)));
    }

    public F2FiniteFieldExtension(Polynomial irreduciblePoly) {
        super(new Zp(BigInteger.valueOf(2)), irreduciblePoly);
        if (!irreduciblePoly.getStructure().equals(base)) {
            throw new IllegalArgumentException("GF(2^m) Polynomial expected");
        }
        efficientIrreducible = getEfficientPolynomial(irreduciblePoly);
        irreducible = irreduciblePoly;
        ONE = new F2FiniteFieldElement(polyRing.new Polynomial(baseRing.createZnElement(BigInteger.ONE)));
        ZERO = new F2FiniteFieldElement(polyRing.new Polynomial(baseRing.createZnElement(BigInteger.ZERO)));
    }

    /**
     * Encodes the given polynomial as a {@code BigInteger} by converting the coefficients to bits.
     * 
     * @see #getPolynomial(BigInteger) 
     *
     * @param p the polynomial with coefficients in \(\mathbb{Z}_2\)
     * @return the {@code BigInteger} created from interpreting the polynomials coefficients as a bit string
     */
    public static BigInteger getEfficientPolynomial(Polynomial p) {

        RingElement[] coeff = p.getCoefficients();
        String bitString = "";
        for (int i = p.getDegree(); i >= 0; i--) {
            ZpElement zp = (ZpElement) coeff[i];

            if (zp.getInteger().equals(BigInteger.ONE)) {
                bitString = bitString + "1";
            } else if (zp.getInteger().equals(BigInteger.ZERO)) {
                bitString = bitString + "0";
            } else {
                throw new IllegalStateException("Zero or One expected as coefficients!");
            }
        }
        return new BigInteger(bitString, 2);
    }

    /**
     * Converts the {@code BigInteger} polynomial encoding back to a {@code Polynomial}.
     *
     * @see #getEfficientPolynomial(Polynomial)
     * 
     * @param efficientPolynomial the {@code BigInteger} to convert
     * @return the corresponding {@code Polynomial}
     */
    public static Polynomial getPolynomial(BigInteger efficientPolynomial) {
        RingElement[] coefficients = new RingElement[efficientPolynomial.bitLength()];
        for (int i = 0; i < efficientPolynomial.bitLength(); i++) {
            if (efficientPolynomial.testBit(i)) {
                coefficients[i] = baseRing.getOneElement();
            } else {
                coefficients[i] = baseRing.getZeroElement();
            }
        }
        return polyRing.new Polynomial(coefficients);
    }

    @Override
    public FieldElement getOneElement() {
        return ONE;
    }

    @Override
    public FieldElement getZeroElement() {
        return ZERO;
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation repr = new ObjectRepresentation();
        repr.put("irreducible", irreducible.getRepresentation());
        repr.put("efficientIrreducible", new BigIntegerRepresentation(efficientIrreducible));
        return repr;
    }

    @Override
    public F2FiniteFieldElement getElement(Representation repr) {
        return new F2FiniteFieldElement(repr.bigInt().get());
    }

    @Override
    public F2FiniteFieldElement getUniformlyRandomUnit() throws UnsupportedOperationException {
        return new F2FiniteFieldElement(((FiniteFieldElement) super.getUniformlyRandomUnit()).getRepresentative());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((efficientIrreducible == null) ? 0 : efficientIrreducible.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        F2FiniteFieldExtension other = (F2FiniteFieldExtension) obj;
        if (efficientIrreducible == null) {
            if (other.efficientIrreducible != null)
                return false;
        } else if (!efficientIrreducible.equals(other.efficientIrreducible))
            return false;
        return true;
    }

    /**
     * Elements that are polynomials over \(GF(2^m)\).
     * This class provides more efficient reductions and multiplications of polynomials than the standard approach.
     * <p>
     * Every Element stores the coefficients as a {@code BigInteger} as an intermediate format.
     * <p>
     * [1] Julio Lopez and Ricardo Dahab: High-Speed Software Multiplication in \(\mathbb{F}_{2^m}\)
     */
    public class F2FiniteFieldElement extends FiniteFieldElement {

        private BigInteger efficientPolynomial;

        public Polynomial getRepresentative() {
            return (Polynomial) representative;
        }

        public F2FiniteFieldElement(FiniteFieldElement f) {
            this(f.getRepresentative());
        }

        public F2FiniteFieldElement(Polynomial pRepresentative) {
            super(pRepresentative);
            efficientPolynomial = getEfficientPolynomial(this.getRepresentative());
        }

        public F2FiniteFieldElement(BigInteger seed) {
            super(getPolynomial(seed));
            efficientPolynomial = getEfficientPolynomial(this.getRepresentative());
        }

        @Override
        public F2FiniteFieldElement add(Element e) {
            F2FiniteFieldElement f2E = null;
            if (e instanceof F2FiniteFieldElement) {
                f2E = (F2FiniteFieldElement) e;
            } else {
                f2E = new F2FiniteFieldElement((FiniteFieldElement) e);
            }

            BigInteger result = efficientPolynomial.xor(f2E.efficientPolynomial);
            F2FiniteFieldElement resultElement = new F2FiniteFieldElement(result);
            resultElement.reduce();
            return resultElement;
        }

        @Override
        public F2FiniteFieldElement sub(Element e) {
            return add(e);
        }

        @Override
        public F2FiniteFieldElement neg() {
            // + is - in F2
            return new F2FiniteFieldElement(efficientPolynomial);
        }

        /**
         * Implements a more efficient implementation for a multiplication with \(x^d\) which is realized
         * as a d wise bit shift.
         *
         * @param degree the degree of x
         */
        public F2FiniteFieldElement shiftL(int degree) {
            BigInteger result = efficientPolynomial.shiftLeft(degree);
            F2FiniteFieldElement resultElement = new F2FiniteFieldElement(getPolynomial(result));
            return resultElement;
        }

        @Override
        public F2FiniteFieldElement mul(Element e) {
            F2FiniteFieldElement bElement = null;
            if (e instanceof F2FiniteFieldElement) {
                bElement = (F2FiniteFieldElement) e;
            } else {
                bElement = new F2FiniteFieldElement(((FiniteFieldElement) e).getRepresentative());
            }

            byte[] f = efficientIrreducible.toByteArray();

            int s = f.length;

            int w = 8;

            byte[] c = new byte[s];

            for (int i = 0; i < c.length; i++) {
                c[i] = 0;
            }

            int m = irreducible.getDegree();

            int k = (m - 1) - w * (s - 1);

            for (int i = s - 1; i >= 0; i--) {
                for (int j = k; j >= 0; j--) {
                    BigInteger bigC = new BigInteger(c);
                    bigC = bigC.shiftLeft(1);
                    if (efficientPolynomial.testBit(i * w + j)) {
                        bigC = bigC.xor(bElement.efficientPolynomial);
                    }
                    if (bigC.testBit(m)) {
                        bigC = bigC.xor(efficientIrreducible);
                    }
                    c = bigC.toByteArray();
                }
                k = w - 1;
            }
            return new F2FiniteFieldElement(new BigInteger(c));
        }

        /**
         * Implements more efficient multiplication with a prime {@code e}.
         */
        public F2FiniteFieldElement mulPrime(Element e) {
            F2FiniteFieldElement bElement = null;
            if (e instanceof F2FiniteFieldElement) {
                bElement = (F2FiniteFieldElement) e;
            } else {
                bElement = new F2FiniteFieldElement(((FiniteFieldElement) e).getRepresentative());
            }

            byte[] b = bElement.efficientPolynomial.toByteArray();

            byte[] a = efficientPolynomial.toByteArray();

            byte[] f = efficientIrreducible.toByteArray();

            int s = f.length;
            int w = 8;

            byte[] t = new byte[2 * s];

            for (int i = 0; i < t.length; i++) {
                t[i] = 0;
            }
            for (int j = w - 1; j >= 0; j--) {
                for (int i = 0; i <= s - 1; i++) {
                    int A_i = a[i];
                    if (((A_i >> (j - 1)) & 1) == 1) {
                        for (int k = 0; k <= s - 1; k++) {
                            int tki = t[i + k];
                            int bk = b[k];
                            t[i + k] = (byte) (tki ^ bk);
                        }
                    }
                }
                if (j != 0) {
                    BigInteger bigT = new BigInteger(t);
                    bigT = bigT.shiftLeft(1);
                    t = bigT.toByteArray();
                }
            }

            return new F2FiniteFieldElement(new BigInteger(t));

        }

        /**
         * Implementation of the modular reduction proposed in Algorithm 2 of [1].
         */
        protected void reducePrime() {
            efficientPolynomial = getEfficientPolynomial(this.getRepresentative());
            // window size is 8 (we use bytes)
            int w = 8; // a = (A_n-1, ... , A_0)
            byte[] efficientBytes = efficientPolynomial.toByteArray();
            int n = efficientBytes.length;
            // f = (F_s-1, ... , F_0)
            byte[] irreducibleBytes = efficientIrreducible.toByteArray();
            int s = irreducibleBytes.length;
            // f(x) = x^m + g(x) (where g(x) has a degree < m-w)
            int m = irreducible.getDegree();

            // Step 1.
            for (int i = n - 1; i >= s; i--) {
                // d = i * w - m
                int d = i * w - m;
                // t = A_i(x) * x^d *f(x)
                Polynomial t = calculateT(d, efficientBytes[i]);

                byte[] byteT = getEfficientPolynomial(t).toByteArray();

                for (int j = i; j >= i - s; j--) {
                    // A_j = A_j \oplus T_j
                    int aj = efficientBytes[j];
                    int tj = byteT[j];
                    efficientBytes[j] = (byte) (aj ^ tj);
                }
            }
            // rebuild a so that we can access it
            efficientPolynomial = new BigInteger(efficientBytes);

            // Step 2.
            ZpElement[] coeff = new ZpElement[s * w - m];

            for (int j = 0; j < coeff.length; j++) {

                if (efficientPolynomial.testBit(m + j)) {
                    coeff[j] = baseRing.createZnElement(BigInteger.ONE);
                } else {
                    coeff[j] = baseRing.createZnElement(BigInteger.ZERO);
                }
            }
            Polynomial polyT = polyRing.new Polynomial(coeff).mul(irreducible);

            byte[] byteT = getEfficientPolynomial(polyT).toByteArray();

            // Step 3.
            for (int j = s - 1; j >= 0; j--) {
                int aj = efficientBytes[j];
                int tj = byteT[j];
                byte xor = (byte) (aj ^ tj);
                efficientBytes[j] = xor;
            }
            efficientPolynomial = new BigInteger(efficientBytes);
            representative = getPolynomial(efficientPolynomial);

        }

        private Polynomial calculateT(int d, byte i) {
            F2FiniteFieldElement A_i = new F2FiniteFieldElement(getPolynomial(new BigInteger(new byte[]{i})));
            F2FiniteFieldElement shiftedA_i = (F2FiniteFieldElement) A_i.shiftL(d);
            A_i.representative = getPolynomial(shiftedA_i.efficientPolynomial);
            Polynomial pol = A_i.getRepresentative().mul(irreducible);
            return pol;

        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((efficientPolynomial == null) ? 0 : efficientPolynomial.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (getClass() != obj.getClass())
                return false;
            F2FiniteFieldElement other = (F2FiniteFieldElement) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (efficientPolynomial == null) {
                if (other.efficientPolynomial != null)
                    return false;
            } else if (!efficientPolynomial.equals(other.efficientPolynomial))
                return false;
            return true;
        }

        public String toString() {
            return representative.toString();
        }

        private F2FiniteFieldExtension getOuterType() {
            return F2FiniteFieldExtension.this;
        }

        @Override
        public Representation getRepresentation() {
            return new BigIntegerRepresentation(efficientPolynomial);
        }
    }

}
