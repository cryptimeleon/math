package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;

import java.math.BigInteger;
import java.util.function.Function;

public class BarretoNaehrigGroup1 extends BarretoNaehrigSourceGroup {
    /**
     * Construct subgroup of E:y^2=x^3+a6 with assumed size and cofactor.
     *
     * @param size
     * @param cofactor
     * @param a6
     */
    public BarretoNaehrigGroup1(BigInteger size, BigInteger cofactor, ExtensionFieldElement a6,
                                Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        super(size, cofactor, a6, ecpCoordConstructor);
    }

    /**
     * standard constructor for StandaloneRepresentation
     **/
    public BarretoNaehrigGroup1(Representation r, Function<WeierstrassCurve, AbstractECPCoordinate> ecpCoordConstructor) {
        super(r, ecpCoordConstructor);
    }

    private BigInteger traceFrobenius() {
        // t=q-E(F_q)+1 = q-r+1
        return this.getFieldOfDefinition().size().subtract(this.size()).add(BigInteger.ONE);
    }

    @Override
    public BarretoNaehrigSourceGroupElement getNeutralElement() {
        return new BarretoNaehrigSourceGroupElement(this);
    }

    @Override
    public BarretoNaehrigSourceGroupElement getElement(FieldElement x, FieldElement y) {
        return new BarretoNaehrigSourceGroupElement(this, x, y);
    }


}
