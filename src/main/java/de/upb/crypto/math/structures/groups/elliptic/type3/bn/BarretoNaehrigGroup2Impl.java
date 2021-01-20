package de.upb.crypto.math.structures.groups.elliptic.type3.bn;

import de.upb.crypto.math.structures.rings.FieldElement;
import de.upb.crypto.math.structures.groups.GroupElementImpl;
import de.upb.crypto.math.structures.rings.extfield.ExtensionFieldElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

/**
 * G2 in the Barreto-Naehrig bilinear group.
 */
public class BarretoNaehrigGroup2Impl extends BarretoNaehrigSourceGroupImpl {

    /**
     * Construct subgroup of E:y^2=x^3+a6 using given parameters.
     *
     * @param size size of the subgroup
     * @param traceFrobenius used to calculate the cofactor for the resulting group
     * @param a6 curve parameter for weierstrass equation
     */
    public BarretoNaehrigGroup2Impl(BigInteger size, BigInteger traceFrobenius, ExtensionFieldElement a6) {
        /* according to thesis of Naehrig, Remark 2.13 it holds that #E'(F_p^2)=(p-1+t)*#E(F_p) */
        super(size, a6.getStructure().getBaseField().size().subtract(BigInteger.ONE).add(traceFrobenius), a6);
    }

    /**
     * Recreates a group from the given representation.
     *
     * @param r the representation to use for reconstruction
     */
    public BarretoNaehrigGroup2Impl(Representation r) {
        super(r);
    }

    @Override
    public BarretoNaehrigGroup2ElementImpl getElement(FieldElement x, FieldElement y) {
        return new BarretoNaehrigGroup2ElementImpl(this, x, y);
    }

    @Override
    public GroupElementImpl getNeutralElement() {
        return new BarretoNaehrigGroup2ElementImpl(this);
    }

    @Override
    public double estimateCostInvPerOp() {
        return 600;
    }
}
