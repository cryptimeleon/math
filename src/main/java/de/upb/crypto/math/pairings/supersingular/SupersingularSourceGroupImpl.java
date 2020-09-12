package de.upb.crypto.math.pairings.supersingular;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupImpl;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.helpers.FiniteFieldTools;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

/**
 * A type 1 pairing group:
 * Let E = {(x,y) over F_q | y^2 = x^3 - 3*x} (q prime and q = 3 mod 4).
 * Then this class represents E[getSize()], i.e. the subgroup of size getSize().
 */
public class SupersingularSourceGroupImpl extends PairingSourceGroupImpl {

    /**
     * Instantiates the group
     *
     * @param size              the desired size of the group
     * @param cofactor          the number c such that size * c = number of points on the WeierstrassCurve over fieldOfDefinition
     * @param fieldOfDefinition the field where x,y from the Weierstrass equation come from
     */
    public SupersingularSourceGroupImpl(BigInteger size, BigInteger cofactor, ExtensionField fieldOfDefinition) {
        super(size, cofactor, fieldOfDefinition.getElement(-3), fieldOfDefinition.getZeroElement());
    }

    public SupersingularSourceGroupImpl(Representation r) {
        super(r);
    }

    @Override
    public SupersingularSourceGroupElementImpl getNeutralElement() {
        return new SupersingularSourceGroupElementImpl(this);
    }

    @Override
    public SupersingularSourceGroupElementImpl getElement(FieldElement x, FieldElement y) {
        return new SupersingularSourceGroupElementImpl(this, x, y);
    }


    /**
     * A one to one mapping F_q -> E(F_q)\{O}
     * <p>
     * A value z is mapped to the point (z,y) for appropriate y if z(z^2+A) is a QR
     * and to (-z,-y) otherwise.
     * Note that -1 is a QNR for type A pairings.
     * Hence z(z^2+A) is a QR iff -z(z^2+A)=(-z)((-z)^2+A) is a QNR.
     * <p>
     * This mapping is bijective because if z(z^2+A) is a QNR -z is mapped to (y,-z).
     *
     * @param z an element from this group's base field (getFieldOfDefinition())
     */
    protected SupersingularSourceGroupElementImpl mapToPoint(ExtensionFieldElement z) {
        ExtensionFieldElement x, y;
        x = z;

        if (!this.isShortForm() || !this.getA6().isZero()) {
            throw new UnsupportedOperationException("Mapping to point only implemented for curves of the form x^3+a4x=y^2");
        }
        y = x.mul(x).add(this.getA4()).mul(x); //x^3+a_4 x + a_6

        // y^2=x^3+ax, if x^3+ax is a QNR then -(x^3-ax) is a QR because p=3 mod 4
        // hence, we can set x to -x

        if (!FiniteFieldTools.isSquare(y)) {
            x = x.neg();
            y = y.neg();
            // to get a one to one mapping, we need to invert y (to hit the negative points)
            y = (ExtensionFieldElement) FiniteFieldTools.sqrt(y).neg();
        } else {
            y = (ExtensionFieldElement) FiniteFieldTools.sqrt(y);
        }

        /*now we need to map the point (x,y) on the curve to our subgroup and return it*/
        return (SupersingularSourceGroupElementImpl) cofactorMultiplication(x, y);
    }

    @Override
    public SupersingularSourceGroupElementImpl getGenerator() {
        if (generator != null)
            return (SupersingularSourceGroupElementImpl) generator;

        SupersingularSourceGroupElementImpl elem;
        do {
            elem = this.getUniformlyRandomElement();
        } while (elem.equals(getNeutralElement()));

        return elem;
    }

    @Override
    public boolean hasPrimeSize() {
        return true;
    }

    @Override
    public SupersingularSourceGroupElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        if (getSize().compareTo(getCofactor()) < 0 && generator != null) { //cheaper to just do rndInt*generator as the steps below include cofactor multiplcation
            return (SupersingularSourceGroupElementImpl) getGenerator().pow(new Zn(getSize()).getUniformlyRandomElement().getInteger());
        } else {
            return mapToPoint((ExtensionFieldElement) getFieldOfDefinition().getUniformlyRandomElement());
        }
    }

    @Override
    public boolean isShortForm() {
        return true;
    }
}
