package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.serialization.StringRepresentation;
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
    public BarretoNaehrigGroup1(BigInteger size, BigInteger cofactor, ExtensionFieldElement a6, Class coordinateClass) {
        super(size, cofactor, a6, coordinateClass);
    }

    /**
     * standard constructor for StandaloneRepresentation
     **/
    public BarretoNaehrigGroup1(Representation r) {
        super(r);
    }

    private BigInteger traceFrobenius() {
        // t=q-E(F_q)+1 = q-r+1
        return this.getFieldOfDefinition().size().subtract(this.size()).add(BigInteger.ONE);
    }

    public WeierstrassCurve withCoordinateClass(Class newCoordinateClass) {
        Representation repr = this.getRepresentation();
        ((ObjectRepresentation) repr).put("coordinate_class", new StringRepresentation(newCoordinateClass.getTypeName()));
        return new BarretoNaehrigGroup1(repr);
    }
}
