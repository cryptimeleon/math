package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.Representation;

import java.util.Objects;

public abstract class PairingTargetGroupElementImpl implements GroupElementImpl, UniqueByteRepresentable {

    protected ExtensionFieldElement elem;
    protected PairingTargetGroupImpl group;

    public ExtensionFieldElement getElem() {
        return elem;
    }

    public PairingTargetGroupElementImpl(PairingTargetGroupImpl g, ExtensionFieldElement fe) {
        group = g;
        elem = fe;
    }

    @Override
    public Representation getRepresentation() {
        return elem.getRepresentation();
    }

    @Override
    public PairingTargetGroupImpl getStructure() {
        return group;
    }

    @Override
    public PairingTargetGroupElementImpl inv() {
//		return new BarretoNaehrigTargetGroupElement(getStructure(),
//				(BarretoNaehrigFieldElement) impl.inv());
        /* Let the target field be an extension field of degree 12 of size p^12. The unit group has order
         * p^12-1. Hence, target group as a subgroup of Fp^12* of size r dividing p^12-1=(p^6-1)(p^6+1). Since 12 is the
         * embedding degree, r does not divide p^6-1. Hence, r divides p^6+1. Hence, for a in target group, it holds that a^(p^6+1)=1.
         * Hence a^(p^6+1)=aa^p^6=1. Hence, a^p^6 is the inverse of a.
         *
         * We can interpret target field
         * as the quadratic extension of F_p^6. Hence, exp. with p^6 is conjugation. Therefore, we write
         * a=a0+a1 x + a2 x^2 + a3x^3 + a4 x^4 + a5 x^ 5 with x zero of y^6+v in Fq^6.
         * y^2+v is irreducible in Fq^3 because 3 and  2 are coprime. We express a as element in K=Fq[y]/(y^2+v):
         *
         *  x^4x^3=x^6 x = -vx. Hence
         *
         * a = a0-a1 v x^4 x^3 + a2 x^2 +a3 x^3 + a4 x^4 + a5 x^5
         *   = a0 + a2 x^2 + a4x^4 + [a3 + a5 x^2 -a1v x^4] x^3
         *
         *   Because b0=a0 + a2 x^2 + a4x^4  and b1=[a3 + a5 x^2 -a1v x^4]
         *   are in F_p^6, exponentiation with p^6 is identity.
         *
         *   Because F_p^12 is a quadratic extension of F_p^6 and x^3 is in F_p^6 it
         *   holds that x^3^(p^6) = -x^3
         *
         *  Transforming back to the original field gives
         *
         *  a^p^6 = a0 -a1x +a2x^2-a3x^3+a4x^4-a5x^5
         */

//		return new BarretoNaehrigTargetGroupElement(getStructure(),
//				(BarretoNaehrigFieldElement) this.getImpl().pow(
//						this.getStructure().getFieldOfDefinition().getCharacteristic().pow(6))
//				);

        FieldElement coefficients[] = new FieldElement[this.getElem().getCoefficients().length];
        for (int i = 0; i < this.getElem().getCoefficients().length; i++) {
            if ((i % 2) == 0) {
                coefficients[i] = this.getElem().getCoefficients()[i];
            } else {
                coefficients[i] = this.getElem().getCoefficients()[i].neg();
            }
        }
        return getStructure().getElement(
                (ExtensionFieldElement) this.getStructure().getFieldOfDefinition().createElement(coefficients));


//		return new BarretoNaehrigTargetGroupElement(getStructure(),
//			         (BarretoNaehrigFieldElement) this.getImpl().conjugate());

    }

    @Override
    public PairingTargetGroupElementImpl op(GroupElementImpl e) throws IllegalArgumentException {
        return getStructure().getElement(elem.mul(((PairingTargetGroupElementImpl) e).getElem()));
    }

    public String toString() {
        return elem.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairingTargetGroupElementImpl that = (PairingTargetGroupElementImpl) o;
        return elem.equals(that.elem) &&
                group.equals(that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elem);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        accumulator = elem.updateAccumulator(accumulator);
        return accumulator;
    }
}
