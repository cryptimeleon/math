package de.upb.crypto.math.raphael;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.interfaces.structures.Group;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

public class OptGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    private boolean enableCaching;

    public OptGroupElementExpressionEvaluator() {
        enableCaching = true;
    }

    @Override
    public GroupElement evaluate(GroupElementExpression expr) {
        MultiExpContext multiExpContext = new MultiExpContext();
        boolean inInversion = false;
        MultiExpContext(expr, inInversion, multiExpContext);
        if (!multiExpContext.allBasesSameGroup()) {
            throw new IllegalArgumentException("Expression contains elements with different" +
                    "groups outside of pairings.");
        }

        return evaluateMultiExp(multiExpContext);
    }

    private GroupElement evaluateMultiExp(MultiExpContext multiExpContext) {
        // use swantes recommendations
        if (enableCaching && multiExpContext.bases.size() < 10) {
            // either 1 or 2 window size
            return simultaneousSlidingWindowMulExp(multiExpContext, 1);
        } else {
            if (enableCaching) {
                // TODO: chose sensible large value but not too large
                return interleavingSlidingWindowMultiExp(multiExpContext, 8);
            } else {
                return interleavingSlidingWindowMultiExp(multiExpContext, 4);
            }
        }
    }

    private GroupElement simultaneousSlidingWindowMulExp(MultiExpContext multiExpContext,
                                                         int windowSize) {
        // TODO: we should not do any precomputations for bases with exponents 1
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<GroupElement> powerProducts = new ArrayList<>();
        // we just assume caching is enabled since only precomputations class has method
        // for computing product powers for now and this is only used when caching is enabled
        GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                GroupPrecomputationsFactory.get(bases.get(0).getStructure());
        powerProducts = groupPrecomputations.getPowerProducts(bases, windowSize);
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

        GroupElement A = bases.get(0).getStructure().getNeutralElement();
        int j = getLongestExponentBitLength(exponents) - 1;
        while (j >= 0) {
            final int finalj = j;
            if (IntStream.range(0, numBases)
                    .noneMatch(it -> posExponents.get(it).testBit(finalj))) {
                A = A.op(A);
                j--;
            } else {
                int jNew = Math.max(j - windowSize, -1);
                int J = jNew + 1;

                while (true) {
                    final int finalJ = J;
                    if (IntStream.range(0, numBases)
                            .anyMatch(it -> posExponents.get(it).testBit(finalJ))) {
                        break;
                    }
                    J++;
                }
                int e = 0;
                for (int i = numBases - 1; i >= 0; i--) {
                    int ePart = 0;
                    for (int k = j; k >= J; k--) {
                        ePart <<= 1;
                        if (posExponents.get(i).testBit(k)) {
                            ePart++;
                        }
                    }
                    e <<= windowSize;
                    e |= ePart;
                }
                while (j >= J) {
                    A = A.op(A);
                    j--;
                }
                A = A.op(powerProducts.get(e));
                while (j > jNew) {
                    A = A.op(A);
                    j--;
                }
            }
        }
        return A;
    }

    private GroupElement interleavingSlidingWindowMultiExp(MultiExpContext multiExpContext,
                                                           int windowSize) {
        // TODO: we should not do any precomputations for bases with exponents 1
        List<GroupElement> bases = multiExpContext.getBases();
        List<BigInteger> exponents = multiExpContext.getExponents();
        List<List<GroupElement>> oddPowers = new ArrayList<>();
        if (enableCaching) {
            GroupPrecomputationsFactory.GroupPrecomputations groupPrecomputations =
                    GroupPrecomputationsFactory.get(bases.get(0).getStructure());
            for (GroupElement base : bases) {
                oddPowers.add(groupPrecomputations.getOddPowers(base, (1 << windowSize) - 1));
            }
        } else {
            for (GroupElement base : bases) {
                oddPowers.add(base.precomputeSmallOddPowers((1 << windowSize) - 1));
            }
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
                    while (!testBit(posExponents.get(i), J)) {
                        J++;
                    }
                    wh[i] = J;
                    e[i] = 0;
                    for (int k = j; k >= J; k--) {
                        e[i] <<= 1;
                        if (testBit(posExponents.get(i), k)) {
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

    private boolean testBit(BigInteger n, int index) {
        if (index < 0) {
            return false;
        }
        return n.testBit(index);
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

    private void MultiExpContext(GroupElementExpression expr, boolean inInversion,
                                 MultiExpContext multiExpContext) {
        if (expr instanceof GroupOpExpr) {
            GroupOpExpr op_expr = (GroupOpExpr) expr;
            // group not necessarily commutative, so if we are in inversion, switch order
            if (inInversion) {
                MultiExpContext(op_expr.getRhs(), inInversion, multiExpContext);
                MultiExpContext(op_expr.getLhs(), inInversion, multiExpContext);
            } else {
                MultiExpContext(op_expr.getLhs(), inInversion, multiExpContext);
                MultiExpContext(op_expr.getRhs(), inInversion, multiExpContext);
            }
        } else if (expr instanceof GroupInvExpr) {
            MultiExpContext(((GroupInvExpr) expr).getBase(), !inInversion, multiExpContext);
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

    protected static class MultiExpContext {

        private List<GroupElement> bases;
        private List<BigInteger> exponents;

        MultiExpContext() {
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

        boolean allBasesSameGroup() {
            Group group = bases.get(0).getStructure();
            for (GroupElement base : bases) {
                if (base.getStructure() != group) {
                    return false;
                }
            }
            return true;
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

    public void setEnableCaching(boolean newSetting) {
        enableCaching = newSetting;
    }
}
