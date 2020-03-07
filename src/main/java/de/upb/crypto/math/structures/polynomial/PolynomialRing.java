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

public class PolynomialRing implements Ring {
    protected Ring baseRing;

    /**
     * Creates the polynomial ring over base.
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

    public class Polynomial implements RingElement {
        /**
         * Coefficients of this polynomial, i.e. this = \sum coefficients[i] * x^i. All
         * entries with index > degree may be null. (iterate from i=0 to degree). This
         * array always contains at least one element.
         */
        protected RingElement[] coefficients;

        protected int degree;

        /**
         * Creates an empty polynomial (set "coefficients" array yourself!, then call
         * computeDegree()). Use createPolyInternal() as a shortcut for that. This omits
         * the copying process and ensureArrayInvariants() call of the public
         * constructors.
         */
        private Polynomial() {

        }

        /**
         * Helper function: Uses the private constructor to create a polynomial from the
         * coefficient list
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
         * order i.e. new Polynomial (10) returns 0 + 1*X + 0*X^2 + 1*X^3.
         * <p>
         * This constructor should only be used if the underlying ring is Zp(2)
         * <p>
         * (i.e. this is a helper constructor for polynomials in the galois field)
         *
         * @param coefficients the coefficients in descending order (!)
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
         * Creates a polynomial with the given coefficients e.g., new Polynomial(1,2,3)
         * returns 1+2x+3x^2
         *
         * @param coefficients with _increasing_ exponent.
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
         * Creates a polynomial with the given coefficients
         *
         * @param coefficients of the polynomial \sum coefficients.get(i) * x^i
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
         * Creates the polynomial a * x^i
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
         * Creates the polynomial x^i
         */
        public Polynomial(int i) {
            this(i, baseRing.getOneElement());
        }

        /**
         * Sets the degree member variable according to the coefficients. This is done
         * once when creating the polynomial
         */
        protected void computeDegree() {
            degree = coefficients.length - 1;
            while (degree > 0 && (coefficients[degree] == null || coefficients[degree].isZero()))
                degree--;
        }

        /**
         * Makes sure that the coefficients array does not contain null values at
         * indices <= degree
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
         * Return the value of the polynomial evaluated at <code>x</code>. Applies
         * Horner scheme.
         *
         * @param x position to evaluate
         * @return result of evaluation at position <code>x</code>
         */
        public RingElement evaluate(Element x) {
            if (!(x.getStructure().equals(baseRing))) {
                throw new UnsupportedOperationException(
                        "Evalutate only supports elements from the base ring as argument");
            }
            RingElement result = baseRing.getZeroElement();

            for (int i = this.coefficients.length; i > 0; i--) {
                result = this.coefficients[i - 1].add(result.mul(x));
            }

            return result;
        }

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

        @Override
        public Polynomial neg() {
            RingElement[] result = new RingElement[degree + 1];
            for (int i = 0; i <= degree; i++)
                result[i] = coefficients[i].neg();
            return createPolyInternal(result);
        }

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

        @Override
        public Polynomial mul(BigInteger k) {
            RingElement[] result = new RingElement[this.degree+1];
            for (int i=0;i<result.length;i++) {
                result[i] = this.coefficients[i].mul(k);
            }

            return createPolyInternal(result);
        }

        @Override
        public Polynomial inv() throws UnsupportedOperationException {
            if (degree > 0)
                throw new UnsupportedOperationException("Cannot invert non-zero-degree polynomials");
            return new Polynomial(coefficients[0].inv());
        }

        /**
         * Returns the scalar product of this polynomial with elem from the base ring
         */
        public Polynomial scalarMul(Element elem) {
            RingElement[] result = new RingElement[degree + 1];
            for (int i = 0; i <= degree; i++)
                result[i] = coefficients[i].mul(elem);

            return createPolyInternal(result);
        }

        /**
         * Returns the normalized polynomial a_n^{-1}*this
         *
         * @throws UnsupportedOperationException if the highest coefficient is not a unit
         */
        public Polynomial normalize() throws UnsupportedOperationException {
            return scalarMul(coefficients[degree].inv());
        }

        @Override
        public boolean divides(RingElement e) throws UnsupportedOperationException {
            if (e.isZero())
                return true;
            return e.divideWithRemainder(this)[1].isZero();
        }

        @Override
        public Polynomial[] divideWithRemainder(RingElement e)
                throws UnsupportedOperationException, IllegalArgumentException {
            Polynomial divisor = (Polynomial) e, dividend = this;
            Polynomial quotient = getZeroElement(), remainder;

            RingElement invOfLeadingCoeff = divisor.coefficients[divisor.degree].inv(); // note that we require
            // invertibility of coefficients
            // here
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
        public boolean equals(Object obj) {
            if (!(obj instanceof Polynomial) || !((Polynomial) obj).getStructure().equals(getStructure()))
                return false;
            Polynomial o = (Polynomial) obj;
            if (o.degree != degree)
                return false;
            for (int i = 0; i <= degree; i++)
                if (!o.coefficients[i].equals(coefficients[i]))
                    return false;

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
     * Shorthand to create polynomials
     *
     * @param coefficients the polynomial's coefficients (from lowest exponent to highest),
     *                     e.g., getPoly(1,2,3) returns 1+2x+3x^2
     */
    public static Polynomial getPoly(RingElement... coefficients) {
        if (coefficients.length == 0)
            throw new IllegalArgumentException("Empty coefficients");

        PolynomialRing r = new PolynomialRing(coefficients[0].getStructure());
        return r.new Polynomial(coefficients);
    }


    /**
     * Shorthand to create polynomials using interpolation. One mus provide at least d+1 data points to interpolate
     * a polynomial of degree d.
     *
     * <p>
     * The interpolation implements Neville's Algorithm (see http://mathworld.wolfram.com/NevillesAlgorithm.html).
     * Beware that this operation has complexity O(n^2), where n is the number of dataPoints needed to interpolate
     * the polynomial (degree + 1).
     * Only to be used if the actual coefficients of the polynomial are unknown.
     * </p>
     * <p>
     * Note: This implementation is based on Apache's commons math library
     * (org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm#computeCoefficients)
     *
     * @param dataPoints         known points (x_i, y_i) on the resulting polynomial P used for interpolation.
     * @param degreeOfPolynomial the desired degree of the interpolated polynomial
     * @return interpolated polynomial P where P(x_i) = y_i for every given data point.
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
     * Shorthand to create polynomials using interpolation. The resulting polynomial will have the largest possible
     * degree (number of supplied data points - 1)
     *
     * <p>
     * The interpolation implements Neville's Algorithm (see http://mathworld.wolfram.com/NevillesAlgorithm.html).
     * Beware that this operation has complexity O(n^2), where n is the number of dataPoints needed to interpolate
     * the polynomial (degree + 1).
     * Only to be used if the actual coefficients of the polynomial are unknown.
     * </p>
     * <p>
     * Note: This implementation is based on Apache's commons math library
     * (org.apache.commons.math3.analysis.polynomials.PolynomialFunctionLagrangeForm#computeCoefficients)
     *
     * @param dataPoints known points (x_i, y_i) on the resulting polynomial P used for interpolation.
     * @return interpolated polynomial P where P(x_i) = y_i for every given data point.
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
