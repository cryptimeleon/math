package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.AbstractPairing;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;
import de.upb.crypto.math.serialization.Representation;

/**
 * Tate-pairing specific implementation of BN based pairings.
 *
 * @author peter.guenther
 */
public class BarretoNaehrigTatePairing extends AbstractPairing {
    /**
     * Construct Tate pairing \(\mathbb{G}_1 \times \mathbb{G}_2 \rightarrow \mathbb{G}_T\).
     */
    public BarretoNaehrigTatePairing(BarretoNaehrigGroup1Impl g1, BarretoNaehrigGroup2Impl g2, BarretoNaehrigTargetGroupImpl gT) {
        super(g1, g2, gT);

    }

    public BarretoNaehrigTatePairing(BarretoNaehrigSourceGroupImpl g1, BarretoNaehrigSourceGroupImpl g2,
                                     BarretoNaehrigTargetGroupImpl gT) {
        super(g1, g2, gT);
    }

    public BarretoNaehrigTatePairing(Representation r) {
        super(r);
    }

    /**
     * TODO (rh): Write javadoc for this. I had some notes somewhere about how exactly this works
     *  and especially the line parameterization format.
     *
     * @param line parameterization of the line
     */
    @Override
    protected ExtensionFieldElement evaluateLine(FieldElement[] line, PairingSourceGroupElement P, PairingSourceGroupElement Q) {
        ExtensionField targetField = (ExtensionField) gT.getFieldOfDefinition();
        ExtensionField extField = (ExtensionField) Q.getFieldOfDefinition();

        /*
         * G2 is a subgroup sextic twist E':y^2=x^3-b/v with xi^6=v from E'->E, phi:(x,y)->(x xi^2,y xi^3). GT is
         * defined over degree 6 extension field defined by X^6-v.
         *
         * Hence,
         * l_P(phi(xq,yq))=a_0(yq xi^3-yp) - a_1(xq xi^2 - xp) = (a_1 xp - a_0 yp) + 0 xi + (- a_1 xq) xi^2 + a_0 yq
         * xi^3 + 0 xi^4 + 0 xi^5.
         *
         * Here, non-vertical lines are parameterize by [a_0,a_1]=[1,lambda_P] where lambda_P is the slope through P and
         * vertical lines are parameterized by [a_0,a_1]=[0,1].
         */
        if (!P.isNormalized() || !Q.isNormalized()) {
            throw new IllegalArgumentException("Currently, only affine points are supported.");
        }

        FieldElement[] coefficients = new FieldElement[4];
        coefficients[0] = extField.lift((ExtensionFieldElement) P.getX().mul(line[1]).sub(P.getY().mul(line[0])));

        coefficients[1] = extField.getZeroElement();
        coefficients[2] = extField.lift((ExtensionFieldElement) line[1]).mul(Q.getX()).neg();

        coefficients[3] = Q.getY().mul(extField.lift((ExtensionFieldElement) line[0]));

        return targetField.createElement(coefficients);
    }

    @Override
    protected ExtensionFieldElement pair(PairingSourceGroupElement P, PairingSourceGroupElement Q) {
        ExtensionFieldElement result = this.miller(P, Q, g1.size());
        /*this might happen, if P and Q are from same subgroup. In this case, we get neutral element for Tate pairing.*/
        if (result.isZero()) {
            return result.getStructure().getOneElement();
        } else {
            return result;
        }

    }

    @Override
    public String toString() {
        return "Tate Pairing G1xG2->Gt of Type 3";
    }

    @Override
    public boolean isSymmetric() {
        return false;
    }
}
