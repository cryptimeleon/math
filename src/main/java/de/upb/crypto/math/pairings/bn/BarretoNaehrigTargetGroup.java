package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingTargetGroup;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;


public class BarretoNaehrigTargetGroup extends PairingTargetGroup {
    /**
     * Constructs a subgroup of size size in F12 where F12=F(v)=F[x]/(x^6+v).
     *
     * @param v    element that defines extension of degree 6
     * @param size size of subgroup
     */
    public BarretoNaehrigTargetGroup(ExtensionFieldElement v, BigInteger size) {
        super(new ExtensionField(v, 6), size);
    }


    public BarretoNaehrigTargetGroup(Representation r) {
        super(r);
    }

    @Override
    public BarretoNaehrigTargetGroupElement getElement(ExtensionFieldElement fe) {
        return new BarretoNaehrigTargetGroupElement(this, fe);
    }
}
