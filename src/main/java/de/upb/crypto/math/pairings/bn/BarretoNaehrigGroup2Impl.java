package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

public class BarretoNaehrigGroup2Impl extends BarretoNaehrigSourceGroupImpl {

    public BarretoNaehrigGroup2Impl(BigInteger size, BigInteger traceFrobenius, ExtensionFieldElement a6) {
        /* according to thesis of Naehrig, Remark 2.13 it holds that #E'(F_p^2)=(p-1+t)*#E(F_p) */
        super(size, a6.getStructure().getBaseField().size().subtract(BigInteger.ONE).add(traceFrobenius), a6);
    }

    /**
     * standard constructor for StandaloneRepresentation
     **/
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
}
