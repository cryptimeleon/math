package de.upb.crypto.math.structures.sn;

import de.upb.crypto.math.interfaces.hash.ByteAccumulator;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupImpl;
import de.upb.crypto.math.interfaces.structures.group.impl.GroupElementImpl;
import de.upb.crypto.math.serialization.BigIntegerRepresentation;
import de.upb.crypto.math.serialization.ListRepresentation;
import de.upb.crypto.math.serialization.Representation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

/**
 * The group Sn for a natural number n is the set of permutations
 * \(\pi : \{1,...,n\} \rightarrow \{1,...,n\}\) where the group operation is function composition.
 */
public class Sn implements GroupImpl {
    protected int n;
    private SnElementImpl identity = null;

    /**
     * Constructs Sn for n.
     */
    public Sn(int n) {
        this.n = n;
    }

    public Sn(Representation repr) {
        this.n = repr.bigInt().get().intValue();
    }

    /**
     * Returns the integer n of this group, such that its permutation map from and onto \(\{1,...,n\}\).
     */
    public int getN() {
        return n;
    }

    @Override
    public BigInteger size() throws UnsupportedOperationException {
        //size is n! (n factorial)
        BigInteger size = BigInteger.ONE;
        for (int i = 2; i <= n; i++)
            size = size.multiply(BigInteger.valueOf(i));
        return size;
    }

    @Override
    public boolean hasPrimeSize() throws UnsupportedOperationException {
        return n == 2;
    }

    @Override
    public double estimateCostInvPerOp() {
        return 1;
    }

    @Override
    public Representation getRepresentation() {
        return new BigIntegerRepresentation(BigInteger.valueOf(n));
    }

    @Override
    public SnElementImpl getNeutralElement() {
        if (identity == null)
            identity = new SnElementImpl(Function.identity());
        return identity;
    }

    @Override
    public SnElementImpl getUniformlyRandomElement() throws UnsupportedOperationException {
        ArrayList<Integer> images = new ArrayList<>();
        for (int i = 1; i <= n; i++)
            images.add(i);
        Collections.shuffle(images);

        return new SnElementImpl(i -> images.get(i - 1)); //(we could do this more efficiently)
    }

    @Override
    public SnElementImpl getElement(Representation repr) {
        return new SnElementImpl(repr);
    }

    @Override
    public GroupElementImpl getGenerator() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * A permutation on \(\{1,...,n\}\).
     */
    public class SnElementImpl implements GroupElementImpl, Function<Integer, Integer> {
        /**
         * Contains the images of this permutation in order.
         * Specifically, {@code images[i] = j} is equivalent to i being mapped to j.
         * Note that images[0] is unused as we map \(\{1,...,n\}\).
         */
        protected int[] images;

        /**
         * Constructor for java's serialization.
         */
        private SnElementImpl() {

        }

        /**
         * Recreate from representation.
         */
        public SnElementImpl(Representation repr) {
            this(i -> repr.list().get(i).bigInt().get().intValue());
        }

        /**
         * Create from a mapping.
         */
        public SnElementImpl(Function<Integer, Integer> permutation) {
            images = new int[n + 1];
            for (int i = 1; i <= n; i++)
                images[i] = permutation.apply(i);
        }

        /**
         * Create from a list of images, which contains the images of this permutation in order.
         * Specifically, {@code images[i] = j} is equivalent to i being mapped to j.
         * Note that images[0] is unused as we map \(\{1,...,n\}\).
         */
        public SnElementImpl(int[] images) {
            this.images = Arrays.copyOf(images, n + 1);
            if (!checkValidElement()) throw new IllegalArgumentException(this + " is not a permutation");
        }

        /**
         * Checks that this claimed permutation is indeed bijective.
         */
        public boolean checkValidElement() {
            try {
                int[] inverse = new int[n + 1];
                for (int i = 1; i <= n; i++) {
                    if (inverse[images[i]] != 0)
                        return false; //not injective
                    inverse[images[i]] = i;
                }

                images[0] = 1;
                if (!Arrays.stream(inverse).allMatch(x -> x > 0)) //not surjective
                    return false;
                images[0] = 0;
            } catch (ArrayIndexOutOfBoundsException e) {
                return false;
            }
            return true;
        }

        @Override
        public Representation getRepresentation() {
            ListRepresentation repr = new ListRepresentation();
            Arrays.stream(images).mapToObj(i -> new BigIntegerRepresentation(i)).forEachOrdered(x -> repr.put(x));
            return repr;
        }

        @Override
        public GroupImpl getStructure() {
            return Sn.this;
        }

        @Override
        public SnElementImpl inv() {
            int[] inverse = new int[n + 1];
            for (int i = 0; i <= n; i++)
                inverse[images[i]] = i;
            return createElement(inverse);
        }

        @Override
        public SnElementImpl op(GroupElementImpl e) throws IllegalArgumentException {
            if (!e.getStructure().equals(getStructure()))
                throw new IllegalArgumentException("Trying to operate on elements of two different groups");

            int[] rhs = ((SnElementImpl) e).images;
            int[] lhs = images;
            int[] result = new int[n + 1];
            for (int i = 1; i <= n; i++)
                result[i] = lhs[rhs[i]];

            return createElement(result);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(images);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SnElementImpl) || !((SnElementImpl) obj).getStructure().equals(getStructure()))
                return false;
            return Arrays.equals(((SnElementImpl) obj).images, this.images);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 1; i <= n; i++)
                builder.append(i == 1 ? "" : " ").append(images[i]);
            builder.append("]");
            return builder.toString();
        }

        @Override
        public Integer apply(Integer i) {
            return images[i];
        }

        @Override
        public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
            int nLength = BigInteger.valueOf(n).toByteArray().length;
            for (int i = 1; i <= n; i++) {
                int j = images[i];
                BigInteger bigI = BigInteger.valueOf(j);
                accumulator.appendPadded(nLength, bigI.toByteArray());
            }
            return accumulator;
        }
    }

    /**
     * Creates a permutation using a given array of images.
     * <p>
     * The resulting {@code SnElementImpl} depending on the passed images, may not actually be a correct permutation.
     * Furthermore, since the set of images is passed by reference, and not cloned, it could potentially be mutated
     * from the outside.
     */
    private SnElementImpl createElement(int[] images) {
        SnElementImpl result = new SnElementImpl();
        result.images = images;
        return result;
    }

    /**
     * Creates a permutation from a string of images, each of which is separated by an empty space.
     * <p>
     * Specifically, the format is {@code "[image1 image2 image3]"}.
     */
    public static SnElementImpl createElementFromString(String str) {
        str = str.substring(1, str.length() - 1);
        Integer[] ints = Arrays.stream(str.split(" ")).map(s -> Integer.parseInt(s)).toArray(len -> new Integer[len]);
        int n = ints.length - 1;
        Sn sn = new Sn(n);
        return sn.new SnElementImpl(i -> ints[i - 1]);
    }

    @Override
    public int hashCode() {
        return n;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Sn && ((Sn) obj).n == n;
    }

    @Override
    public String toString() {
        return "S" + n;
    }

    @Override
    public Optional<Integer> getUniqueByteLength() {
        return Optional.of(n * BigInteger.valueOf(n).toByteArray().length);
    }

    @Override
    public boolean isCommutative() {
        return n <= 2;
    }
}
