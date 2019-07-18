package de.upb.crypto.math.pairings.bn;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupRequirement;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Field;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.pairings.generic.AbstractPairing;
import de.upb.crypto.math.pairings.generic.ExtensionField;
import de.upb.crypto.math.pairings.generic.ExtensionFieldElement;
import de.upb.crypto.math.pairings.generic.PairingSourceGroupElement;
import de.upb.crypto.math.structures.ec.AbstractEllipticCurvePoint;
import de.upb.crypto.math.structures.zn.Zp;
import de.upb.crypto.math.swante.misc;

import java.math.BigInteger;

/**
 * Ate-pairing specific implementation of BN based pairings.
 * Just like the Tate pairing, but using t as parameter in the miller loop.
 * Also, since G2 must be limited, we add a special generator that will generate
 * the desired subgroup from the original G2 of the Tate pairing.
 * @author sscholz
 */
public class MyBarretoNaehrigAtePairing extends AbstractPairing {
    public final BarretoNaehrigGroup2Element actualG2Generator;
    
    /**
     * Construct Ate pairing {@code g1} x {@code g2} -> {@code gT}.
     */
    public MyBarretoNaehrigAtePairing(BarretoNaehrigSourceGroup g1, BarretoNaehrigSourceGroup g2,
                                      BarretoNaehrigTargetGroup gT, BarretoNaehrigGroup2Element actualG2Generator) {
        super(g1, g2, gT);
        this.actualG2Generator = actualG2Generator;
    }
    
    /**
     *
     * @return random non-zero element from the actual G2 group of the Ate pairing
     */
    @Override
    public BarretoNaehrigSourceGroupElement getUnitRandomElementFromG2Group() {
        BigInteger e = misc.randBig(g2.size().subtract(BigInteger.ONE));
        return this.actualG2Generator.pow(e);
    }

    @Override
    protected ExtensionFieldElement evaluateLine(FieldElement[] line, PairingSourceGroupElement P, PairingSourceGroupElement Q) {
        ExtensionField targetField = this.getGT().getFieldOfDefinition();
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
        // swante: xi = z (im BN paper)
        if (!P.isNormalized() || !Q.isNormalized()) {
            throw new IllegalArgumentException("Currently, only affine points are supported.");
        }

        FieldElement[] coefficients = new FieldElement[4];
        coefficients[0] = extField.lift(P.getX().mul(line[1]).sub(P.getY().mul(line[0])));

        coefficients[1] = extField.getZeroElement();
        coefficients[2] = extField.lift(line[1]).mul(Q.getX()).neg();

        coefficients[3] = Q.getY().mul(extField.lift(line[0]));

        return targetField.createElement(coefficients);
    }
    
    /**
     * Uses the same order as the Tate pairing:
     * @param P - from G1
     * @param Q - from G2
     * @return the pairing result from GT
     */
    @Override
    protected ExtensionFieldElement pair(PairingSourceGroupElement P, PairingSourceGroupElement Q) {
        BigInteger t = ((BarretoNaehrigGroup2) this.getG2()).getTraceFrobenius();
        ExtensionFieldElement result = this.miller(P, Q, t.subtract(BigInteger.ONE));
        /*this might happen, if P and Q are from same subgroup. In this case, we get neutral element for Ate pairing.*/
        if (result.isZero()) {
            return result.getStructure().getOneElement();
        } else {
            return result;
        }

    }
    
    /**
     *
     * @param bitLength
     * @return ate pairing with desired security. uses the existing Tate pairing,
     * but additionally computes a correct generator for G2 so that random elements
     * can be computed.
     */
    public static MyBarretoNaehrigAtePairing createAtePairing(int bitLength) {
        BarretoNaehrigProvider bnProvider = new BarretoNaehrigProvider();
        BilinearMap bnMap = bnProvider.provideBilinearGroup(bitLength, new BilinearGroupRequirement(BilinearGroup.Type.TYPE_3)).getBilinearMap();
        while (true) {
            AbstractEllipticCurvePoint Q = (AbstractEllipticCurvePoint) bnMap.getG2().getUniformlyRandomElement();
            AbstractEllipticCurvePoint Qinv = Q.inv();
            Field structure = ((AbstractEllipticCurvePoint) bnMap.getG1().getGenerator()).getX().getStructure();
            BigInteger p = ((ExtensionField) structure).getBaseField().size();
            Q.applyFrobenius(p);
            BarretoNaehrigGroup2Element generator = (BarretoNaehrigGroup2Element) Q.add(Qinv);
            if (!generator.isNeutralElement()) {
                return new MyBarretoNaehrigAtePairing((BarretoNaehrigGroup1)bnMap.getG1(), (BarretoNaehrigGroup2)bnMap.getG2(), (BarretoNaehrigTargetGroup) bnMap.getGT(), generator);
            }
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
        return "Ate Pairing G1xG2->Gt of Type 3";
    }

    @Override
    public boolean isSymmetric() {
        return false;
    }
}
