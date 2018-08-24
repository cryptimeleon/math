package de.upb.crypto.math.interfaces.structures;


import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A mutable object containing an expression of the form
 * \prod_i (g_i)^(x_i).
 * <p>
 * Such an expression can be evaluated in a fast way
 * using Group::evaluate().
 */
public class PowProductExpression implements GroupElementExpression {
    protected final Group group;
    /**
     * The group's size (cached)
     */
    protected final BigInteger size;

    /**
     * Maps a GroupElement in the product to its exponent.
     * i.e. the product is \prod_((g,x)\in expr) g^x.
     */
    private HashMap<GroupElement, BigInteger> expr;

    private final BiFunction<BigInteger, BigInteger, BigInteger> addAndReduce;
    private final BiFunction<BigInteger, BigInteger, BigInteger> mulAndReduce;

    /**
     * Instantiates an expression. Initially, the expression is empty
     * (i.e. evaluates to group.getNeutralElement())
     */
    public PowProductExpression(Group group) {
        this.group = group;
        size = group.size();

        if (!group.isCommutative())
            throw new UnsupportedOperationException("PowProductExpression does not support non-abelian groups");

        if (size == null) {
            this.addAndReduce = BigInteger::add;
            this.mulAndReduce = BigInteger::multiply;
        } else {
            this.addAndReduce = (x, y) -> x.add(y).mod(size);
            this.mulAndReduce = (x, y) -> x.multiply(y).mod(size);
        }

        expr = new HashMap<>();
    }

    /**
     * Instantiates an expression containing exactly the element g
     */
    public PowProductExpression(GroupElement g) {
        this(g.getStructure());
        op(g);
    }

    /**
     * Copies the expression from the supplied powProductExpression
     */
    public PowProductExpression(PowProductExpression powProductExpression) {
        this(powProductExpression.group);
        op(powProductExpression);
    }

    /**
     * Adds a factor without exponent to the expression.
     *
     * @param g a factor
     * @return "this" for chaining.
     */
    public PowProductExpression op(GroupElement g) {
        return op(g, BigInteger.ONE);
    }

    /**
     * Adds a factor g^x to the expression.
     *
     * @param g a group element
     * @param x its exponent
     * @return "this" for chaining.
     */
    public PowProductExpression op(GroupElement g, BigInteger x) {
        if (size == null && x.signum() < 0) {
            g = g.inv();
            x = x.negate();
        } else if (size != null) {
            x = x.mod(size);
        }

        expr.merge(g, x, addAndReduce);
        return this;
    }

    /**
     * Adds a factor g^x to the expression.
     *
     * @param g a group element
     * @param x its exponent
     * @return "this" for chaining.
     */
    public PowProductExpression op(GroupElement g, Zn.ZnElement x) {
        return op(g, x.getInteger());
    }

    /**
     * Adds a factor g^x to the expression.
     *
     * @param g a group element
     * @param x its exponent
     * @return "this" for chaining.
     */
    public PowProductExpression op(GroupElement g, long x) {
        return op(g, BigInteger.valueOf(x));
    }

    /**
     * Adds the factors present in expr to this expression,
     * i.e. it basically sets this = this*expr.
     *
     * @param e another product expression (will not be modified)
     * @return "this" for chaining.
     */
    public PowProductExpression op(PowProductExpression e) {
        e.forEach(this::op);
        return this;
    }

    /**
     * Exponentiates the current expression by x.
     * (i.e. multiply each exponent with x)
     *
     * @param x an exponent
     * @return "this" for chaining.
     */
    public PowProductExpression pow(BigInteger x) {
        if (size == null && x.signum() < 0) {
            PowProductExpression inverted = new PowProductExpression(group);
            forEach((g, x2) -> inverted.op(g.inv(), x2.multiply(x.negate())));
            this.expr = inverted.expr;
        } else {
            BigInteger xNormalized = size == null ? x : x.mod(size);
            expr.replaceAll((g, x2) -> mulAndReduce.apply(xNormalized, x2));
        }

        return this;
    }

    /**
     * Exponentiates the current expression by x.
     * (i.e. multiply each exponent with x)
     *
     * @param x an exponent
     * @return "this" for chaining.
     */
    public PowProductExpression pow(Zn.ZnElement x) {
        return pow(x.getInteger());
    }

    /**
     * Inverts the current expression.
     * (i.e. multiply each exponent with -1)
     *
     * @return "this" for chaining.
     */
    public PowProductExpression inv() {
        return pow(BigInteger.ONE.negate());
    }

    @Override
    public PowProductExpression staticOptimization() {
        return new PowProductExpression(this);
    }

    /**
     * Optimizes expression, replacing g^a * h^a with (gh)^a,
     * and, if the group has known size and inversion is cheap, replaces
     * g^a * h^(-a) with (g*h^(-1))^a.
     */
    private static PowProductExpression groupByExponents(PowProductExpression expr) {
        if (expr.expr.size() == 1)
            return new PowProductExpression(expr);
        //group equal exponents x = y and negated exponents x = -y
        HashMap<BigInteger, GroupElement> groupedByExponents = new HashMap<>();
        HashMap<BigInteger, GroupElement> negativeExponents = new HashMap<>();
        if (expr.size != null) {
            expr.forEach((g, x) -> {
                if (x.equals(BigInteger.ZERO)) //filter out zero exponents - nothing to do for those
                    return;
                BigInteger minusX = expr.size.subtract(x);
                if (groupedByExponents.keySet().contains(minusX) && expr.group.estimateCostOfInvert() <= 200)
                    negativeExponents.merge(x, g, GroupElement::op);
                else
                    groupedByExponents.merge(x, g, GroupElement::op);
            });
        } else {
            expr.forEach((g, x) -> groupedByExponents.merge(x, g, GroupElement::op));
        }
        PowProductExpression result = new PowProductExpression(expr.group);
        groupedByExponents.forEach((x, g) -> result.op(g, x));
        negativeExponents.forEach((x, g) -> result.op(g.inv(), expr.size.subtract(x))); //safe against nullpointer access on expr.size.

        return result;
    }

    /**
     * Analyses exponents, observing that if exponent bitstrings a,b of two group elements g,h
     * have many ones in common, it makes sense to compute (gh)^(a AND b) * g^(a - a AND b) * h^(b - a AND b) instead.
     *
     * @param expr an expression with nonnegative exponents
     */
    private static PowProductExpression squareMultiplyOptimization(PowProductExpression expr) {
        if (expr.expr.size() == 1)
            return new PowProductExpression(expr);

        PowProductExpression result = expr;
        int bitCountThreshold = expr.group.size().bitLength() / 2; //for two random bit strings, we'd expect an intersection (bitwise "AND") of size group.size()/4
        while (bitCountThreshold > 2) { //the bitCountThreshold is the number of bits that two bit strings must have in common in order to be aggregated. We start with higher values as that results in better optimization.
            PowProductExpression resultNext = new PowProductExpression(expr.group); //will eventually hold an equivalent but optimized expression
            HashSet<GroupElement> doneValue = new HashSet<>(); //set of values we've already added to resultNext

            for (Map.Entry<GroupElement, BigInteger> entry : result.expr.entrySet()) {
                if (doneValue.contains(entry.getKey()))
                    continue;
                BigInteger exp = entry.getValue();
                GroupElement g = entry.getKey();
                for (Map.Entry<GroupElement, BigInteger> entry2 : result.expr.entrySet()) {
                    if (doneValue.contains(entry2.getKey()) || entry.getKey().equals(entry2.getKey()))
                        continue;
                    BigInteger intersection = exp.and(entry2.getValue());
                    if (intersection.bitCount() > bitCountThreshold) {
                        resultNext.op(g.op(entry2.getKey()), intersection);
                        exp = exp.subtract(intersection);
                        resultNext.op(entry2.getKey(), entry2.getValue().subtract(intersection));
                        doneValue.add(entry2.getKey());
                    }
                }
                resultNext.op(g, exp);
                doneValue.add(g);
            }
            bitCountThreshold /= 2;
            result = resultNext;
        }
        return result;
    }

    /**
     * Easy baseline optimization: compute (g^(-1))^(-a) instead of g^a if -a has fewer ones in its
     * bit representation.
     */
    private static PowProductExpression chooseInversionOverExponentiation(PowProductExpression expr) {
        if (expr.group.size() == null)
            return expr;

        PowProductExpression optimized = new PowProductExpression(expr.group);
        expr.forEach((g, x) -> {
            BigInteger minusX = expr.size.subtract(x);
            if (x.bitCount() < minusX.bitCount() + expr.group.estimateCostOfInvert() / 100)
                optimized.op(g, x);
            else
                optimized.op(g.inv(), minusX);
        });
        //TODO optimize further, taking into account bigLength() (i.e. the number of square steps).

        return optimized;
    }

    @Override
    public PowProductExpression dynamicOptimization() {
        PowProductExpression result = chooseInversionOverExponentiation(this);
        result = groupByExponents(result);
        if (group.size() != null)
            result = squareMultiplyOptimization(result);

        return result;
    }

    /**
     * Outputs the BigInteger::bitLength of the largest exponent that
     * comes up in forEach.
     */
    public int getLargestExponentBitLength() {
        return expr.values().stream().mapToInt(BigInteger::bitLength).max().orElse(0);
    }

    /**
     * Computes the value of this expression.
     */
    @Override
    public GroupElement evaluate() {
        return group.evaluate(this);
    }

    /**
     * Computes the value of this expression. Compared to {@link #evaluate()} this might happen concurrently.
     * For more information, see {@link Group#evaluateConcurrent(PowProductExpression)}.
     * <p>
     * The result is being processed on another thread.
     * The result is a {@link FutureGroupElement}. When calling
     * any operation on the {@link FutureGroupElement}, the caller thread
     * may be blocked until the value is ready.
     * <p>
     * Usual use:
     * <p>
     * FutureGroupElement v = lhs.evaluateConcurrent();
     * FutureGroupElement w = rhs.evaluateConcurrent();
     * <p>
     * GroupElement result = bilinearMap.apply(v.get(), w.get());
     */
    @Override
    public FutureGroupElement evaluateConcurrent() {
        return group.evaluateConcurrent(this);
    }

    /**
     * Returns the stored expression as a map of group elements to their exponents.
     */
    public Map<GroupElement, BigInteger> getExpression() {
        return Collections.unmodifiableMap(expr);
    }

    /**
     * Iterates over the factors (g,x).
     * If group.size() != null, the exponents x are guaranteed to be between 0 and group.size().
     * If group.size() == null, the exponents are nonnegative.
     */
    public void forEach(BiConsumer<? super GroupElement, ? super BigInteger> consumer) {
        expr.forEach(consumer);
    }

    /**
     * Returns a stream of (g,x) pairs
     */
    public Stream<Map.Entry<GroupElement, BigInteger>> stream() {
        return expr.entrySet().stream();
    }

    @Override
    public boolean equals(Object o) { //tests equality on expression level, it may return false even if two expressions evaluate to the same value.
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PowProductExpression that = (PowProductExpression) o;
        return Objects.equals(group, that.group) &&
                Objects.equals(expr, that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, expr);
    }
}
