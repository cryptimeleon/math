package org.cryptimeleon.math.structures.rings.extfield;

import org.cryptimeleon.math.serialization.*;
import org.cryptimeleon.math.structures.rings.Field;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.RingElement;
import org.cryptimeleon.math.structures.rings.polynomial.PolynomialRing;
import org.cryptimeleon.math.structures.rings.zn.Zp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * An extension field using an irreducible polynomial of the form \(x^\text{extensionDegree} + \text{constant}\).
 */
public class ExtensionField implements Field {

    //primitive cube root that is required for cube-root computation,
    private FieldElement cubeRoot = null;
    protected FieldElement constant;
    protected int extensionDegree;
    protected PolynomialRing.Polynomial definingPolynomial;
    /**
     * \(\text{frobeniusOfXPowers}[i] = (x^p)^i \mod (x^\text{extensionDegree} + \text{constant})\)
     * for \(i \leq extensionDegree\)
     */
    protected ExtensionFieldElement[] frobeniusOfXPowers;


    /**
     * Create extension defined by polynomial \(x^\text{extensionDegree} + \text{constant}\).
     */
    private void init(FieldElement constant, int extensionDegree) {
        this.constant = constant;
        this.extensionDegree = extensionDegree;
        RingElement[] coefficients = new RingElement[extensionDegree + 1];
        coefficients[extensionDegree] = constant.getStructure().getOneElement();
        coefficients[0] = constant;
        for (int i = 1; i < extensionDegree; i++)
            coefficients[i] = constant.getStructure().getZeroElement();

        this.definingPolynomial = PolynomialRing.getPoly(coefficients);

        //Precompute frobenius stuff (there's probably an embarrassingly better way to do this but ... here we go for now)
        frobeniusOfXPowers = new ExtensionFieldElement[extensionDegree+1];
        frobeniusOfXPowers[0] = getOneElement();
        if (extensionDegree > 0) {
            frobeniusOfXPowers[1] = (ExtensionFieldElement) createElement(constant.getStructure().getZeroElement(), constant.getStructure().getOneElement()).pow(getCharacteristic()); //"x^p"
            for (int i = 2; i < frobeniusOfXPowers.length; i++)
                frobeniusOfXPowers[i] = frobeniusOfXPowers[i-1].mul(frobeniusOfXPowers[1]);
        }
    }

    /**
     * Create extension defined by polynomial \(x^\text{extensionDegree} + \text{constant}\).
     */
    public ExtensionField(FieldElement constant, int extensionDegree) {
        init(constant, extensionDegree);
    }

    /**
     * Instantiates the prime order finite field Zp.
     * (i.e. the special case of an extension field with extension degree 1)
     *
     * @param p size of the field (must be prime)
     */
    public ExtensionField(BigInteger p) {
        Zp baseField = new Zp(p);
        constant = baseField.getZeroElement();
        extensionDegree = 1;
        init(constant, extensionDegree);
    }


    public FieldElement getConstant() {
        return this.constant;
    }

    public boolean isBaseField() {
        return (this.extensionDegree == 1);
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation o = new ObjectRepresentation();
		/*if(this.getCubeRoot()!=null)
			o.put("cubeRoot", this.getCubeRoot().getRepresentation());*/
        o.put("constant", this.constant.getRepresentation());
        o.put("extensionDegree", new BigIntegerRepresentation(extensionDegree));
        //o.put("baseField", this.constant.getStructure().getRepresentation());
        o.put("baseField", new RepresentableRepresentation(constant.getStructure()));
        return o;
    }

    public ExtensionField(Representation r) {
        ObjectRepresentation o = (ObjectRepresentation) r;

        //Field baseField = new BarretoNaehrigField(o.get("baseField"));

        Field baseField = (Field) ((RepresentableRepresentation) o.get("baseField")).recreateRepresentable();


        init(baseField.restoreElement(o.get("constant")), o.get("extensionDegree").bigInt().get().intValueExact());

        if (o.get("cubeRoot") != null)
            this.setCubeRoot(this.restoreElement(o.get("cubeRoot")));
    }


    /**
     * Returns fixed primitive cube root in this field.
     *
     * @return primitive cube root
     */
    public FieldElement getCubeRoot() {
        if (this.cubeRoot == null)
            generatePrimitiveCubeRoot();
        return cubeRoot;
    }


    /**
     * Set auxiliary primitive cube root in this field.
     */
    protected void setCubeRoot(FieldElement cubeRoot) {
        this.cubeRoot = cubeRoot;
    }

    /**
     * Search and set primitive cube root in this field.
     */
    public void generatePrimitiveCubeRoot() {

        FieldElement e;
        do {
            e = this.getUniformlyRandomElement();
            e = e.pow(this.size().subtract(BigInteger.ONE).divide(BigInteger.valueOf(3)));
        } while (e.isOne());

        setCubeRoot(e);
    }


    public FieldElement[] reduce(FieldElement[] coefficients) {
        if (coefficients.length <= this.extensionDegree) {
            return coefficients;
        }

        FieldElement[] result = new FieldElement[this.extensionDegree];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.getBaseField().getZeroElement();
        }


        /*this extension is defined by x^extDeg+const, hence
         * c*x^n with n= i extDeg + j, can be represented as
         * c*(-const)^i x^j=c*(-1)^i * const ^i x^j
         */
        FieldElement equivalence = this.getBaseField().getOneElement();
        for (int degree = 0; degree < coefficients.length; ) {

            int i = degree / this.extensionDegree;
            int j = degree % this.extensionDegree;

            FieldElement tmp = equivalence.mul(coefficients[degree]);
            //account for (-1)^i
            if (i % 2 == 0) {
                result[j] = result[j].add(tmp);
            } else {
                result[j] = result[j].sub(tmp);
            }

            degree++;
            if (degree % this.extensionDegree == 0)
                equivalence = equivalence.mul(this.constant);
        }

        //TODO: possible after issue #99 is fixed
//		for (FieldElement c:coefficients) {
//			c = c.reduce();
//		}

        return result;
    }


    /**
     * Create an element in this field, based on a given polynomial
     * representation of an element in the ideal of the quotient field.
     */
    public ExtensionFieldElement createElement(FieldElement... coefficients) {
        return new ExtensionFieldElement(this, reduce(coefficients));
    }

    @Override
    public ExtensionFieldElement getElement(BigInteger i) {
        return createElement(constant.getStructure().getElement(i));
    }

    @Override
    public ExtensionFieldElement getElement(long i) {
        return getElement(BigInteger.valueOf(i));
    }

    @Override
    public double estimateCostInvPerOp() {
        // Tested with base field Zp(741618179)
        return 0.3;
    }

    @Override
    public double estimateCostNegPerOp() {
        return constant.getStructure().estimateCostNegPerOp();
    }

    /**
     * Map an integer b to an element in this field.
     * <p>
     * Let this field L be a degree k extension of a field K given by \(L=K/(f(X))\). Let n be the size of K.
     * This function computes the n-ary representation of b:
     * <p>
     * \(b=b_0 n^0 + b_1 n^1 + b_2 n^2 ... b_t n^t\)
     * <p>
     * Then, it maps \(b_i\) to \(c_i\) in K by recursively invoking {@code K.createElement(b_i)}
     * and constructs the polynomial
     * <p>
     * \(p(X) = c_0 X^0 + c_1 X^1 + c_2 X^t + ... + c_t X^t\).
     * Then, it projects \(p(X)\) to \(K/(f(X))\) in the canonical way.
     * <p>
     * Hence, this mapping is injective on the integers smaller than {@code this.size()}.
     *
     * @param b the integer to map to a field element
     * @return field element corresponding to the given integer
     */
    public ExtensionFieldElement createElement(BigInteger b) {
        /* split byte array into coefficients, starting with constant coefficient
         * then pass slices to subfield */


        List<BigInteger> pary = new ArrayList<>();

        BigInteger a = b.abs();

        while (!a.equals(BigInteger.ZERO)) {


            BigInteger r = a.mod(this.getBaseField().size());

            a = a.subtract(r);
            a = a.divide(this.getBaseField().size());

            pary.add(r);

        }

        FieldElement[] coefficients = new FieldElement[pary.size()];
        int i = 0;
        for (BigInteger c : pary) {
            if (this.getBaseField() instanceof Zp) {
                coefficients[i] = ((Zp) this.getBaseField()).createZnElement(c);
            } else {
                coefficients[i] = ((ExtensionField) this.getBaseField()).createElement(c);
            }
            i++;
        }


        ExtensionFieldElement result = this.createElement(coefficients);
        if (b.compareTo(BigInteger.ZERO) < 0) {
            result = result.neg();
        }

        return result;

    }

    @Override
    public String toString() {
        if (this.extensionDegree == 1)
            return this.getBaseField().toString();
        else
            return "degree " + this.extensionDegree + " extension of " + this.getBaseField();
    }

    @Override
    public BigInteger sizeUnitGroup() throws UnsupportedOperationException {
        return this.size().subtract(BigInteger.ONE);
    }

    public Field getBaseField() {
        return constant.getStructure();
    }

    @Override
    public BigInteger getCharacteristic() throws UnsupportedOperationException {
        return getBaseField().getCharacteristic();
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        return getBaseField().size().pow(this.extensionDegree);
    }

    @Override
    public FieldElement getPrimitiveElement() throws UnsupportedOperationException {
        return null;
    }

    @Override
    public ExtensionFieldElement getZeroElement() {
        return this.createElement(getBaseField().getZeroElement());
    }

    @Override
    public ExtensionFieldElement getOneElement() {
        return this.createElement(getBaseField().getOneElement());
    }

    @Override
    public ExtensionFieldElement restoreElement(Representation repr) {
        ListRepresentation lr = (ListRepresentation) repr;
        FieldElement[] coefficients = new FieldElement[lr.size()];
        for (int i = 0; i < lr.size(); i++)
            coefficients[i] = this.getBaseField().restoreElement(lr.get(i));

        return this.createElement(coefficients);
    }

    public ExtensionFieldElement createElement(List<BigInteger> coefficients) {
        FieldElement[] fes = new FieldElement[this.extensionDegree];
        for (int i = 0; i < this.extensionDegree; i++) {
            if (this.getBaseField() instanceof ExtensionField) {
                ExtensionField baseField = (ExtensionField) this.getBaseField();
                fes[i] = baseField.createElement(
                        coefficients.subList(i * baseField.extensionDegree, (i + 1) * baseField.extensionDegree));

            } else if (this.getBaseField() instanceof Zp) {
                Zp baseField = (Zp) this.getBaseField();
                if (coefficients.size() == 0) {
                    fes[i] = baseField.new ZpElement(BigInteger.ZERO);
                } else if (coefficients.size() == 1) {
                    fes[i] = baseField.new ZpElement(coefficients.get(i));
                } else {
                    throw new IllegalArgumentException("Not able to create ZpElement for list of size larger than 1.");
                }
            } else {
                throw new RuntimeException("Creating elements from integer arrays is only supported for Zp base fields.");
            }


        }
        return this.createElement(fes);
    }

    @Override
    public ExtensionFieldElement getUniformlyRandomElement() throws UnsupportedOperationException {
        FieldElement[] coefficients = new FieldElement[this.extensionDegree];
        for (int i = 0; i < extensionDegree; i++) {
            coefficients[i] = getBaseField().getUniformlyRandomElement();
        }

        return this.createElement(coefficients);
    }

    /**
     * Returns the irreducible polynomial that this field is defined over.
     * It's of the form x^extensionDegree + constant.
     */
    public PolynomialRing.Polynomial getDefiningPolynomial() {
        return definingPolynomial;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((constant == null) ? 0 : constant.hashCode());
        result = prime * result + extensionDegree;
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
        if (!(obj instanceof ExtensionField)) {
            return false;
        }
        ExtensionField other = (ExtensionField) obj;
        if (constant == null) {
            if (other.constant != null) {
                return false;
            }
        } else if (!constant.equals(other.constant)) {
            return false;
        }
        return extensionDegree == other.extensionDegree;
    }

    public int getExtensionDegree() {
        return extensionDegree;
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return constant.getStructure().getUniqueByteLength().map(ubl -> ubl * (extensionDegree + 1)); //number of coefficients
    }
}
