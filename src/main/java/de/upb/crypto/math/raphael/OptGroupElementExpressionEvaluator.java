package de.upb.crypto.math.raphael;

import de.upb.crypto.math.expressions.bool.BooleanExpression;
import de.upb.crypto.math.expressions.group.*;
import de.upb.crypto.math.interfaces.structures.GroupElement;

import java.math.BigInteger;
import java.util.*;

public class OptGroupElementExpressionEvaluator implements GroupElementExpressionEvaluator {

    @Override
    public GroupElement evaluate(GroupElementExpression expr) {
        EvalSearchMultiExpContext multiExpContext = new EvalSearchMultiExpContext();
        boolean inInversion = false;
        constructMultiExp(expr, inInversion, multiExpContext);
        System.out.println(multiExpContext.toString());

        return evaluateMultiExp(multiExpContext);
    }

    private GroupElement evaluateMultiExp(EvalSearchMultiExpContext multiExpContext) {
        return multiExpContext.bases.get(0);
    }

    public void constructMultiExp(GroupElementExpression expr, boolean inInversion,
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

        List<GroupElement> bases;
        List<BigInteger> exponents;

        public EvalSearchMultiExpContext() {
            bases = new ArrayList<>();
            exponents = new ArrayList<>();
        }

        public void addExponentiation(GroupElement base, BigInteger exponent, boolean inInversion) {
            bases.add(base);
            if (inInversion) {
                exponents.add(BigInteger.ZERO.subtract(exponent));
            } else {
                exponents.add(exponent);
            }
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
