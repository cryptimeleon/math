package org.cryptimeleon.math.structures.groups.elliptic.type1.supersingular;

import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.elliptic.AbstractPairing;
import org.cryptimeleon.math.structures.groups.elliptic.PairingSourceGroupElement;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

/**
 * Tate-pairing implementation for the supersingular bilinear group.
 */
public class SupersingularTatePairing extends AbstractPairing {

    //SupersingularTypeADistortionMap distortionMap;

    public SupersingularTatePairing(SupersingularSourceGroupImpl g1, SupersingularTargetGroupImpl gT) {
        super(g1, g1, gT);
        //	this.distortionMap = new SupersingularTypeADistortionMap(g1,gT.getFieldOfDefinition());
    }

    public SupersingularTatePairing(Representation r) {
        super(r);
    }

    @Override
    protected ExtensionFieldElement evaluateLine(FieldElement[] line, PairingSourceGroupElement P, PairingSourceGroupElement Q) {
        ExtensionField targetField = (ExtensionField) gT.getFieldOfDefinition();
        //ExtensionField baseField = (ExtensionField) Q.getFieldOfDefinition();

        /*
         * G2=G1\subseteq EC[Fq]. We need to apply the distortion map \phi to
         * map Q to EC[Fq^2].
         *
         * Then we evalutate the line at (xq,yq)=Phi(Q). With (xq,yq) -> (-xq,i yq) and i^2=-1 we obtain
         *
         *
         *  a_0 (yq'-yp) - a_1(xq'-xp) = a1(xq + yq) - a0 yp + a0 yq i
         *
         */

        if (!P.isNormalized() || !Q.isNormalized()) {
            throw new IllegalArgumentException("Currently, only affine points are supported.");
        }

        FieldElement xp = P.getX();
        FieldElement yp = P.getY();
        FieldElement xq = Q.getX();
        FieldElement yq = Q.getY();

        FieldElement t0 = line[1].mul(xq.add(xp)).sub(line[0].mul(yp));
        FieldElement t1 = line[0].mul(yq);

        return targetField.createElement(t0, t1);


    }

    @Override
    protected ExtensionFieldElement pair(PairingSourceGroupElement P, PairingSourceGroupElement Q) {

        /*because of denominator elimination, forulas used for pairing compuation are not complete. Eg they fail for neutral elements*/
        if (P.isNeutralElement() || Q.isNeutralElement()) {
            return gT.getFieldOfDefinition().getOneElement();
        }

        ExtensionFieldElement result = this.miller(P, Q, g1.size());

        return result;

    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}
