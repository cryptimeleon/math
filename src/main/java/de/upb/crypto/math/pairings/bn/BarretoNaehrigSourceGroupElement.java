package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.WeierstrassCurve;
import de.upb.crypto.math.structures.ec.AbstractECPCoordinate;
import de.upb.crypto.math.structures.ec.EllipticCurvePoint;

import java.math.BigInteger;
import java.util.function.Function;

public class BarretoNaehrigSourceGroupElement extends EllipticCurvePoint {

    public BarretoNaehrigSourceGroupElement(BarretoNaehrigSourceGroup curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public BarretoNaehrigSourceGroupElement(BarretoNaehrigSourceGroup curve) {
        super(curve);
    }


    @Override
    public BarretoNaehrigSourceGroupElement pow(BigInteger e) {
        return (BarretoNaehrigSourceGroupElement) super.pow(e);
    }

    /**
     * Point compression.
     * <p>
     * Compress point (x,y) by mapping x to an integer i in {0,1,2} such that this.getStructure().mapToPoint(y,this.compress(x,y)).equals(this). Hence (y,i) is a compression of (x,y) of approximately half size.
     *
     * @return compression of x
     */
    public int compressX() {
        /*
         * search for correct x-coordiante wrt. to this.getStructure().getFieldOfDefinition().getCubeRoot()
         */
        // TODO, more efficient way to injective mapping of primitive cube root into the integers
        // Normalize first, else this wont work
        this.setPoint(this.normalize().getPoint());
        for (int i = 0; i < 3; i++) {
            if (((BarretoNaehrigSourceGroup) this.getStructure()).mapToPoint(this.getNormalizedY(), i).equals(this)) {
                return i;
            }
        }
        throw new RuntimeException("Not able to compress point");
    }
}
