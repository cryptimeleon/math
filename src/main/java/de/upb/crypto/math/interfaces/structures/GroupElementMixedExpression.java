package de.upb.crypto.math.interfaces.structures;

import de.upb.crypto.math.interfaces.mappings.BilinearMap;
import de.upb.crypto.math.interfaces.mappings.PairingProductExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;

/**
 * A GroupElementExpression of the form PowProductExpression * PairingProductExpression.
 * This also works on groups that are not the target group of a pairing (although for obvious reasons,
 * the PairingProductExpression part is empty then).
 */
public class GroupElementMixedExpression implements GroupElementExpression {
    protected PowProductExpression powExpr; //may be null, which means it's the neutral element
    protected PairingProductExpression pairExpr; //may be null, which means it's the neutral element

    /**
     * Instantiates the expression powExpr * pairExpr (where * is the group operation in GT)
     */
    public GroupElementMixedExpression(PowProductExpression powExpr, PairingProductExpression pairExpr) {
        this.powExpr = powExpr;
        this.pairExpr = pairExpr;
    }

    /**
     * Instantiates the expression powExpr
     */
    public GroupElementMixedExpression(PowProductExpression powExpr) {
        this(powExpr, null);
    }

    /**
     * Instantiates the expression pairExpr
     */
    public GroupElementMixedExpression(PairingProductExpression pairExpr) {
        this(null, pairExpr);
    }

    /**
     * Returns a reference to the powExpr part of this mixed expression.
     * May be null. If it is not, then any modification also changes this object.
     */
    public PowProductExpression getPowExpr() {
        return powExpr;
    }

    /**
     * Returns a reference to the pairExpr part of this mixed expression.
     * May be null. If it is not, then any modification also changes this object.
     */
    public PairingProductExpression getPairExpr() {
        return pairExpr;
    }

    /**
     * Adds the factors present in powExpr to this expression,
     * i.e. it basically sets this = this*expr.
     *
     * @param powExpr a product expression (will not be modified)
     * @return "this" for chaining.
     */
    public GroupElementMixedExpression op(PowProductExpression powExpr) {
        ensurePowExprNotNull(powExpr.group);
        this.powExpr.op(powExpr);
        return this;
    }

    /**
     * Adds the factors present in pairExpr to this expression,
     * i.e. it basically sets this = this*expr.
     *
     * @param pairExpr a pairing expression (will not be modified)
     * @return "this" for chaining.
     */
    public GroupElementMixedExpression op(PairingProductExpression pairExpr) {
        ensurePairExprNowNull(pairExpr.getBilinearMap());
        this.pairExpr.op(pairExpr);
        return this;
    }

    /**
     * Adds the factors present in mixedExpr to this expression,
     * i.e. it basically sets this = this*expr.
     *
     * @param mixedExpr a mixed expression (will not be modified)
     * @return "this" for chaining.
     */
    public GroupElementMixedExpression op(GroupElementMixedExpression mixedExpr) {
        if (mixedExpr.powExpr != null)
            op(mixedExpr.powExpr);
        if (mixedExpr.pairExpr != null)
            op(mixedExpr.pairExpr);
        return this;
    }

    /**
     * Interprets the given mixed expressions as expressions in G1 and G2,
     * respectively, pairs them, and adds them to this expression.
     * More specifically, it sets this = this * e(mixedExprLhs, mixedExprRhs).
     *
     * @param bilinearMap  bilinear map with which to pair the elements
     * @param mixedExprLhs left hand side of the pairing
     * @param mixedExprRhs right hand side of the pairing
     * @return this modified object, for chaining.
     */
    /*public GroupElementMixedExpression pairAndOp(BilinearMap bilinearMap,
                                                 GroupElementMixedExpression mixedExprLhs,
                                                 GroupElementMixedExpression mixedExprRhs) {
        if (mixedExprLhs.pairExpr != null || mixedExprRhs.pairExpr != null)
            throw new RuntimeException("Cannot pair elements already in GT");

        ensurePairExprNowNull(bilinearMap);
        this.pairExpr.op(mixedExprLhs.powExpr, mixedExprRhs.powExpr);

        return this;
    }*/

    /**
     * Raises this expression to the power of exponent, i.e. sets this = this^exponent.
     *
     * @return this for chaining
     */
    public GroupElementMixedExpression pow(BigInteger exponent) {
        if (this.powExpr != null)
            this.powExpr.pow(exponent);
        if (this.pairExpr != null)
            this.pairExpr.pow(exponent);
        return this;
    }

    /**
     * Raises this expression to the power of exponent, i.e. sets this = this^exponent.
     *
     * @return this modified object, for chaining
     */
    public GroupElementMixedExpression pow(Zn.ZnElement exponent) {
        return this.pow(exponent.getInteger());
    }

    /**
     * Inverts this expression, i.e. sets this = this^{-1}
     *
     * @return this modified object, for chaining.
     */
    public GroupElementMixedExpression inv() {
        if (powExpr != null)
            powExpr.inv();
        if (pairExpr != null)
            pairExpr.inv();
        return this;
    }

    @Override
    public GroupElement evaluate() {
        if (powExpr != null && pairExpr != null)
            return powExpr.evaluate().op(pairExpr.evaluate());
        if (powExpr != null)
            return powExpr.evaluate();
        if (pairExpr != null)
            return pairExpr.evaluate();

        throw new RuntimeException("GroupElementMixedExpression is completely empty. Cannot evaluate.");
    }

    @Override
    public FutureGroupElement evaluateConcurrent() {
        if (powExpr != null && pairExpr != null) {
            FutureGroupElement powResult = powExpr.evaluateConcurrent();
            FutureGroupElement pairResult = pairExpr.evaluateConcurrent();
            return new FutureGroupElement(() -> powResult.get().op(pairResult.get()));
        }
        if (powExpr != null)
            return powExpr.evaluateConcurrent();
        if (pairExpr != null)
            return pairExpr.evaluateConcurrent();

        throw new RuntimeException("GroupElementMixedExpression is completely empty. Cannot evaluate.");
    }

    @Override
    public GroupElementExpression staticOptimization() {
        return new GroupElementMixedExpression(powExpr.staticOptimization(), pairExpr.staticOptimization());
    }

    @Override
    public GroupElementExpression dynamicOptimization() {
        return new GroupElementMixedExpression(powExpr.dynamicOptimization(), pairExpr.dynamicOptimization());
    }

    protected void ensurePowExprNotNull(Group group) {
        if (this.powExpr == null)
            powExpr = new PowProductExpression(group);
    }

    protected void ensurePairExprNowNull(BilinearMap bilinearMap) {
        if (this.pairExpr == null)
            pairExpr = new PairingProductExpression(bilinearMap);
    }
}
