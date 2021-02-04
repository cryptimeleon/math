package de.upb.crypto.math.structures.rings.extfield;

import de.upb.crypto.math.hash.ByteAccumulator;
import de.upb.crypto.math.hash.UniqueByteRepresentable;
import de.upb.crypto.math.structures.Element;
import de.upb.crypto.math.structures.rings.FieldElement;
import de.upb.crypto.math.structures.rings.RingElement;
import de.upb.crypto.math.structures.rings.polynomial.PolynomialRing;
import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.rings.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Elements in {@link ExtensionField}.
 */

public class ExtensionFieldElement implements FieldElement, UniqueByteRepresentable {

    private final ExtensionField field;
    private final FieldElement[] coefficients;

    public ExtensionFieldElement(ExtensionField f, FieldElement[] coefficients) {
        this.field = f;
        this.coefficients = coefficients.clone();
    }

    @Override
    public Representation getRepresentation() {
        ListRepresentation r = new ListRepresentation();
        for (FieldElement e : coefficients) {
            r.put(e.getRepresentation());
        }
        return r;
    }

    private static int[] minmax(int a, int b) {
        if (a < b)
            return new int[]{a, b};
        else
            return new int[]{b, a};

    }

    @Override
    public ExtensionFieldElement add(Element e) {
        FieldElement[] ec = ((ExtensionFieldElement) e).coefficients;

        int[] mima = minmax(this.coefficients.length, ec.length);

        FieldElement[] result = new FieldElement[mima[1]];

        int i = 0;
        for (; i < mima[0]; i++)
            result[i] = ec[i].add(this.coefficients[i]);

        for (; i < this.coefficients.length; i++) {
            result[i] = this.coefficients[i];
        }

        for (; i < ec.length; i++) {
            result[i] = ec[i];
        }

        return this.getStructure().createElement(result);
    }

    @Override
    public ExtensionFieldElement neg() {
        FieldElement[] result = new FieldElement[this.coefficients.length];

        for (int i = 0; i < this.coefficients.length; i++) {
            result[i] = this.coefficients[i].neg();
        }

        return this.getStructure().createElement(result);
    }

    @Override
    public ExtensionFieldElement mul(Element e) {
        ExtensionFieldElement bne = (ExtensionFieldElement) e;

        FieldElement[] result = new FieldElement[this.coefficients.length + bne.coefficients.length];

        for (int i = 0; i < result.length; i++)
            result[i] = this.getStructure().getBaseField().getZeroElement();

        for (int i = 0; i < this.coefficients.length; i++)
            for (int j = 0; j < bne.coefficients.length; j++) {
                result[i + j] = result[i + j].add(this.coefficients[i].mul(bne.coefficients[j]));
            }

        return this.getStructure().createElement(result);
    }

    /**
     * The coefficients of the polynomial over getStructure().getBaseField() defining this element
     */
    public FieldElement[] getCoefficients() {
        return this.coefficients;
    }

    @Override
    public ExtensionFieldElement inv() throws UnsupportedOperationException {
        PolynomialRing.Polynomial poly = PolynomialRing.getPoly(coefficients);
        PolynomialRing polyRing = poly.getStructure();
        RingElement[] eeaResult = polyRing.extendedEuclideanAlgorithm(poly, field.getDefiningPolynomial()); //eeaResult[0]*poly = eeaResult[2] (mod definingPolynomial) and eeaResult[2] is a unit (because definingPolynomial is irreducible)
        PolynomialRing.Polynomial inversePoly = (PolynomialRing.Polynomial) eeaResult[0].mul(eeaResult[2].inv());

        return getStructure().createElement(Arrays.copyOf(inversePoly.getCoefficients(), inversePoly.getCoefficients().length, FieldElement[].class));
    }

    /**
     * Computes the conjugate of this element.
     * <p>
     * For an element x with coefficients in F_q, compute x^q.
     *
     * @return the first conjugate of this element
     */
    public ExtensionFieldElement conjugate() {
        if (this.getStructure().isBaseField()) {
            return this;
        } else if ((this.getStructure().getExtensionDegree() == 2)) {
            /*
             *
             * We write an element as a+i b where i is a root of x^2+v. Then a-ib is the
             * second root of x^2+v. \sum ai x^i.
             *
             */
            FieldElement[] coefficients
                    = new FieldElement[this.coefficients.length];

            for (int i = 0; i < this.coefficients.length; i++) {
                if (i % 2 == 0) {
                    coefficients[i] = (ExtensionFieldElement) this.coefficients[i];
                } else {
                    coefficients[i] = (ExtensionFieldElement) this.coefficients[i].neg();
                }
            }

            return this.getStructure().createElement(coefficients);
        } else {
            throw new UnsupportedOperationException("Conjugation only supported for extension degree 1 and 2.");
        }
    }

    @Override
    public ExtensionFieldElement applyFrobenius() {
        //Yes, this is very suboptimal.
        ExtensionFieldElement result = getStructure().getZeroElement();
        for (int i=0; i<coefficients.length; i++) {
            result = result.add(field.createElement(coefficients[i].applyFrobenius()).mul(getStructure().frobeniusOfXPowers[i]));
        }
        return result;
        //return (ExtensionFieldElement) this.pow(getStructure().getCharacteristic());
    }

    @Override
    public ExtensionField getStructure() {
        return this.field;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(coefficients);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExtensionFieldElement)) {
            return false;
        }
        ExtensionFieldElement other = (ExtensionFieldElement) obj;

        int[] mm = minmax(coefficients.length, other.coefficients.length);
        for (int i = 0; i < mm[0]; i++) {
            if (!coefficients[i].equals(other.coefficients[i])) {
                return false;
            }
        }
        for (int i = mm[0]; i < coefficients.length; i++) {
            if (!coefficients[i].isZero()) {
                return false;
            }
        }
        for (int i = mm[0]; i < other.coefficients.length; i++) {
            if (!other.coefficients[i].isZero()) {
                return false;
            }
        }

        return true;
    }

    public ExtensionFieldElement reduce() {
        /*
         * this implementation assures that elements are always reduced because : 1. elements are immutable 2. at creation, the representation is always reduced in the create method of BarretoNaehrigField
         */
        return this;
    }

    @Override
    public String toString() {
        if (field.extensionDegree == 1)
            return coefficients[0].toString(); //no brackets for trivial "extensions"

        return "["+Arrays.stream(coefficients).map(Object::toString).collect(Collectors.joining(", "))+"]";
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        Consumer<UniqueByteRepresentable> accumulationMethod;
        if (!field.getUniqueByteLength().isPresent()) { //underlying field does not offer a constant length ubr
            accumulationMethod = accumulator::escapeAndSeparate;
        } else { //underlying field has fixed length representations
            accumulationMethod = accumulator::append;
        }

        for (int i = 0; i < coefficients.length; i++)
            accumulationMethod.accept(coefficients[i]);

        for (int i = coefficients.length; i <= field.getExtensionDegree(); i++) //pad with zeros if the coefficients array is too short
            accumulationMethod.accept(field.getConstant().getStructure().getZeroElement());

        return accumulator;

    }

    @Override
    public BigInteger asInteger() throws UnsupportedOperationException {
        if (coefficients.length == 0)
            return BigInteger.ZERO;
        for (int i=1;i<coefficients.length;i++)
            if (!coefficients[i].isZero())
                throw new UnsupportedOperationException("No integer value for " + this);
        return coefficients[0].asInteger();
    }
}
