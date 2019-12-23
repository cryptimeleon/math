package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;

import java.math.BigInteger;
import java.util.function.Function;

public class BarretoNaehrigGroup2 extends BarretoNaehrigSourceGroup {

    public BarretoNaehrigGroup2(BigInteger size, BigInteger traceFrobenius, ExtensionFieldElement a6,
                                Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        /* according to thesis of Naehrig, Remark 2.13 it holds that #E'(F_p^2)=(p-1+t)*#E(F_p) */
        super(size, a6.getStructure().getBaseField().size().subtract(BigInteger.ONE).add(traceFrobenius), a6,
                ecpCoordConstructor);
    }

    /**
     * standard constructor for StandaloneRepresentation
     **/
    public BarretoNaehrigGroup2(Representation r, Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        super(r, ecpCoordConstructor);
    }

    @Override
    public BarretoNaehrigSourceGroupElement getElement(FieldElement x, FieldElement y) {
        return new BarretoNaehrigSourceGroupElement(this, x, y);
    }

    @Override
    public EllipticCurvePoint getNeutralElement() {
        return new BarretoNaehrigSourceGroupElement(this);
    }
}
