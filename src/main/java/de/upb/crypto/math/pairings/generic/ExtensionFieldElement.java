package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Elements in ExtensionField.
 *
 * @author peter.guenther
 */

public class ExtensionFieldElement implements FieldElement, UniqueByteRepresentable {

    private ExtensionField field;
    private FieldElement[] coefficients;

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
     * @return The first conjugate of this element
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
        String r = "";
        r += "[";

        for (FieldElement e : coefficients) {
            r += e.toString();
            r += " ,";
        }

        r += "]";
        return r;
    }

    public ArrayList<BigInteger> asBigIntegerList() {
        ArrayList<BigInteger> list = new ArrayList<BigInteger>();

        for (FieldElement c : coefficients) {
            if (c instanceof ExtensionFieldElement) {
                for (BigInteger cc : ((ExtensionFieldElement) c).asBigIntegerList()) {
                    list.add(cc);
                }
            } else if (c instanceof ZnElement) {
                list.add(((ZnElement) c).getInteger());
            }
        }

        return list;

    }


    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        Consumer<UniqueByteRepresentable> accumulationMethod;
        if (!field.getUniqueByteLength().isPresent()) { //underlying field does not offer a constant length ubr
            accumulationMethod = accumulator::escapeAndAppendAndSeparate;
        } else { //underlying field has fixed length representations
            accumulationMethod = accumulator::append;
        }

        for (int i = 0; i < coefficients.length; i++)
            accumulationMethod.accept(coefficients[i]);

        for (int i = coefficients.length; i <= field.getExtensionDegree(); i++) //pad with zeros if the coefficients array is too short
            accumulationMethod.accept(field.getConstant().getStructure().getZeroElement());

        return accumulator;

    }
}
