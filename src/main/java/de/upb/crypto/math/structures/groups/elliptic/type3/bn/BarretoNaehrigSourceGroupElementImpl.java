package de.upb.crypto.math.structures.groups.elliptic.type3.bn;

import de.upb.crypto.math.structures.rings.FieldElement;
import de.upb.crypto.math.structures.groups.elliptic.PairingSourceGroupElement;

import java.math.BigInteger;

/**
 * Abstract class for elements of both G1 and G2.
 */
public abstract class BarretoNaehrigSourceGroupElementImpl extends PairingSourceGroupElement {

    public BarretoNaehrigSourceGroupElementImpl(BarretoNaehrigSourceGroupImpl curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    public BarretoNaehrigSourceGroupElementImpl(BarretoNaehrigSourceGroupImpl curve) {
        super(curve);
    }


    @Override
    public BarretoNaehrigSourceGroupElementImpl pow(BigInteger e) {
        return (BarretoNaehrigSourceGroupElementImpl) super.pow(e);
    }

    /**
     * Compresses the point to an integer.
     * <p>
     * Compress point (x,y) by mapping x to an integer i in {0,1,2} such that
     * {@code this.getStructure().mapToPoint(y,this.compress(x,y)).equals(this)}.
     * Hence (y,i) is a compression of (x,y) of approximately half size.
     *
     * @return compression of x
     */
    public int compressX() {
        /*
         * search for correct x-coordiante wrt. to this.getStructure().getFieldOfDefinition().getCubeRoot()
         */
        // TODO, more efficient way to injective mapping of primitive cube root into the integers
        for (int i = 0; i < 3; i++) {
            if (((BarretoNaehrigSourceGroupImpl) this.getStructure()).mapToPoint(this.getY(), i).equals(this)) {
                return i;
            }
        }
        throw new RuntimeException("Not able to compress point");
    }
}
