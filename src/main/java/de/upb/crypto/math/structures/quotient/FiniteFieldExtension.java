package de.upb.crypto.math.structures.quotient;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.polynomial.PolynomialRing;
import de.upb.crypto.math.structures.polynomial.PolynomialRing.Polynomial;

import java.math.BigInteger;
import java.util.Optional;

/**
 * The extension of a finite field \(\mathbb{F}\) represented by
 * \(\mathbb{F}[X]/(f)\) for some irreducible polynomial \(f \in \mathbb{F}[X]\).
 * <p>
 * This can be used to build field towers.
 */
public class FiniteFieldExtension extends QuotientRing<FieldElement> implements Field {
    /**
     * The degree of this extension (the degree of the irreducible polynomial underlying this field extension).
     */
    protected int extensionDegree;

    /**
     * Creates a finite field extension over the given base field using the given irreducible polynomial as modulus.
     *
     * @param baseField       a field \(\mathbb{F}\)
     * @param irreduciblePoly an irreducible polynomial with coefficients in \(\mathbb{F}\)
     */
    public FiniteFieldExtension(Field baseField, Polynomial irreduciblePoly) {
        super(new PolynomialRing(baseField), new PrincipalIdeal(irreduciblePoly));
        extensionDegree = irreduciblePoly.getDegree();
        if (extensionDegree < 1)
            throw new IllegalArgumentException("Illegal polynomial");
    }

    /**
     * Creates a finite field extension from the base field of an irreducible polynomial f.
     *
     * @param irreduciblePoly an irreducible polynomial with coefficients in \(\mathbb{F}\)
     */
    public FiniteFieldExtension(Polynomial irreduciblePoly) {
        this((Field) irreduciblePoly.getStructure().getBaseRing(), irreduciblePoly);
    }

    public FiniteFieldExtension(Representation repr) {
        super(repr);
        extensionDegree = ((Polynomial) ideal.getGenerators().get(0)).getDegree();
        if (extensionDegree < 1)
            throw new IllegalArgumentException("Illegal polynomial");
    }

    @Override
    public BigInteger getCharacteristic() {
        return base.getCharacteristic();
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        BigInteger bs = ((PolynomialRing) base).getBaseRing().size();
        if (bs == null)
            return null;
        return bs.pow(extensionDegree);
    }

    @Override
    public FieldElement getPrimitiveElement() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BigInteger sizeUnitGroup() throws UnsupportedOperationException {
        return Field.super.sizeUnitGroup();
    }

    @Override
    public FieldElement getUniformlyRandomElement() throws UnsupportedOperationException {
        //Choose a random polynomial over the base field
        Ring baseField = ((PolynomialRing) base).getBaseRing();
        RingElement[] coefficients = new FieldElement[extensionDegree];
        for (int i = 0; i < extensionDegree; i++)
            coefficients[i] = baseField.getUniformlyRandomElement();

        return createElement(((PolynomialRing) base).new Polynomial(coefficients));
    }

    @Override
    public FieldElement createElement(RingElement representative) {
        return createElement((Polynomial) representative);
    }

    /**
     * Maps elements from the base field to this extension field.
     * <p>
     * Let this be an extension \(K\) of \(\mathbb{F}\) and \(e\) be an element in \(\mathbb{F}\).
     * Then we map \(e\) to \(K\) by representing \(e\) as a constant (polynomial) in the base ring of \(K\).
     *
     * @param e the element of the base field to be mapped to this field
     * @return e as an element in this field
     */
    public FieldElement fromBaseField(FieldElement e) {
        return createElement(((PolynomialRing) getQuotientRingBase()).new Polynomial(e));
    }

    /**
     * Get the base field of this extension field.
     *
     * @return the base field
     */
    public Field getBaseField() {
        return (Field) ((PolynomialRing) getQuotientRingBase()).getBaseRing();
    }

    /**
     * Creates a new element of this field from the given polynomial.
     */
    public FieldElement createElement(Polynomial p) {
        return new FiniteFieldElement(p);
    }

    /**
     * The elements of this field extension.
     */
    public class FiniteFieldElement extends QuotientRingElement implements FieldElement {

        public FiniteFieldElement(Polynomial representative) {
            super(representative);
        }

        public Polynomial getRepresentative() {
            return (Polynomial) this.representative;
        }

        @Override
        public FiniteFieldExtension getStructure() {
            return FiniteFieldExtension.this;
        }

        @Override
        public boolean divides(RingElement e) throws UnsupportedOperationException {
            return FieldElement.super.divides(e);
        }

        @Override
        public RingElement[] divideWithRemainder(RingElement e) throws UnsupportedOperationException, IllegalArgumentException {
            return FieldElement.super.divideWithRemainder(e);
        }

        @Override
        public BigInteger getRank() throws UnsupportedOperationException {
            return FieldElement.super.getRank();
        }

        @Override
        public int hashCode() {
            return 0; //not sure whether we can do better
        }

        @Override
        protected void reduce() {
            //Reduce the polynomial degree by polynomial division
            representative = representative.divideWithRemainder(ideal.getGenerators().get(0))[1];
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            return representative.updateAccumulator(accumulator);
        }


    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return base.getUniqueByteLength();
    }
}
