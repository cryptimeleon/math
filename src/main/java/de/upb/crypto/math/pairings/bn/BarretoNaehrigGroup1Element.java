package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;

public class BarretoNaehrigGroup1Element extends BarretoNaehrigSourceGroupElement {
    private BarretoNaehrigGroup1 curve;

    /**
     * Construct point with given x and y coordinate.
     *
     * @param curve - curve of point
     * @param x     - x coordinate of point
     * @param y     - y coordinate of point
     */
    BarretoNaehrigGroup1Element(BarretoNaehrigGroup1 curve, FieldElement x, FieldElement y) {
        super(curve, x, y);
    }

    BarretoNaehrigGroup1Element(BarretoNaehrigGroup1 curve) {
        super(curve);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;

        BarretoNaehrigGroup1Element that = (BarretoNaehrigGroup1Element) o;

        return curve != null ? curve.equals(that.curve) : that.curve == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (curve != null ? curve.hashCode() : 0);
        return result;
    }
}
