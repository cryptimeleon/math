package de.upb.crypto.math.interfaces.structures;

/**
 * Expression that can be evaluated to a {@link Element} value. For example, see
 * {@link de.upb.crypto.math.interfaces.mappings.PairingProductExpression} and
 * {@link de.upb.crypto.math.interfaces.structures.PowProductExpression}.
 */
public interface Expression {
    Element evaluate();

    /**
     * Tries to optimize the given expression.
     * In contrast to dynamicOptimization(), this does optimization
     * that could have been easily done by rewriting the expression by hand.
     * Hence for well-optimized expressions, this call should just waste time.
     * <p>
     * If you already optimized the product by hand, this call should
     * be avoided (as it may only present unnecessary overhead).
     *
     * @return an optimized version of this expression (the object this is called on is not touched)
     */
    Expression staticOptimization();

    /**
     * Optimizes this expression to speed up evaluation.
     * This optimization is something that can usually not be done statically by rewriting
     * the expression symbolically.
     * It is also fairly cheap to execute, so it should usually just be applied for all expressions.
     *
     * @return an optimized version of this expression (the object this is called on is not touched)
     */
    Expression dynamicOptimization();
}
