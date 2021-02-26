package org.cryptimeleon.math.structures.groups.elliptic;

import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.RepresentableRepresentation;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElementImpl;
import org.cryptimeleon.math.structures.rings.FieldElement;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionField;
import org.cryptimeleon.math.structures.rings.extfield.ExtensionFieldElement;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Base class for pairings such as Tate Pairing, Ate Pairing and Optimal Ate Pairing.
 */
public abstract class AbstractPairing implements BilinearMapImpl {
    protected PairingSourceGroupImpl g1;
    protected PairingSourceGroupImpl g2;
    protected PairingTargetGroupImpl gT;

    protected void init(PairingSourceGroupImpl g1, PairingSourceGroupImpl g2, PairingTargetGroupImpl gT) {
        this.g1 = g1;
        this.g2 = g2;
        this.gT = gT;
    }

    public AbstractPairing(PairingSourceGroupImpl g1, PairingSourceGroupImpl g2, PairingTargetGroupImpl gT) {
        init(g1, g2, gT);
    }

    @Override
    public PairingTargetGroupElementImpl apply(GroupElementImpl g, GroupElementImpl h, BigInteger exponent) {
        return exponentiate(pair((PairingSourceGroupElement) g.pow(exponent), (PairingSourceGroupElement) h));
    }

    /**
     * Implements final exponentiation.
     * <p>
     * Exponentiation of f to the power of e=(q^k-1)/r where r is the size of groups
     * G1, G2, and GT, respectively.
     *
     * @param f the element to exponentiate
     * @return f^e
     */
    public PairingTargetGroupElementImpl exponentiate(FieldElement f) {
        return gT.getElement((ExtensionFieldElement) f.pow(gT.getCofactor()));
    }

    public AbstractPairing(Representation r) {
        ObjectRepresentation or = (ObjectRepresentation) r;

        init(
                (PairingSourceGroupImpl) ((RepresentableRepresentation) or.get("G1")).recreateRepresentable(),
                (PairingSourceGroupImpl) ((RepresentableRepresentation) or.get("G2")).recreateRepresentable(),
                (PairingTargetGroupImpl) ((RepresentableRepresentation) or.get("GT")).recreateRepresentable()
        );
    }

    /**
     * Abstract class that evaluates a line through a given point at another point.
     * <p>
     * The line is parameterized by the point P and the argument line.
     * Here, line is the result of the function EllipticCurvePoint.computeLine.
     * The line is evaluated at the point Q.
     *
     * @param line - parameterization of the line
     * @param P    - point on the line
     * @param Q    - point where line es evaluated
     * @return l_P(Q)
     */
    protected abstract ExtensionFieldElement evaluateLine(FieldElement[] line, PairingSourceGroupElement P, PairingSourceGroupElement Q);


    /**
     * Computes the first step of the pairing.
     * <p>
     * A pairing is computed in several steps, where the first step includes miller Algorithm
     * and the second step is the final exponentiation.
     * This functions computes the first step of the pairing computation
     * that depends on the concrete pairing.
     *
     * @param P - first argument of pairing
     * @param Q - second argument of pairing
     * @return - result of first step
     */
    protected abstract ExtensionFieldElement pair(PairingSourceGroupElement P, PairingSourceGroupElement Q);


    /**
     * Implementation of Miller algorithm to be used as part of the function pair.
     * <p>
     * This algorithm evaluates a function with divisor [n](P-O) at Q and applies denominator elimination.
     *
     * @param P - first argument
     * @param Q - second argument
     * @param n - loop bound
     * @return f_n(P, Q)
     */
    protected ExtensionFieldElement miller(PairingSourceGroupElement P, PairingSourceGroupElement Q, BigInteger n) {
        FieldElement[] line;
        ExtensionField targetField = (ExtensionField) gT.getFieldOfDefinition();
        /*
         * f_1=1; f_2=1 R=P;
         */

        FieldElement millerVariable = targetField.getOneElement();



        /*
         * e2 needs to be normalized for the evaluateLineAt and evaluateVertical
         * functions we normalize also e1 to be faster with the addition of P to
         * R.
         */
        PairingSourceGroupElement pNormalized = (PairingSourceGroupElement) P.normalize();
        PairingSourceGroupElement qNormalized = (PairingSourceGroupElement) Q.normalize();



        /*
         * variable point of the pairing
         */
        PairingSourceGroupElement R = pNormalized;

        for (int i = n.bitLength() - 2; i >= 0; i--) {

            /*
             * f_1=f_1^2
             */
            millerVariable = (FieldElement) millerVariable.square();


            /*
             * calculate parametrization of tangent line l_R,R.
             */
            line = R.computeLine(R);


            /*
             * f*=l_R,R(Q)
             *
             * Evaluate line at Q and multiply result with f. How to evaluate the line,
             * depends on the concrete implementation. It depends on the form of coordinates
             * and on untwisting R or Q. For example for affine coordinates, the line is returned
             * in the form of [a0, a1] such that l_R,R(x,y) = a_0(y-yR) - a_1(x-xR).
             */
            millerVariable = millerVariable.mul(evaluateLine(line, R, qNormalized));

            /*
             * R=2R
             */
            R = (PairingSourceGroupElement) R.add(R, line);

            /*
             * if bit order_i is set to 1 also do
             *
             * f_1*=l_V,P(Q), R = R+P
             */
            if (n.testBit(i)) {


                /*here, it for projective coordinates it is important to use the normalized P as the argument.*/
                line = R.computeLine(pNormalized);

                millerVariable = millerVariable.mul(evaluateLine(line, R, qNormalized));

                /*
                 * V=V+P
                 */

                R = (PairingSourceGroupElement) R.add(pNormalized, line);
                //	System.out.println(line[0]+ " " + line[1]);

            }

        }

        //millerVariable is not an element of target group because it has not been exponentiated by cofactor yet.
        return (ExtensionFieldElement) millerVariable;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        AbstractPairing that = (AbstractPairing) other;
        return Objects.equals(g1, that.g1) &&
                Objects.equals(g2, that.g2) &&
                Objects.equals(gT, that.gT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(g1, g2, gT);
    }
}
