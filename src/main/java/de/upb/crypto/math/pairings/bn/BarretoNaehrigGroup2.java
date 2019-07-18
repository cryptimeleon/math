package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;

public class BarretoNaehrigGroup2 extends BarretoNaehrigSourceGroup {
    
    protected BigInteger traceFrobenius = null;
    
    public BarretoNaehrigGroup2(BigInteger size, BigInteger traceFrobenius, ExtensionFieldElement a6) {
        /* according to thesis of Naehrig, Remark 2.13 it holds that #E'(F_p^2)=(p-1+t)*#E(F_p) */
        super(size, a6.getStructure().getBaseField().size().subtract(BigInteger.ONE).add(traceFrobenius), a6);
        this.traceFrobenius = traceFrobenius;
    }
    
    public BigInteger getTraceFrobenius() {
        return traceFrobenius;
    }
    
    /**
     * standard constructor for StandaloneRepresentation
     **/
    public BarretoNaehrigGroup2(Representation r) {
        super(r);
    }

    @Override
    public BarretoNaehrigGroup2Element getElement(FieldElement x, FieldElement y) {
        return new BarretoNaehrigGroup2Element(this, x, y);
    }

    @Override
    public GroupElement getNeutralElement() {
        return new BarretoNaehrigGroup2Element(this);
    }
}
