package de.upb.crypto.math.structures.quotient;

import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.Ring;
import de.upb.crypto.math.interfaces.structures.RingElement;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Implements the quotient ring \(R/I\) for some ring \(R\) and principal ideal \(I\)
 * (i.e. the ring consisting of equivalence classes \([a] = \{a + r | r \in I\}\) for \(a \in R\))
 * <p>
 * The base ring must be an euclidean domain, i.e. it needs to implement division with remainder.
 *
 * @param <RE> type of the quotient ring's elements (i.e. equivalence class type, NOT type of their representatives!)
 *            (may be {@code RingElement} or {@code FieldElement})
 */
public abstract class QuotientRing<RE extends RingElement> implements Ring {
    protected Ring base;
    protected Ideal ideal;

    public Ideal getIdeal() {
        return ideal;
    }

    public QuotientRing(Ring base, Ideal quotient) {
        this.base = base;
        this.ideal = quotient;
    }

    public QuotientRing(Representation repr) {
        base = (Ring) repr.obj().get("base").repr().recreateRepresentable();
        ideal = (Ideal) repr.obj().get("ideal").repr().recreateRepresentable();
    }

    public Ring getQuotientRingBase() {
        return base;
    }

    /**
     * Subclasses implement this to define how to create elements for this ring
     * (i.e. how to create an equivalence class instance).
     * <p>
     * You need to do something like:
     * <pre>
     * return new MyQuotientRingElementSubclass(representative);
     * </pre>
     * @param representative the representative of the equivalence class
     * @return an equivalence class
     */
    public abstract RE createElement(RingElement representative);

    @Override
    public RE getElement(BigInteger i) {
        return createElement(base.getElement(i));
    }

    public abstract class QuotientRingElement implements RingElement {
        protected RingElement representative;

        public QuotientRingElement(RingElement representative) {
            this.representative = representative;
            reduce();
        }

        /**
         * "Reduces" the representative element to some sort of "canonical" representative of the same equivalence class
         * (e.g., do modulo reduction in Z/nZ or in a polynomial ring).
         */
        protected abstract void reduce();

        @Override
        public RE add(Element e) {
            return createElement(representative.add(((QuotientRingElement) e).representative));
        }

        @Override
        public RE sub(Element e) {
            return createElement(representative.sub(((QuotientRingElement) e).representative));
        }

        @Override
        public RE neg() {
            return createElement(representative.neg());
        }

        @Override
        public RE mul(Element e) {
            return createElement(representative.mul(((QuotientRingElement) e).representative));
        }

        @Override
        public RE inv() throws UnsupportedOperationException {
            ArrayList<RingElement> elements = new ArrayList<>(ideal.getGenerators());
            elements.add(representative);

            //Computing someUnit = gcd(idealGenerators, representative) = \sum_idealGenerators generator_i*x_i + representative*x_{n+1}
            //Then x_{n+1}*someUnit^{-1} is (a representative for) the inverse of this element
            ArrayList<RingElement> result = base.extendedEuclideanAlgorithm(elements);
            RingElement sum = base.getZeroElement();
//			for(int i=0;i<elements.size();i++) {
//				sum = sum.add(elements.get(i).mul(result.get(i)));
//			}
//			if (!sum.equals(result.get(result.size()-1))) {
//				throw new InternalError();
//			};
            if (!result.get(result.size() - 1).isUnit()) //gcd must be a unit, otherwise we cannot invert
                throw new UnsupportedOperationException("This element is not a unit");

            return createElement(result.get(result.size() - 1).inv().mul(result.get(result.size() - 2)));
        }

        @Override
        public RE div(Element e) {
            return this.mul(((RingElement) e).inv());
        }

        @Override
        public Representation getRepresentation() {
            return representative.getRepresentation();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            // can break symmetry (reconsider this if implementing equals for F2FiniteFieldExtension)
            if (!(obj instanceof RingElement))
                return false;
            if (!this.getStructure().equals(((RingElement) obj).getStructure()))
                return false;

            // Two elements a, b are equal iff they are in the same equivalence class,
            // i.e. their representative's difference is a member of the ideal.
            return ideal.isMember(this.representative.sub(((QuotientRingElement) obj).representative));
        }

        @Override
        public abstract int hashCode();

        @Override
        public String toString() {
            return "<" + representative.toString() + ">";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        QuotientRing<?> r = (QuotientRing<?>) obj;
        return r.base.equals(this.base) && r.ideal.equals(this.ideal);
    }

    @Override
    public int hashCode() {
        return base.hashCode() + 7 * ideal.hashCode();
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation repr = new ObjectRepresentation();
        repr.put("base", new RepresentableRepresentation(base));
        repr.put("ideal", new RepresentableRepresentation(ideal));

        return repr;
    }


    @Override
    public RE getZeroElement() {
        return createElement(base.getZeroElement());
    }

    @Override
    public RE getOneElement() {
        return createElement(base.getOneElement());
    }

    @Override
    public RE getElement(Representation repr) {
        return createElement(base.getElement(repr));
    }
}
