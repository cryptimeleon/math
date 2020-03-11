package de.upb.crypto.math.performance.expressions;

import de.upb.crypto.math.expressions.exponent.ExponentConstantExpr;
import de.upb.crypto.math.expressions.exponent.ExponentExpr;
import de.upb.crypto.math.expressions.exponent.ExponentVariableExpr;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.group.GroupEmptyExpr;
import de.upb.crypto.math.expressions.group.PairingExpr;
import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn;

import java.util.Arrays;

/**
 * This class allows generating various expressions for tests to use.
 *
 * @author Raphael Heitjohann
 */
public class ExpressionGenerator {

    /**
     * Generates a simple multi-exponentiation with the given number bases and exponents.
     * @param numBases How many different bases the multi-exponentiation should contain. If this
     *                 is less than the number of exponents, the bases will be reused in order.
     * @param numExponents How many different exponents the multi-exponentiation should contain.
     * @return An expression of the form b1^e1 op b2^e2 ...
     */
    public static GroupElementExpression genMultiExponentiation(Group group, int numBases,
                                                         int numExponents) {
        GroupElement[] bases = new GroupElement[numBases];
        for (int i = 0; i < bases.length; ++i) {
            bases[i] = group.getUniformlyRandomNonNeutral();
        }
        //System.out.println("Chose bases: " + Arrays.toString(bases));
        Zn.ZnElement[] exponents = new Zn.ZnElement[numExponents];
        for (int i = 0; i < exponents.length; ++i) {
            exponents[i] = group.getZn().getUniformlyRandomElement();
        }
        //System.out.println("Chose exponents: " + Arrays.toString(exponents));
        GroupElementExpression expr = group.expr();
        if (exponents.length >= bases.length) {
            for (int i = 0; i < exponents.length; ++i) {
                expr = expr.opPow(bases[i % numBases], exponents[i]);
            }
        } else {
            for (int i = 0; i < bases.length; ++i) {
                expr = expr.opPow(bases[i], exponents[i % numExponents]);
            }
        }

        return expr;
    }

    /**
     * Generate one pairing with contained multi-exponentiations on each side.
     * @param bilMap The bilinear map for the pairing.
     * @return The constructed expression.
     */
    public static GroupElementExpression
    genPairingWithMultiExp(BilinearMap bilMap, int leftNumBases, int leftNumExponents,
                           int rightNumBases, int rightNumExponents) {
        return new PairingExpr(
                bilMap,
                ExpressionGenerator
                        .genMultiExponentiation(bilMap.getG1(), leftNumBases, leftNumExponents),
                ExpressionGenerator
                        .genMultiExponentiation(bilMap.getG2(), rightNumBases, rightNumExponents)
        );
    }

    /**
     * Generates a multi-exponentiation of the form e(g_1[1], g_2[1])^e_1 op e(g_1[2], g_2[2])^e_2 ...
     * @param bilMap The bilinear map to use for the pairing.
     * @param numPairings The number of pairings in the multi-exponentiation.
     * @return The constructed expression.
     */
    public static GroupElementExpression genPairingWithMultiExpOutside(BilinearMap bilMap, int numPairings,
                                                                       boolean useVarExponents) {
        GroupElement[] g1Elements = new GroupElement[numPairings];
        GroupElement[] g2Elements = new GroupElement[numPairings];
        ExponentExpr[] exponents = new ExponentExpr[numPairings];
        for (int i = 0; i < numPairings; ++i) {
            g1Elements[i] = bilMap.getG1().getUniformlyRandomNonNeutral();
            g2Elements[i] = bilMap.getG2().getUniformlyRandomNonNeutral();
            if (useVarExponents) {
                exponents[i] = new ExponentVariableExpr("x" + i);
            } else {
                exponents[i] = new ExponentConstantExpr(bilMap.getGT().getZn().getUniformlyRandomElement());
            }
        }
        GroupElementExpression expr = new GroupEmptyExpr(bilMap.getGT());

        for (int i = 0; i < numPairings; ++i) {
            expr = expr.opPow(new PairingExpr(bilMap, g1Elements[i].expr(), g2Elements[i].expr()), exponents[i]);
        }
        return expr;
    }
}
