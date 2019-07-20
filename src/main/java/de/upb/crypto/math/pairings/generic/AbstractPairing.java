package de.upb.crypto.math.pairings.generic;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.interfaces.structures.FieldElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.serialization.ObjectRepresentation;
import de.upb.crypto.math.serialization.RepresentableRepresentation;
import de.upb.crypto.math.serialization.Representation;
import de.upb.crypto.math.swante.MyUtil;

import java.math.BigInteger;

/**
 * Base class for pairings based on BN curves such as Tate Pairing, Ate Pairing and Optimal Ate Pairing.
 *
 * @author peter.guenther
 */
public abstract class AbstractPairing implements BilinearMap {
    protected PairingSourceGroup g1;
    protected PairingSourceGroup g2;
    protected PairingTargetGroup gT;
    
    
    public PairingSourceGroupElement getUnitRandomElementFromG2Group() {
        BigInteger e = MyUtil.randBig(g2.size().subtract(BigInteger.ONE));
        return (PairingSourceGroupElement) this.g2.generator.pow(e);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((g1 == null) ? 0 : g1.hashCode());
        result = prime * result + ((g2 == null) ? 0 : g2.hashCode());
        result = prime * result + ((gT == null) ? 0 : gT.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractPairing)) {
            return false;
        }
        AbstractPairing other = (AbstractPairing) obj;
        if (g1 == null) {
            if (other.g1 != null) {
                return false;
            }
        } else if (!g1.equals(other.g1)) {
            return false;
        }
        if (g2 == null) {
            if (other.g2 != null) {
                return false;
            }
        } else if (!g2.equals(other.g2)) {
            return false;
        }
        if (gT == null) {
            if (other.gT != null) {
                return false;
            }
        } else if (!gT.equals(other.gT)) {
            return false;
        }
        return true;
    }

    protected void init(PairingSourceGroup g1, PairingSourceGroup g2, PairingTargetGroup gT) {
        this.g1 = g1;
        this.g2 = g2;
        this.gT = gT;
    }

    public AbstractPairing(PairingSourceGroup g1, PairingSourceGroup g2, PairingTargetGroup gT) {
        init(g1, g2, gT);
    }

    @Override
    public PairingTargetGroupElement evaluate(PairingProductExpression expr) {
        expr = expr.dynamicOptimization();
        //we assume that operations in g1 are more efficient than in g2 and gt
        FieldElement result = expr.stream()
                .map(x -> this.pair(
                        (PairingSourceGroupElement) x.getKey().getGExpression().pow(x.getValue()).evaluate(), //exponentiate in G1
                        (PairingSourceGroupElement) x.getKey().getH()))
                .reduce(ExtensionFieldElement::mul)
                .orElse(this.getGT().getFieldOfDefinition().getOneElement());

        /*perform final exponentiation at product*/
        return this.exponentiate(result);
    }

    @Override
    public PairingTargetGroupElement apply(GroupElement g, GroupElement h, BigInteger exponent) {
        return exponentiate(pair((PairingSourceGroupElement) g.pow(exponent), (PairingSourceGroupElement) h));
    } // swante: why g immediately power by exponent? (exponent is equal to 1)

    /**
     * Implements final exponentiation.
     * <p>
     * Exponentiation of f to the power of e=(q^k-1)/r where r is the size of groups
     * G1, G2, and GT, respectively.
     *
     * @param f the element to exponentiate
     * @return f^e
     */
    public PairingTargetGroupElement exponentiate(FieldElement f) {
        PairingTargetGroupElement result = gT.getElement((ExtensionFieldElement) f.pow(gT.getCofactor()));
        return result;
    }

    @Override
    public Representation getRepresentation() {
        ObjectRepresentation or = new ObjectRepresentation();


        or.put("G1", new RepresentableRepresentation(this.getG1()));
        or.put("G2", new RepresentableRepresentation(this.getG2()));
        or.put("GT", new RepresentableRepresentation(this.getGT()));

        return or;
    }

    public AbstractPairing(Representation r) {
        ObjectRepresentation or = (ObjectRepresentation) r;

        init(
                (PairingSourceGroup) ((RepresentableRepresentation) or.get("G1")).recreateRepresentable(),
                (PairingSourceGroup) ((RepresentableRepresentation) or.get("G2")).recreateRepresentable(),
                (PairingTargetGroup) ((RepresentableRepresentation) or.get("GT")).recreateRepresentable()
        );
    }

    @Override
    public PairingSourceGroup getG1() {
        return g1;
    }

    @Override
    public PairingSourceGroup getG2() {
        return g2;
    }

    @Override
    public PairingTargetGroup getGT() {
        return gT;
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
        ExtensionField targetField = (ExtensionField) this.getGT().getFieldOfDefinition();
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


}
