package de.upb.crypto.math.raphael;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.*;

public class OptGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    @Override
    public GroupElement evaluate(GroupElementExpression expr) {
        EvalSearchMultiExpContext multiExpContext = new EvalSearchMultiExpContext();
        boolean inInversion = false;
        constructMultiExp(expr, inInversion, multiExpContext);

        return evaluateMultiExp(multiExpContext);
    }

    private GroupElement evaluateMultiExp(EvalSearchMultiExpContext multiExpContext) {
        return interleavingSlidingWindowMultiExp(multiExpContext, 4);
    }

    private
    GroupElement interleavingSlidingWindowMultiExp(EvalSearchMultiExpContext multiExpContext,
                                                   int windowSize) {
        // Enable caching by default for now
        // TODO: we should not do any precompuations for bases with exponents 1
        //  in other words, we should select window size individually
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                GroupPrecomputationsFactory.get(bases.get(0).getStructure());
        List<List<GroupElement>> oddPowers = new ArrayList<>();
        for (GroupElement base : bases) {
            oddPowers.add(groupPrecomputations.getOddPowers(base, (1 << windowSize) - 1));
        }
        int numBases = bases.size();
        // make sure all exponents are positive
        List<BigInteger> posExponents = new ArrayList<>();
        for (int i = 0; i < exponents.size(); ++i) {
            BigInteger exp = exponents.get(i);
            if (exp.compareTo(BigInteger.ZERO) < 0) {
                posExponents.add(exp.mod(bases.get(i).getStructure().size()));
            } else {
                posExponents.add(exp);
            }
        }

        // we are assuming that every base has same underlying group
        // TODO: this only works if every exponent has longer bit length than window size
        GroupElement A = bases.get(0).getStructure().getNeutralElement();
        int longestExponentBitLength = getLongestExponentBitLength(posExponents);
        int[] wh = new int[numBases];
        int[] e = new int[numBases];
        for (int i = 0; i < numBases; i++) {
            wh[i] = -1;
        }
        for (int j = longestExponentBitLength - 1; j >= 0; j--) {
            if (j != longestExponentBitLength - 1) {
                A = A.op(A);
            }
            for (int i = 0; i < numBases; i++) {
                if (wh[i] == -1 && posExponents.get(i).testBit(j)) {
                    int J = j - windowSize + 1;
                    while (!posExponents.get(i).testBit(J)) {
                        J++;
                    }
                    wh[i] = J;
                    e[i] = 0;
                    for (int k = j; k >= J; k--) {
                        e[i] <<= 1;
                        if (posExponents.get(i).testBit(k)) {
                            e[i]++;
                        }
                    }
                }
                if (wh[i] == j) {
                    A = A.op(oddPowers.get(i).get(e[i] / 2));
                    wh[i] = -1;
                }
            }
        }
        return A;
    }

    private int getLongestExponentBitLength(List<BigInteger> exponents) {
        int max = 1;
        for (BigInteger exp : exponents) {
            if (exp.bitLength() > max) {
                max = exp.bitLength();
            }
        }
        return max;
    }

    private void constructMultiExp(GroupElementExpression expr, boolean inInversion,
                                   EvalSearchMultiExpContext multiExpContext) {
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr op_expr = (GroupOpExpr) expr;
            // group not necessarily commutative, so if we are in inversion, switch order
            if (inInversion) {
                constructMultiExp(op_expr.getRhs(), inInversion, multiExpContext);
                constructMultiExp(op_expr.getLhs(), inInversion, multiExpContext);
            } else {
                constructMultiExp(op_expr.getLhs(), inInversion, multiExpContext);
                constructMultiExp(op_expr.getRhs(), inInversion, multiExpContext);
            }
        } else if (expr instanceof GroupInvExpr) {
            constructMultiExp(((GroupInvExpr) expr).getBase(), !inInversion, multiExpContext);
        } else if (expr instanceof GroupPowExpr) {
            GroupPowExpr pow_expr = (GroupPowExpr) expr;
            // for now, just use evaluate naive on base and exponent
            multiExpContext.addExponentiation(
                    pow_expr.getBase().evaluate(),
                    pow_expr.getExponent().evaluate(),
                    inInversion
            );
        } else if (expr instanceof GroupElementConstantExpr) {
            GroupElementConstantExpr const_expr = (GroupElementConstantExpr) expr;
            // count this as basis too, multiexp algorithm can distinguish
            multiExpContext.addExponentiation(const_expr.evaluateNaive(), BigInteger.ONE,
                    inInversion);
        } else if (expr instanceof PairingExpr) {
            PairingExpr pair_expr = (PairingExpr) expr;
            // TODO: Can do this in parallel
            GroupElement lhs = pair_expr.evaluate(this);
            GroupElement rhs = pair_expr.evaluate(this);
            GroupElement pair_result = pair_expr.getMap().apply(lhs, rhs);
            // Also use this as basis for multiexp
            multiExpContext.addExponentiation(pair_result, BigInteger.ONE, inInversion);
        } else if (expr instanceof GroupVariableExpr) {
            throw new IllegalArgumentException("Cannot evaluate variable expression. " +
                    "Insert value first");
        } else if (expr instanceof GroupEmptyExpr) {
            throw new IllegalArgumentException("Cannot evaluate empty expression.");
        } else {
            throw new IllegalArgumentException("Found something in expression tree that" +
                    "is not a proper expression.");
        }
    }

    protected static class EvalSearchMultiExpContext {

        private List<GroupElement> bases;
        private List<BigInteger> exponents;

        EvalSearchMultiExpContext() {
            bases = new ArrayList<>();
            exponents = new ArrayList<>();
        }

        void addExponentiation(GroupElement base, BigInteger exponent, boolean inInversion) {
            bases.add(base);
            if (inInversion) {
                exponents.add(BigInteger.ZERO.subtract(exponent));
            } else {
                exponents.add(exponent);
            }
        }

        public List<GroupElement> getBases() {
            return bases;
        }

        public List<BigInteger> getExponents() {
            return exponents;
        }

        public String toString() {
            return "Bases: " + Arrays.toString(bases.toArray()) + "\n" +
                    "Exponents: " + Arrays.toString(exponents.toArray());
        }
    }

    @Override
    public GroupElementExpression optimize(GroupElementExpression expr) {
        return expr;
    }

    @Override
    public GroupElementExpression precompute(GroupElementExpression expr) {
        return expr;
    }

    @Override
    public BooleanExpression precompute(BooleanExpression expr) {
        return expr;
    }
}
