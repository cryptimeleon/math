package org.cryptimeleon.math.misc;

import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.prf.PrfImage;
import org.cryptimeleon.math.prf.PrfKey;
import org.cryptimeleon.math.prf.PrfPreimage;
import org.cryptimeleon.math.random.RandomGenerator;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

import java.util.Arrays;

public class ByteArrayImpl implements PrfKey, PrfPreimage, PrfImage {

    @Represented
    protected byte[] data;

    public ByteArrayImpl(byte[] bytes) {
        this.data = bytes;
    }

    public ByteArrayImpl(Representation repr) {
        new ReprUtil(this).deserialize(repr);
    }

    /**
     * Creates a new {@code ByteArrayImpl} instance filled with {@code numberBytes} bytes of randomness
     *
     * @param numberBytes number of random bytes / length of resulting ByteArrayImplementation
     */
    public static ByteArrayImpl fromRandom(int numberBytes) {
        return new ByteArrayImpl(RandomGenerator.getRandomBytes(numberBytes));
    }

    public byte[] getData() {
        return data;
    }

    /**
     * Create new byte array as concatenation of {@code this} with {@code a}.
     *
     * @param a the array to append
     * @return the result of concatenation
     */
    public ByteArrayImpl append(ByteArrayImpl a) {
        byte[] result = new byte[data.length + a.getData().length];
        System.arraycopy(data, 0, result, 0, data.length);
        System.arraycopy(a.data, 0, result, data.length, a.getData().length);
        return new ByteArrayImpl(result);
    }

    /**
     * Returns an array containing the designated part of this byte array, meaning that
     * {@code returned[i] = this[firstIndex+i]}.
     * @param firstIndex first (byte) index that will be copied to the result
     * @param length the number of bytes to be copied
     * @return a {@linkplain ByteArrayImpl} of length {@code length} that is a substring of this original.
     */
    public ByteArrayImpl substring(int firstIndex, int length) {
        byte[] result = new byte[length];
        System.arraycopy(data, firstIndex, result, 0, length);
        return new ByteArrayImpl(result);
    }

    /**
     * Returns the length of this byte array.
     *
     * @return the length of this byte array
     */
    public int length() {
        return data.length;
    }

    /**
     * Compute exclusive or of two byte arrays.
     * <p>
     * Returns a new byte array where the i-th entry is the exclusive or of {@code this}
     * byte array's i-th entry and {@code a}'s i-th entry.
     *
     * @param a the argument to XOR {@code this} with
     * @return the result of XORing
     */
    public ByteArrayImpl xor(ByteArrayImpl a) {
        int min = Math.min(this.length(), a.length());
        int max = Math.max(this.length(), a.length());

        byte[] result = new byte[max];
        for (int i = 0; i < min; i++) {
            result[i] = (byte) (this.getData()[i] ^ a.getData()[i]);
        }
        return new ByteArrayImpl(result);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ByteArrayImpl other = (ByteArrayImpl) obj;
        return Arrays.equals(data, other.data);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < this.getData().length; i++) {
            result.append(String.format("%d", Byte.toUnsignedInt(this.getData()[i])));
            if (i < this.getData().length - 1)
                result.append(",");
        }
        result.append("]");
        return result.toString();
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        accumulator.append(data);
        return accumulator;
    }
}
