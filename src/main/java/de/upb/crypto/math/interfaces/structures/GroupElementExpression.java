package de.upb.crypto.math.interfaces.structures;

/**
 * {@link Expression} that evaluates to a {@link GroupElement}.
 */
public interface GroupElementExpression extends Expression {
    GroupElement evaluate();

    FutureGroupElement evaluateConcurrent();

    @Override
    GroupElementExpression staticOptimization();

    @Override
    GroupElementExpression dynamicOptimization();
}
