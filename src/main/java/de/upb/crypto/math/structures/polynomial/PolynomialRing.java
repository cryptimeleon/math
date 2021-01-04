package de.upb.crypto.math.structures.polynomial;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.structures.zn.Zp.ZpElement;

import java.math.BigInteger;
import java.util.*;

/**
 * A polynomial ring over a given base commutative {@link Ring}.
 */
public class PolynomialRing implements Ring {
    /**
     * The ring over which the polynomials are defined.
     */
    protected Ring baseRing;

    /**
     * Creates the polynomial ring over the given base.
     */
    public PolynomialRing(Ring base) {
        this.baseRing = base;
        if (!baseRing.isCommutative())
            throw new IllegalArgumentException("base ring of polynomial ring must be commutative");
    }

    public PolynomialRing(Representation arg) {
        RepresentableRepresentation repr = (RepresentableRepresentation) arg;
        baseRing = (Ring) repr.recreateRepresentable();
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return null; // infinite
    }

    @Override
    public Representation getRepresentation() {
        return new RepresentableRepresentation(baseRing);
    }

    @Override
    public BigInteger sizeUnitGroup() throws UnsupportedOperationException {
        return baseRing.sizeUnitGroup(); // units are all in the base ring
    }

    @Override
    public Polynomial getZeroElement() {
        return new Polynomial(baseRing.getZeroElement());
    }

    @Override
    public Polynomial getOneElement() {
        return new Polynomial(baseRing.getOneElement());
    }

    @Override
    public Polynomial getElement(BigInteger i) {
        return new Polynomial(baseRing.getElement(i));
    }

    /**
     * Returns the base ring.
     */
    public Ring getBaseRing() {
        return baseRing;
    }

    @Override
    public Polynomial getElement(Representation arg) {
        ListRepresentation repr = (ListRepresentation) arg;
        RingElement[] coefficients = new RingElement[repr.size()];
        for (int i = 0; i < repr.size(); i++)
            coefficients[i] = baseRing.getElement(repr.get(i));

        return new Polynomial(coefficients);
    }

    @Override
    public Polynomial getUniformlyRandomElement() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("infinite ring");
    }

    @Override
    public Polynomial getUniformlyRandomUnit() throws UnsupportedOperationException {
        return new Polynomial(baseRing.getUniformlyRandomUnit());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PolynomialRing && ((PolynomialRing) obj).getBaseRing().equals(baseRing);
    }

    @Override
    public int hashCode() {
        return 1 + baseRing.hashCode();
    }

    /**
     * An element of {@link PolynomialRing}.
     */
    public class Polynomial implements RingElement {
        /**
         * Coefficients of this polynomial, i.e. \(\text{this} = \sum coefficients[i] * x^i\).
         * <p>
         * All entries with index {@code i > degree} may be null.
         * This array always contains at least one element.
         */
        protected RingElement[] coefficients;

        /**
         * The degree of this polynomial.
         */
        protected int degree;

        /**
         * Creates an empty polynomial.
         * <p>
         * Set the {@code this.coefficients} array yourself, then call {@code computeDegree()}).
         * Use {@code}createPolyInternal() as a shortcut for that.
         * <p>
         * This omits the copying process and {@code ensureArrayInvariants() } call of the public constructors.
         */
        private Polynomial() {

        }

        /**
         * Uses the private constructor to create a polynomial from the given coefficients.
         */
        private Polynomial createPolyInternal(RingElement[] coefficients) {
            if (coefficients.length == 0)
                throw new IllegalArgumentException("Empty coefficient list for polynomial");
            Polynomial result = new Polynomial();
            result.coefficients = coefficients;
            result.computeDegree();
            return result;
        }

        public RingElement[] getCoefficients() {
            return coefficients;
        }

        /**
         * Creates a polynomial using a bit string as the coefficients in descending
         * order, i.e. {@code new Polynomial(10)} returns \(0 + 1 \cdot X + 0 \cdot X^2 + 1 \cdot X^3\).
         * <p>
         * This constructor should only be used if the underlying ring is \(\mathbb{Z}_2\).
         *
         * @param seed bit string representing coefficients of new polynomial in ascending order
         */
        public Polynomial(Seed seed) {
            if (!baseRing.equals(new Zp(BigInteger.valueOf(2)))) {
                throw new IllegalArgumentException(
                        "This constructor should only be used if the underlying ring is Zp2");
            }

            if (seed.getByteLength() == 0) {
                seed = new Seed(new byte[]{0});
            }

            rebuildPolynomial(seed);

        }

        /**
         * Creates a polynomial with the given coefficients.
         * <p>
         * For example, {@code new Polynomial(1,2,3)} returns \(1 + 2 \cdot X + 3 \cdot X^2\).
         *
         * @param coefficients coefficients for the new polynomial, in order from smallest to largest exponent
         */
        public Polynomial(RingElement... coefficients) {
            if (coefficients.length == 0)
                coefficients = new RingElement[]{baseRing.getZeroElement()};
            this.coefficients = Arrays.copyOf(coefficients, coefficients.length); // copy to make sure this Polynomial
            // object is immutable
            computeDegree();
            ensureArrayNonNull();
        }

        /**
         * Creates a polynomial with the given coefficients.
         * <p>
         * For example, {@code new Polynomial([1,2,3])} returns \(1 + 2 \cdot X + 3 \cdot X^2\).
         *
         * @param coefficients coefficients for the new polynomial, in order from smallest to largest exponents
         */
        public Polynomial(List<RingElement> coefficients) {
            if (coefficients.size() == 0)
                coefficients = Collections.singletonList(baseRing.getZeroElement());
            this.coefficients = coefficients.toArray(new RingElement[coefficients.size()]); // copy to make sure this
            // Polynomial object is
            // immutable
            computeDegree();
            ensureArrayNonNull();
        }

        /**
         * Creates the polynomial \(a \cdot X^i\).
         */
        public Polynomial(int i, RingElement a) {
            if (i < 0)
                throw new IllegalArgumentException();

            if (a.isZero())
                degree = 0;
            else
                degree = i;

            // Create coefficient array
            this.coefficients = new RingElement[degree + 1];
            this.coefficients[degree] = a;

            // Fill array with zeros at all other positions
            for (int j = 0; j < degree; j++)
                this.coefficients[j] = baseRing.getZeroElement();
        }

        /**
         * Creates the polynomial \(X^i\).
         */
        public Polynomial(int i) {
            this(i, baseRing.getOneElement());
        }

        /**
         * Sets the degree instance variable according to the coefficients.
         * <p>
         * This is done once when creating the polynomial.
         */
        protected void computeDegree() {
            degree = coefficients.length - 1;
            while (degree > 0 && (coefficients[degree] == null || coefficients[degree].isZero()))
                degree--;
        }

        /**
         * Makes sure that the coefficients array does not contain null values at indices {@code i <= degree}.
         *
         * @throws IllegalArgumentException if any coefficient of the polynomial is null
         */
        protected void ensureArrayNonNull() {
            for (int i = 0; i <= degree; i++)
                if (coefficients[i] == null)
                    throw new IllegalArgumentException("There are null values in this polynomial.");
        }

        @Override
        public Representation getRepresentation() {
            ListRepresentation result = new ListRepresentation();
            for (RingElement elem : coefficients)
                result.put(elem.getRepresentation());

            return result;
        }

        @Override
        public PolynomialRing getStructure() {
            return PolynomialRing.this;
        }

        /**
         * Return the value of the polynomial evaluated at {@code x}.
         * <p>
         * Result is computes using the Horner scheme.
         *
         * @param x position to evaluate
         * @return result of evaluation at position {@code x}
         */
        public RingElement evaluate(Element x) {
            if (!(x.getStructure().equals(baseRing))) {
                throw new UnsupportedOperationException(
                        "Evaluate only supports elements from the base ring as argument");
            }
            RingElement result = baseRing.getZeroElement();

            for (int i = this.coefficients.length; i > 0; i--) {
                result = this.coefficients[i - 1].add(result.mul(x));
            }

            return result;
        }

        /**
         * Creates a new polynomial by adding the given polynomial to this.
         * <p>
         * Addition is done by adding the coefficients together.
         * Degree of the resulting polynomial is the maximum degree of the two involved polynomials.
         *
         * @param e the addend
         * @return the resulting polynomial
         */
        @Override
        public Polynomial add(Element e) {
            Polynomial[] polys = new Polynomial[]{this, (Polynomial) e};
            Arrays.sort(polys, Comparator.comparing(p -> p.degree));

            // At this point, polys[0] is the polynomial with smaller degree
            RingElement[] result = new RingElement[polys[1].degree + 1];

            // Put coefficients
            for (int i = 0; i <= polys[0].degree; i++) // the sums
                result[i] = polys[0].coefficients[i].add(polys[1].coefficients[i]);
            for (int i = polys[0].degree + 1; i <= polys[1].degree; i++) // the missing coefficients of poly[1]
                result[i] = polys[1].coefficients[i];

            return createPolyInternal(result);
        }

        /**
         * Negates the polynomial by negating each coefficient.
         * @return the negated polynomial
         */
        @Override
        public Polynomial neg() {
            RingElement[] result = new RingElement[degree + 1];
            for (int i = 0; i <= degree; i++)
                result[i] = coefficients[i].neg();
            return createPolyInternal(result);
        }

        /**
         * Computes the inner product of the coefficient vectors.
         */
        public ZpElement scalarProduct(Polynomial e) {
            if (!(baseRing instanceof Zp) || !(e.getStructure().baseRing instanceof Zp)) {
                throw new UnsupportedOperationException("Only supported for ZpElements");
            }
            ZpElement result = (ZpElement) baseRing.getZeroElement();
            int minLength = e.coefficients.length < coefficients.length ? e.coefficients.length : coefficients.length;
            for (int i = 0; i < minLength; i++) {
                result = result.add(coefficients[i].mul(e.coefficients[i]));
            }
            return result;
        }

        /**
         * Creates a new polynomial by subtracting the given polynomial from this.
         * <p>
         * Subtraction is done by subtracting the coefficients of the given polynomial from this.
         * Degree of the resulting polynomial is the maximum degree of the two involved polynomials.
         *
         * @param e the polynomial to subtract
         * @return the resulting polynomial
         */
        @Override
        public Polynomial sub(Element e) {

            Polynomial[] polys = new Polynomial[]{this, (Polynomial) e};
            Arrays.sort(polys, Comparator.comparing(p -> p.degree));

            // At this point, polys[0] is the polynomial with smaller degree
            RingElement[] result = new RingElement[polys[1].degree + 1];

            // Put coefficients
            for (int i = 0; i <= polys[0].degree; i++) // the sums
                result[i] = this.coefficients[i].sub(((Polynomial) e).coefficients[i]);
            for (int i = polys[0].degree + 1; i <= polys[1].degree; i++) // the missing coefficients of poly[1]
                result[i] = polys[1] == e ? polys[1].coefficients[i].neg() : polys[1].coefficients[i];

            return createPolyInternal(result);

        }

        /**
         * Multiplies the given polynomial with this using standard polynomial multiplication.
         *
         * @param e the factor
         * @return the result of the multiplication
         */
        @Override
        public Polynomial mul(Element e) {
            Polynomial a = (Polynomial) e, b = this;

            // At this point, polys[0] is the polynomial with smaller degree.
            // Initialize result coefficients
            RingElement[] result = new RingElement[a.degree + b.degree + 1];
            RingElement zero = baseRing.getZeroElement();
            for (int i = 0; i < result.length; i++)
                result[i] = zero;

            // Multiply a and b
            for (int i = 0; i <= a.degree; i++)
                for (int j = 0; j <= b.degree; j++) {
                    result[i + j] = result[i + j].add(a.coefficients[i].mul(b.coefficients[j]));
                }
            return createPolyInternal(result);
        }

        /**
         * Multiplies each coefficient with the given integer.
         *
         * @param k the factor
         * @return the resulting polynomial
         */
        @Override
        public Polynomial mul(BigInteger k) {
            RingElement[] result = new RingElement[this.degree+1];
            for (int i=0;i<result.length;i++) {
                result[i] = this.coefficients[i].mul(k);
            }

            return createPolyInternal(result);
        }

        /**
         * Inverts the constant polynomial by inverting the zero degree coefficient in its corresponding ring.
         * <p>
         * Does not work for polynomial of higher degree.
         * @return the inverted polynomial
         * @throws UnsupportedOperationException if the degree of the polynomial to invert is greater than zero
         */
        @Override
        public Polynomial inv() throws UnsupportedOperationException {
            if (degree > 0)
                throw new UnsupportedOperationException("Cannot invert non-zero-degree polynomials");
            return new Polynomial(coefficients[0].inv());
        }

        /**
         * Multiplies each coefficient of this polynomial with the given base ring element.
         */
        public Polynomial scalarMul(Element elem) {
            RingElement[] result = new RingElement[degree + 1];
            for (int i = 0; i <= degree; i++)
                result[i] = coefficients[i].mul(elem);

            return createPolyInternal(result);
        }

        /**
         * Normalizes this polynomial by factoring out the coefficient corresponding to the highest exponent.
         * <p>
         * Results in a polynomial with coefficient one for the highest (leading) exponent.
         *
         * @throws UnsupportedOperationException if the highest coefficient is not a unit, i.e. cannot be factored out
         */
        public Polynomial normalize() throws UnsupportedOperationException {
            return scalarMul(coefficients[degree].inv());
        }

        /**
         * Checks whether this polynomial can be divided without remainder by the given polynomial.
         *
         * @param e the divisor
         * @return true if the remainder is zero, else false
         * @throws UnsupportedOperationException if the divisors leading coefficient cannot be inverted
         */
        @Override
        public boolean divides(RingElement e) throws UnsupportedOperationException {
            if (e.isZero())
                return true;
            return e.divideWithRemainder(this)[1].isZero();
        }

        /**
         * Performs polynomial division with remainder.
         *
         * @param e the divisor
         * @return a {@code Polynomial} array containing the quotient and remainder, in that order
         * @throws UnsupportedOperationException if the divisors leading coefficient cannot be inverted
         */
        @Override
        public Polynomial[] divideWithRemainder(RingElement e) throws UnsupportedOperationException{
            Polynomial divisor = (Polynomial) e, dividend = this;
            Polynomial quotient = getZeroElement(), remainder;

            // note that we require invertibility of coefficients here
            RingElement invOfLeadingCoeff = divisor.coefficients[divisor.degree].inv();

            while (!dividend.isZero() && (dividend.getDegree() >= divisor.getDegree())) {
                // want to find polynomial temp = t*x^k that eliminates the leading
                // coefficient of dividend
                int k = dividend.getDegree() - divisor.getDegree();
                RingElement t = dividend.coefficients[dividend.degree].mul(invOfLeadingCoeff);
                Polynomial temp = dividend.getStructure().new Polynomial(k, t);

                /* add the element to the quotient */
                quotient = quotient.add(temp);

                /* modify the dividend */
                dividend = dividend.sub(divisor.mul(temp));
            }
            remainder = dividend;

            return new Polynomial[]{quotient, remainder};
        }

        @Override
        public BigInteger getRank() throws UnsupportedOperationException {
            return BigInteger.valueOf(getDegree());
        }

        public int getDegree() {
            return degree;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Polynomial obj = (Polynomial) o;
            if (obj.degree != degree)
                return false;
            for (int i = 0; i <= degree; i++) {
                if (!obj.coefficients[i].equals(coefficients[i]))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (int i = 0; i <= degree; i++)
                result += coefficients[i].hashCode() * i;
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");

            for (int i = degree; i >= 0; i--) {
                if (i < degree)
                    builder.append("+");
                builder.append(coefficients[i].toString());

                if (i > 0) {
                    builder.append("x");
                    if (i > 1)
                        builder.append("^" + i);
                }
            }

            return builder.append("]").toString();
        }

        /**
         * Initialize the  polynomial using a bit string as the coefficients in descending
         * order, i.e. {@code new Polynomial(10)} returns \(0 + 1 \cdot X + 0 \cdot X^2 + 1 \cdot X^3\).
         * <p>
         * This constructor should only be used if the underlying ring is \(\mathbb{Z}_2\).
         *
         * @param seed the bit string representing the new coefficients
         */
        public void rebuildPolynomial(Seed seed) {
            Zp zp = (Zp) baseRing;
            this.coefficients = new RingElement[seed.getBitLength()];

            for (int i = 0; i < seed.getBitLength(); i++) {
                if (seed.getBitAt(i) == 1) {
                    this.coefficients[i] = zp.createZnElement(BigInteger.ONE);
                } else {
                    this.coefficients[i] = zp.createZnElement(BigInteger.ZERO);
                }

            }
            computeDegree();
            ensureArrayNonNull();

        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            for (RingElement elem : getCoefficients()) {
                accumulator.escapeAndAppend(elem.getUniqueByteRepresentation());
                accumulator.appendSeperator();
            }
            return accumulator;
        }
    }

    @Override
    public BigInteger getCharacteristic() {
        return this.getBaseRing().getCharacteristic();
    }

    /**
     * Shorthand to create a polynomial from given coefficients in order from lowest exponent to highest.
     * <p>
     * For example, {@code getPoly(1,2,3)} returns \(1 + 2 \cdot X + 3 \cdot X^2\).
     *
     * @param coefficients the polynomial's coefficients in orderfrom lowest exponent to highest
     */
    public static Polynomial getPoly(RingElement... coefficients) {
        if (coefficients.length == 0)
            throw new IllegalArgumentException("Empty coefficients");

        PolynomialRing r = new PolynomialRing(coefficients[0].getStructure());
        return r.new Polynomial(coefficients);
    }


    /**
     * Creates a new polynomial using interpolation. One must provide at least d+1 data points to interpolate
     * a polynomial of degree d.
     * <p>
     * The interpolation implements Neville's Algorithm (see http://mathworld.wolfram.com/NevillesAlgorithm.html).
     * Beware that this operation has complexity \(O(n^2)\), where \(n\) is the number of dataPoints needed to interpolate
     * the polynomial (degree + 1).
     * Only to be used if the actual coefficients of the polynomial are unknown.
     * <p>
     * Note: This implementation is based on Apache's commons math library
     * (org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm#computeCoefficients).
     *
     * @param dataPoints         known points \((x_i, y_i)\) on the resulting polynomial \(P\) used for interpolation.
     * @param degreeOfPolynomial the desired degree of the interpolated polynomial
     * @return interpolated polynomial \(P\) where \(P(x_i) = y_i\) for every given data point.
     */
    public static Polynomial getPoly(Map<? extends RingElement, ? extends RingElement> dataPoints, int
            degreeOfPolynomial) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            throw new IllegalArgumentException("No data points provided for interpolation");
        }

        if (degreeOfPolynomial < 0) {
            throw new IllegalArgumentException("Degree of polynomial must be positive");
        }

        if (dataPoints.size() < degreeOfPolynomial + 1) {
            throw new IllegalArgumentException("Not enough data points provided for interpolation. " +
                    "Needed: " + (degreeOfPolynomial + 1) + " ; Got " + dataPoints.size());
        }

        Ring ring = dataPoints.keySet().stream().map(RingElement::getStructure).findFirst().get();
        int numberOfCoefficients = degreeOfPolynomial + 1;

        //Create arrays of x_i to simplify access during iterations
        RingElement[] xValues = dataPoints.keySet().toArray(new RingElement[dataPoints.size()]);

        //Start with zero for all coefficients
        ZpElement[] coefficients = new ZpElement[numberOfCoefficients];
        Arrays.fill(coefficients, ring.getZeroElement());

        // c[] are the coefficients of P(x) = (x-x[0])(x-x[1])...(x-x[n-1])
        // These correspond to the first "column" of Neville's interpolation "pyramid"
        final RingElement[] c = new RingElement[numberOfCoefficients + 1];
        c[0] = ring.getOneElement();
        for (int i = 0; i < numberOfCoefficients; i++) {
            for (int j = i; j > 0; j--) {
                c[j] = c[j - 1].add((c[j].mul(xValues[i])).neg());
            }
            c[0] = c[0].mul(xValues[i].neg());
            c[i + 1] = ring.getOneElement();
        }

        final RingElement[] tc = new RingElement[numberOfCoefficients];
        for (int i = 0; i < numberOfCoefficients; i++) {
            // calculate the divisor \prod\limits_{i\neq j}(x_i - x_j)
            // d = (x[i]-x[0])...(x[i]-x[i-1])(x[i]-x[i+1])...(x[i]-x[n-1])
            RingElement d = ring.getOneElement();
            for (int j = 0; j < numberOfCoefficients; j++) {
                if (i != j) {
                    d = d.mul(xValues[i].add(xValues[j].neg()));
                }
            }

            // t = \frac{x_i}{d}
            final RingElement t = dataPoints.get(xValues[i]).mul(d.inv());

            // Lagrange polynomial is the sum of n terms, each of which is a
            // polynomial of degree n-1. tc[] are the coefficients of the i-th
            // numerator Pi(x) = (x-x[0])...(x-x[i-1])(x-x[i+1])...(x-x[n-1]).
            tc[numberOfCoefficients - 1] = c[numberOfCoefficients];
            coefficients[numberOfCoefficients - 1] =
                    coefficients[numberOfCoefficients - 1].add(t.mul(tc[numberOfCoefficients - 1]));

            for (int j = numberOfCoefficients - 2; j >= 0; j--) {
                tc[j] = c[j + 1].add(tc[j + 1].mul(xValues[i]));
                coefficients[j] = coefficients[j].add(t.mul(tc[j]));
            }
        }

        return new PolynomialRing(ring).new Polynomial(coefficients);
    }


    /**
     * Creates a new polynomial using interpolation. The resulting polynomial will have the largest possible
     * degree (number of supplied data points - 1).
     * <p>
     * The interpolation implements Neville's Algorithm (see http://mathworld.wolfram.com/NevillesAlgorithm.html).
     * Beware that this operation has complexity \(O(n^2)\), where \(n\) is the number of dataPoints needed to
     * interpolate the polynomial (degree + 1).
     * Only to be used if the actual coefficients of the polynomial are unknown.
     * <p>
     * Note: This implementation is based on Apache's commons math library
     * (org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm#computeCoefficients).
     *
     * @param dataPoints known points \((x_i, y_i)\) on the resulting polynomial \(P\) used for interpolation.
     * @return interpolated polynomial \(P\) where \(P(x_i) = y_i\) for every given data point.
     */
    public static Polynomial getPoly(Map<? extends RingElement, ? extends RingElement> dataPoints) {
        return getPoly(dataPoints, dataPoints.size() - 1);
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.empty();
    }

    @Override
    public boolean isCommutative() {
        return true;
    }
}
