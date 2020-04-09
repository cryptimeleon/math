package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.*;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.structures.ec.ProjectiveECPCoordinate;

import java.math.BigInteger;

/**
 * Tate-pairing specific implementation of BN based pairings.
 *
 * @author peter.guenther
 */
public class BarretoNaehrigTatePairing extends AbstractPairing {
    /**
     * Construct Tate pairing {@code g1} x {@code g2} -> {@code gT}.
     */
    public BarretoNaehrigTatePairing(BarretoNaehrigGroup1 g1, BarretoNaehrigGroup2 g2, BarretoNaehrigTargetGroup gT) {
        super(g1, g2, gT);

    }

    public BarretoNaehrigTatePairing(BarretoNaehrigSourceGroup g1, BarretoNaehrigSourceGroup g2,
                                     BarretoNaehrigTargetGroup gT) {
        super(g1, g2, gT);
    }

    public BarretoNaehrigTatePairing(Representation r) {
        super(r);
    }

    @Override
    public ExtensionFieldElement evaluateLine(FieldElement[] line, PairingSourceGroupElement P, PairingSourceGroupElement A) {
        ExtensionField targetField = (ExtensionField) this.getGT().getFieldOfDefinition();
        ExtensionField extField = (ExtensionField) A.getFieldOfDefinition();

        /*
         * G2 is a subgroup sextic twist E':y^2=x^3-b/v with xi^6=v from E'->E, phi:(x,y)->(x xi^2,y xi^3). GT is
         * defined over degree 6 extension field defined by X^6-v.
         *
         * Hence,
         * l_P(phi(x_A,y_A)) = a_0(y_A xi^3-y_P) - a_1(x_A xi^2 - x_P)
         *                   = (a_1 x_P - a_0 y_P) + 0 xi + (- a_1 x_A) xi^2 + a_0 y_A xi^3 + 0 xi^4 + 0 xi^5.
         *
         * Here, non-vertical lines are parameterize by [a_0,a_1]=[1,lambda_P] where lambda_P is the slope through P and
         * vertical lines are parameterized by [a_0,a_1]=[0,1].
         */
        if (P.getPoint() instanceof ProjectiveECPCoordinate) {
            // Formula from "Implementing Cryptographic Pairings over Barreto-Naehrig Curves", Devegili et al. Section 5
            // Formula: l_{P,Q}(A) = (y_A * z_P^3 - y_P) * z_R - (x_A * z_P^3 - x_P * z_P) * \lambda_{P,Q}
            //  where R = P + Q
            PairingSourceGroupElement ANormalized = (PairingSourceGroupElement) A.normalize();
            FieldElement[] coefficients = new FieldElement[4];
            FieldElement zPToThree = P.getZ().pow(BigInteger.valueOf(3));
            coefficients[0] = extField.lift(line[1].mul(P.getZ()).mul(P.getX()).sub(P.getY().mul(line[0])));
            coefficients[1] = extField.getZeroElement();
            coefficients[2] = extField.lift(line[1].mul(zPToThree)).mul(ANormalized.getX());
            coefficients[3] = extField.lift(line[0].mul(zPToThree)).mul(ANormalized.getY());
            return targetField.createElement(coefficients);
        } else {
            FieldElement[] coefficients = new FieldElement[4];
            coefficients[0] = extField.lift((ExtensionFieldElement) P.getX().mul(line[1]).sub(P.getY().mul(line[0])));

            coefficients[1] = extField.getZeroElement();
            coefficients[2] = extField.lift((ExtensionFieldElement) line[1]).mul(A.getX()).neg();

            coefficients[3] = A.getY().mul(extField.lift((ExtensionFieldElement) line[0]));

            return targetField.createElement(coefficients);
        }
    }

    @Override
    protected ExtensionFieldElement pair(PairingSourceGroupElement P, PairingSourceGroupElement Q) {
        ExtensionFieldElement result = this.miller(P, Q, this.getG1().size());
        /*this might happen, if P and Q are from same subgroup. In this case, we get neutral element for Tate pairing.*/
        if (result.isZero()) {
            return result.getStructure().getOneElement();
        } else {
            return result;
        }

    }

    @Override
    public BarretoNaehrigSourceGroup getG1() {
        return (BarretoNaehrigSourceGroup) super.getG1();
    }

    @Override
    public BarretoNaehrigSourceGroup getG2() {
        return (BarretoNaehrigSourceGroup) super.getG2();
    }

    @Override
    public BarretoNaehrigTargetGroup getGT() {
        return (BarretoNaehrigTargetGroup) super.getGT();
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
