package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.hash.UniqueByteRepresentable;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.Representation;

public abstract class PairingTargetGroupElement implements GroupElement, UniqueByteRepresentable {

    protected ExtensionFieldElement impl;
    protected PairingTargetGroup group;

    public ExtensionFieldElement getImpl() {
        return impl;
    }

    public PairingTargetGroupElement(PairingTargetGroup g, ExtensionFieldElement fe) {
        group = g;
        impl = fe;
    }

    @Override
    public Representation getRepresentation() {
        return impl.getRepresentation();
    }

    @Override
    public PairingTargetGroup getStructure() {
        return group;
    }

    @Override
    public PairingTargetGroupElement inv() {
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

        FieldElement coefficients[] = new FieldElement[this.getImpl().getCoefficients().length];
        for (int i = 0; i < this.getImpl().getCoefficients().length; i++) {
            if ((i % 2) == 0) {
                coefficients[i] = this.getImpl().getCoefficients()[i];
            } else {
                coefficients[i] = this.getImpl().getCoefficients()[i].neg();
            }
        }
        return getStructure().getElement(
                (ExtensionFieldElement) this.getStructure().getFieldOfDefinition().createElement(coefficients));


//		return new BarretoNaehrigTargetGroupElement(getStructure(),
//			         (BarretoNaehrigFieldElement) this.getImpl().conjugate());

    }

    @Override
    public PairingTargetGroupElement op(Element e) throws IllegalArgumentException {
        return getStructure().getElement(
                (ExtensionFieldElement) (impl.mul(((PairingTargetGroupElement) e).getImpl())));
    }

    public String toString() {
        return impl.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((impl == null) ? 0 : impl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof PairingTargetGroupElement))
            return false;
        PairingTargetGroupElement other = (PairingTargetGroupElement) obj;

        if (impl == null) {
            if (other.impl != null)
                return false;
        } else if (!impl.equals(other.impl))
            return false;
        return true;
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        accumulator = impl.updateAccumulator(accumulator);
        return accumulator;
    }
}
