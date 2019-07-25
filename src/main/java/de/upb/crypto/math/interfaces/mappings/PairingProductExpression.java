package de.upb.crypto.math.interfaces.mappings;

import de.upb.crypto.math.interfaces.structures.FutureGroupElement;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.expressions.group.GroupElementExpression;
import de.upb.crypto.math.expressions.PowProductExpression;
import de.upb.crypto.math.structures.zn.Zn;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A mutable object containing an expression of the form
 * \prod_i e(g_i, h_i)^(x_i).
 */
public class PairingProductExpression implements GroupElementExpression {
    protected final BilinearMap bilinearMap;
    protected final BigInteger groupSize;

    /**
     * An expression of the form e(g,h),
     * where g,h may be group elements or
     * PowProductExpressions
     */
    public abstract class AtomicExpression {
        public abstract GroupElement getG();

        public abstract GroupElement getH();

        public abstract PowProductExpression getGExpression();

        public abstract PowProductExpression getHExpression();

        public boolean isSymmetricPair() {
            return bilinearMap.isSymmetric() && getGExpression().equals(getHExpression());
        }
    }

    /**
     * An expression of the form e(g,h).
     * This is what should usually be used.
     */
    public class PairingExpression extends AtomicExpression {
        protected GroupElement g, h;

        public PairingExpression(GroupElement g, GroupElement h) {
            if (g == null || h == null)
                throw new NullPointerException("Cannot instantiate PairingExpression with null");
            this.g = g;
            this.h = h;
        }

        public GroupElement getG() {
            return g;
        }

        public GroupElement getH() {
            return h;
        }

        @Override
        public PowProductExpression getGExpression() {
            return new PowProductExpression(g);
        }

        @Override
        public PowProductExpression getHExpression() {
            return new PowProductExpression(h);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PairingExpression that = (PairingExpression) o;
            return Objects.equals(g, that.g) &&
                    Objects.equals(h, that.h) ||
                    bilinearMap.isSymmetric() &&
                            Objects.equals(g, that.h) &&
                            Objects.equals(h, that.g);
        }

        @Override
        public int hashCode() {
            if (bilinearMap.isSymmetric())
                return g.hashCode() + h.hashCode();
            else
                return Objects.hash(g, h);
        }
    }

    /**
     * An expression of the form e(g,h), where g,h are PowProductExpressions.
     */
    public class SymbolicPairingExpression extends AtomicExpression {
        protected PowProductExpression g, h;

        public SymbolicPairingExpression(PowProductExpression g, PowProductExpression h) {
            if (g == null || h == null)
                throw new NullPointerException("Cannot instantiate SymbolicPairingExpression with null");
            this.g = g;
            this.h = h;
        }

        @Override
        public GroupElement getG() {
            return g.evaluate();
        }

        @Override
        public GroupElement getH() {
            return h.evaluate();
        }

        public PowProductExpression getGExpression() {
            return g;
        }

        public PowProductExpression getHExpression() {
            return h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SymbolicPairingExpression that = (SymbolicPairingExpression) o;
            return Objects.equals(g, that.g) &&
                    Objects.equals(h, that.h) ||
                    bilinearMap.isSymmetric() &&
                            Objects.equals(g, that.h) &&
                            Objects.equals(h, that.g);
        }

        @Override
        public int hashCode() {
            if (bilinearMap.isSymmetric())
                return g.hashCode() + h.hashCode();
            else
                return Objects.hash(g, h);
        }
    }

    /**
     * Maps an AtomicExpression in the product to its exponent.
     * i.e. the product is \prod_((g,x)\in expr) g^x.
     */
    private HashMap<AtomicExpression, BigInteger> expr;

    private final BiFunction<BigInteger, BigInteger, BigInteger> addAndReduce;
    private final BiFunction<BigInteger, BigInteger, BigInteger> mulAndReduce;

    /**
     * Instantiates an expression. Initially, the expression is empty
     * (i.e. evaluates to bilMap.getGT().getNeutralElement())
     */
    public PairingProductExpression(BilinearMap bilMap) {
        this.bilinearMap = bilMap;
        groupSize = bilMap.getGT().size();

        if (groupSize == null) {
            throw new IllegalArgumentException("Group size needs to be known");
        }
        this.addAndReduce = (x, y) -> x.add(y).mod(groupSize);
        this.mulAndReduce = (x, y) -> x.multiply(y).mod(groupSize);

        expr = new HashMap<>();
    }

    public PairingProductExpression(PairingProductExpression expr) {
        this(expr.bilinearMap);
        op(expr);
    }

    /**
     * Returns the bilinear map that this expression is defined over.
     */
    public BilinearMap getBilinearMap() {
        return bilinearMap;
    }

    /**
     * Adds a factor e(g,h) without exponent to the expression.
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression op(GroupElement g, GroupElement h) {
        return op(g, h, BigInteger.ONE);
    }

    /**
     * Adds a factor e(g,h) without exponent to the expression.
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression op(PowProductExpression g, PowProductExpression h) {
        return op(g, h, BigInteger.ONE);
    }

    /**
     * Adds a factor e(g,h)^x to the expression.
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression op(GroupElement g, GroupElement h, BigInteger x) {
        return op(new PairingExpression(g, h), x);
    }

    /**
     * Adds a factor e(g,h)^x to the expression.
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression op(PowProductExpression g, PowProductExpression h, BigInteger x) {
        return op(new SymbolicPairingExpression(g, h), x);
    }

    /**
     * Adds a factor e(g,h)^x to the expression.
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression op(AtomicExpression pair, BigInteger x) {
        expr.merge(pair, x, addAndReduce);
        return this;
    }

    /**
     * Adds a factor e(g,h)^x to the expression.
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression op(GroupElement g, GroupElement h, Zn.ZnElement x) {
        return op(g, h, x.getInteger());
    }

    /**
     * Adds a factor e(g,h)^x to the expression.
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression op(PowProductExpression g, PowProductExpression h, Zn.ZnElement x) {
        return op(g, h, x.getInteger());
    }

    /**
     * Adds the factors present in expr to this expression,
     * i.e. it basically sets this = this*expr.
     *
     * @param e another product expression (will not be modified)
     * @return "this" for chaining.
     */
    public PairingProductExpression op(PairingProductExpression e) {
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
    public PairingProductExpression pow(BigInteger x) {
        expr.replaceAll((g, x2) -> mulAndReduce.apply(x, x2));
        return this;
    }

    /**
     * Exponentiates the current expression by x.
     * (i.e. multiply each exponent with x)
     *
     * @param x an exponent
     * @return "this" for chaining.
     */
    public PairingProductExpression pow(Zn.ZnElement x) {
        return pow(x.getInteger());
    }

    /**
     * Inverts the current expression.
     * (i.e. multiply each exponent with -1)
     *
     * @return "this" for chaining.
     */
    public PairingProductExpression inv() {
        return pow(BigInteger.ONE.negate());
    }

    /**
     * Flatten expressions e(g^a * g2^b, h^c) to e(g,h)^ac * e(g2,h)^bc (this is in case expressions are grouped unfortunately. Then the next steps groups them "more smartly")
     */
    private static PairingProductExpression flattenExpression(PairingProductExpression expr) {
        PairingProductExpression result = new PairingProductExpression(expr.bilinearMap);
        for (Map.Entry<AtomicExpression, BigInteger> entry : expr.expr.entrySet()) {
            if (entry.getKey() instanceof SymbolicPairingExpression) {
                entry.getKey().getGExpression().forEach((g, xg) -> {
                    entry.getKey().getHExpression().forEach((h, xh) -> {
                        result.op(expr.new PairingExpression(g, h), xg.multiply(xh).multiply(entry.getValue()));
                    });
                });
            } else {
                result.op(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    public PairingProductExpression staticOptimization() {
        if (getNumberOfFactors() == 0)
            return new PairingProductExpression(this);

        PairingProductExpression result = this;
        //result = flattenExpression(result);

        return new PairingProductExpression(result);
    }

    public PairingProductExpression dynamicOptimization() {
        PairingProductExpression optimized = new PairingProductExpression(bilinearMap);

        optimized.op(this);
        //todo optimize something simple?

        return optimized;
    }

    /**
     * Replaces all occurences of SymbolicPairingExpression with
     * PairingExpressions.
     * <p>
     * This may allow for more optimization, but is not usually needed.
     *
     * @return "this" for chaining
     */
    protected PairingProductExpression normalizeSymbolicPairingExpressions() {
        PairingProductExpression expr2 = new PairingProductExpression(bilinearMap);
        forEach((k, v) -> {
            if (k instanceof SymbolicPairingExpression)
                expr2.op(k.getG(), k.getH(), v);
            else
                expr2.op(k, v);
        });

        this.expr = expr2.expr;
        return this;
    }

    /**
     * Computes the value of this expression.
     */
    @Override
    public GroupElement evaluate() {
        return bilinearMap.evaluate(this);
    }

    /**
     * Computes the value of this expression. Compared to {@link #evaluate()} this might happen concurrently.
     * For more information, see {@link BilinearMap#evaluateConcurrent(PairingProductExpression)}.
     * <p>
     * The result is being processed on another thread.
     * The result is a {@link FutureGroupElement}. When calling
     * any operation on the {@link FutureGroupElement}, the caller thread
     * may be blocked until the value is ready.
     */
    public FutureGroupElement evaluateConcurrent() {
        return bilinearMap.evaluateConcurrent(this);
    }

    /**
     * Returns the stored expression as a map of PairingExpressions to their exponents.
     */
    public Map<AtomicExpression, BigInteger> getExpression() {
        return Collections.unmodifiableMap(expr);
    }

    /**
     * Iterates over the factors (e(g,h),x).
     * The exponents x are guaranteed to be between 0 and e.getGT.size().
     */
    public void forEach(BiConsumer<? super AtomicExpression, ? super BigInteger> consumer) {
        expr.forEach((k, v) -> consumer.accept((AtomicExpression) k, v));
    }

    /**
     * Returns a stream of (e(g,h),x) pairs
     */
    public Stream<Map.Entry<AtomicExpression, BigInteger>> stream() {
        return expr.entrySet().stream();
    }

    public int getNumberOfFactors() {
        return expr.size();
    }

    @Override
    public boolean equals(Object o) { //tests equality on expression level, it may return false even if two expressions evaluate to the same value.
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairingProductExpression that = (PairingProductExpression) o;
        return Objects.equals(bilinearMap, that.bilinearMap) &&
                Objects.equals(expr, that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr);
    }
}
